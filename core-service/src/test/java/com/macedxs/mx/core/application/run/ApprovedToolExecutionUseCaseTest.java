package com.macedxs.mx.core.application.run;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.PolicyEngine;
import com.macedxs.mx.tool.application.Tool;
import com.macedxs.mx.tool.application.ToolDefinition;
import com.macedxs.mx.tool.application.ToolEffect;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolExecutionResult;
import com.macedxs.mx.tool.application.ToolExecutor;
import com.macedxs.mx.tool.application.ToolRegistry;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovedToolExecutionUseCaseTest {

    @Test
    void shouldExecuteTheOriginalToolOnlyAfterValidApprovalAndPersistItsEvidence() {
        InMemoryExecutionRunStore store = new InMemoryExecutionRunStore();
        AtomicBoolean executed = new AtomicBoolean(false);
        ToolRegistry registry = new ToolRegistry();
        registry.register(new ControlledWriteTool(executed));
        ToolExecutor executor = new ToolExecutor(registry, new PolicyEngine(), store, Duration.ofMinutes(15));
        UUID userId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        ToolExecutionResult pending = executor.execute(
                new ToolRequest("workspace.write_file", Map.of("path", "notes/plan.md", "content", "approved")),
                new ToolExecutionContext(userId, correlationId, "development", AutonomyLevel.PROPOSE, Set.of("workspace.write_file")),
                false
        );

        assertThat(pending.requiresApproval()).isTrue();
        assertThat(executed).isFalse();

        Optional<ExecutionRunSnapshot> completed = new ApprovedToolExecutionUseCase(store, executor)
                .execute(userId, pending.approvalRunId(), pending.approvalNonce());

        assertThat(completed).isPresent();
        assertThat(completed.orElseThrow().status()).isEqualTo(RunStatus.COMPLETED);
        assertThat(completed.orElseThrow().output()).contains("workspace.write_file", "notes/plan.md");
        assertThat(executed).isTrue();
    }

    private static final class ControlledWriteTool implements Tool {
        private final AtomicBoolean executed;

        private ControlledWriteTool(AtomicBoolean executed) {
            this.executed = executed;
        }

        @Override
        public ToolDefinition definition() {
            return new ToolDefinition(
                    "workspace.write_file", "1.0.0", "Controlled write", ToolEffect.WRITE,
                    AutonomyLevel.EXECUTE_WITH_APPROVAL, Duration.ofSeconds(5), Set.of("path", "content")
            );
        }

        @Override
        public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
            executed.set(true);
            return ToolResult.success(definition().name(), context.correlationId(), Map.of("path", request.arguments().get("path")));
        }
    }

    private static final class InMemoryExecutionRunStore implements ExecutionRunStore {
        private final Map<UUID, ExecutionRunSnapshot> snapshots = new HashMap<>();

        @Override
        public ExecutionRunSnapshot save(ExecutionRun run) {
            ExecutionRunSnapshot snapshot = run.snapshot();
            snapshots.put(snapshot.runId(), snapshot);
            return snapshot;
        }

        @Override
        public Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId) {
            return Optional.ofNullable(snapshots.get(runId)).filter(snapshot -> snapshot.userId().equals(userId));
        }
    }
}
