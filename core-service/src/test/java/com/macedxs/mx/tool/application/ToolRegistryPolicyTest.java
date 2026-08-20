package com.macedxs.mx.tool.application;

import com.macedxs.mx.agent.application.AutonomyLevel;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolRegistryPolicyTest {

    @Test
    void shouldRegisterToolOnceAndRejectDuplicateNames() {
        ToolRegistry registry = new ToolRegistry();
        ToolDefinition definition = readTool();

        registry.register(new FakeTool(definition));

        assertThat(registry.getRequired("workspace.list").definition()).isEqualTo(definition);
        assertThatThrownBy(() -> registry.register(new FakeTool(definition)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void shouldAllowReadOnlyToolForReadOnlyAutonomy() {
        PolicyDecision decision = new PolicyEngine().evaluate(
                readTool(),
                new ToolRequest("workspace.list", Map.of("path", "D:/MX")),
                AutonomyLevel.EXECUTE_READ_ONLY,
                false
        );

        assertThat(decision.outcome()).isEqualTo(PolicyOutcome.ALLOW);
        assertThat(decision.reason()).contains("read-only");
    }

    @Test
    void shouldRequireApprovalForWriteTool() {
        PolicyDecision decision = new PolicyEngine().evaluate(
                writeTool(),
                new ToolRequest("workspace.write_file", Map.of("path", "D:/MX/a.txt", "content", "conteúdo de teste")),
                AutonomyLevel.PROPOSE,
                false
        );

        assertThat(decision.outcome()).isEqualTo(PolicyOutcome.REQUIRE_APPROVAL);
    }

    @Test
    void shouldDenyReadOnlyToolWhenCoreHasNotGrantedExecution() {
        PolicyDecision decision = new PolicyEngine().evaluate(
                readTool(),
                new ToolRequest("workspace.list", Map.of()),
                AutonomyLevel.RESPOND,
                false
        );

        assertThat(decision.outcome()).isEqualTo(PolicyOutcome.DENY);
    }

    private static ToolDefinition readTool() {
        return new ToolDefinition(
                "workspace.list",
                "1.0.0",
                "Lista arquivos de um workspace autorizado",
                ToolEffect.READ_ONLY,
                AutonomyLevel.EXECUTE_READ_ONLY,
                Duration.ofSeconds(10),
                Set.of("path")
        );
    }

    private static ToolDefinition writeTool() {
        return new ToolDefinition(
                "workspace.write_file",
                "1.0.0",
                "Escreve arquivo após aprovação",
                ToolEffect.WRITE,
                AutonomyLevel.EXECUTE_WITH_APPROVAL,
                Duration.ofSeconds(20),
                Set.of("path", "content")
        );
    }

    private record FakeTool(ToolDefinition definition) implements Tool {
        @Override
        public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
            return ToolResult.success(definition.name(), context.correlationId(), Map.of());
        }
    }
}
