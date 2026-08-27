package com.macedxs.mx.agent.skill.development;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.tool.application.PolicyEngine;
import com.macedxs.mx.tool.application.Tool;
import com.macedxs.mx.tool.application.ToolDefinition;
import com.macedxs.mx.tool.application.ToolEffect;
import com.macedxs.mx.tool.application.ToolExecutor;
import com.macedxs.mx.tool.application.ToolRegistry;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;
import com.macedxs.mx.core.application.run.ExecutionRunSnapshot;
import com.macedxs.mx.core.application.run.ExecutionRunStore;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class DevelopmentSkillTest {

    @Test
    void shouldExposeOnlyNamedWorkspaceRecipesAtProposalAutonomy() {
        var definition = new DevelopmentSkill(new FakeModelGateway("Resposta sem tool.")).definition();

        assertThat(definition.maximumAutonomy()).isEqualTo(AutonomyLevel.PROPOSE);
        assertThat(definition.allowedTools()).containsExactlyInAnyOrder(
                "workspace.read_file",
                "workspace.list",
                "workspace.write_file",
                "workspace.initialize_static_project",
                "workspace.preview_static"
        );
    }

    @Test
    void shouldExecuteAnExplicitReadOnlyToolAndGiveTheResultBackToTheModel() {
        AtomicBoolean executed = new AtomicBoolean(false);
        ToolRegistry registry = new ToolRegistry();
        registry.register(tool("workspace.read_file", ToolEffect.READ_ONLY, AutonomyLevel.EXECUTE_READ_ONLY, executed));
        FakeModelGateway gateway = new FakeModelGateway(
                "[MX_TOOL_CALL]{\"toolName\":\"workspace.read_file\",\"arguments\":{\"path\":\"README.md\"}}[/MX_TOOL_CALL]",
                "Encontrei o arquivo solicitado."
        );

        var result = new DevelopmentSkill(
                gateway,
                new ToolExecutor(registry, new PolicyEngine())
        ).execute(
                new SkillRequest("Leia o README"),
                new SkillExecutionContext(UUID.randomUUID(), UUID.randomUUID(), AutonomyLevel.EXECUTE_READ_ONLY)
        );

        assertThat(result.answer()).isEqualTo("Encontrei o arquivo solicitado.");
        assertThat(result.metadata()).containsEntry("toolExecuted", true);
        assertThat(executed).isTrue();
        assertThat(gateway.prompts()).hasSize(2);
    }

    @Test
    void shouldCreateApprovalMetadataAndNeverExecuteAWriteTool() {
        AtomicBoolean executed = new AtomicBoolean(false);
        CapturingStore store = new CapturingStore();
        ToolRegistry registry = new ToolRegistry();
        registry.register(tool("workspace.write_file", ToolEffect.WRITE, AutonomyLevel.EXECUTE_WITH_APPROVAL, executed));
        FakeModelGateway gateway = new FakeModelGateway(
                "[MX_TOOL_CALL]{\"toolName\":\"workspace.write_file\",\"arguments\":{\"path\":\"notes.txt\"}}[/MX_TOOL_CALL]"
        );

        var result = new DevelopmentSkill(
                gateway,
                new ToolExecutor(registry, new PolicyEngine(), store, Duration.ofMinutes(5))
        ).execute(
                new SkillRequest("Escreva uma anotação"),
                new SkillExecutionContext(UUID.randomUUID(), UUID.randomUUID(), AutonomyLevel.EXECUTE_READ_ONLY)
        );

        assertThat(result.metadata()).containsEntry("approvalRequired", true);
        assertThat(result.metadata()).containsKey("approvalRunId");
        assertThat(result.metadata()).containsKey("approvalNonce");
        assertThat(executed).isFalse();
        assertThat(store.saved).isNotNull();
    }

    @Test
    void shouldProposeStaticPreviewAndNeverStartItBeforeApproval() {
        AtomicBoolean executed = new AtomicBoolean(false);
        CapturingStore store = new CapturingStore();
        ToolRegistry registry = new ToolRegistry();
        registry.register(tool("workspace.preview_static", ToolEffect.WRITE, AutonomyLevel.EXECUTE_WITH_APPROVAL, executed));
        FakeModelGateway gateway = new FakeModelGateway(
                "[MX_TOOL_CALL]{\"toolName\":\"workspace.preview_static\",\"arguments\":{\"project\":\"portal-local\"}}[/MX_TOOL_CALL]"
        );

        var result = new DevelopmentSkill(
                gateway,
                new ToolExecutor(registry, new PolicyEngine(), store, Duration.ofMinutes(5))
        ).execute(
                new SkillRequest("Prepare um preview local do portal"),
                new SkillExecutionContext(UUID.randomUUID(), UUID.randomUUID(), AutonomyLevel.PROPOSE)
        );

        assertThat(result.metadata()).containsEntry("approvalRequired", true);
        assertThat(result.metadata()).containsEntry("toolName", "workspace.preview_static");
        assertThat(executed).isFalse();
        assertThat(store.saved).isNotNull();
    }

    private static Tool tool(String name, ToolEffect effect, AutonomyLevel minimumAutonomy, AtomicBoolean executed) {
        return new Tool() {
            @Override
            public ToolDefinition definition() {
                Set<String> requiredArguments = name.equals("workspace.preview_static") ? Set.of("project") : Set.of("path");
                return new ToolDefinition(name, "1.0.0", name, effect, minimumAutonomy,
                        Duration.ofSeconds(5), requiredArguments);
            }

            @Override
            public ToolResult execute(ToolRequest request, com.macedxs.mx.tool.application.ToolExecutionContext context) {
                executed.set(true);
                return ToolResult.success(name, context.correlationId(), Map.of("content", "conteúdo seguro"));
            }
        };
    }

    private static final class FakeModelGateway implements ModelGateway {
        private final Deque<String> answers = new ArrayDeque<>();
        private final java.util.List<String> prompts = new java.util.ArrayList<>();

        private FakeModelGateway(String... answers) {
            this.answers.addAll(java.util.List.of(answers));
        }

        @Override
        public ModelResponse complete(ModelRequest request) {
            prompts.add(request.prompt());
            return new ModelResponse(answers.removeFirst(), "test-model", 1L);
        }

        private java.util.List<String> prompts() {
            return prompts;
        }
    }

    private static final class CapturingStore implements ExecutionRunStore {
        private ExecutionRunSnapshot saved;

        @Override
        public ExecutionRunSnapshot save(com.macedxs.mx.core.application.run.ExecutionRun run) {
            saved = run.snapshot();
            return saved;
        }

        @Override
        public Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId) {
            return Optional.ofNullable(saved);
        }
    }
}
