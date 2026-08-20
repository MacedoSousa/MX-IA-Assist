package com.macedxs.mx.tool.application;

import com.macedxs.mx.agent.application.AutonomyLevel;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ToolExecutorTest {

    @Test
    void shouldExecuteOnlyAfterPolicyAllowsTheTool() {
        AtomicBoolean executed = new AtomicBoolean(false);
        ToolRegistry registry = new ToolRegistry();
        registry.register(tool("workspace.list", ToolEffect.READ_ONLY, AutonomyLevel.EXECUTE_READ_ONLY, executed));

        ToolExecutionResult result = new ToolExecutor(registry, new PolicyEngine()).execute(
                new ToolRequest("workspace.list", Map.of()),
                context(AutonomyLevel.EXECUTE_READ_ONLY),
                false
        );

        assertThat(result.policyDecision().outcome()).isEqualTo(PolicyOutcome.ALLOW);
        assertThat(result.toolResult()).isNotNull();
        assertThat(executed).isTrue();
    }

    @Test
    void shouldNotExecuteSensitiveToolBeforeApproval() {
        AtomicBoolean executed = new AtomicBoolean(false);
        ToolRegistry registry = new ToolRegistry();
        registry.register(tool("workspace.write_file", ToolEffect.WRITE, AutonomyLevel.EXECUTE_WITH_APPROVAL, executed));

        ToolExecutionResult result = new ToolExecutor(registry, new PolicyEngine()).execute(
                new ToolRequest("workspace.write_file", Map.of()),
                context(AutonomyLevel.PROPOSE),
                false
        );

        assertThat(result.policyDecision().outcome()).isEqualTo(PolicyOutcome.REQUIRE_APPROVAL);
        assertThat(result.toolResult()).isNull();
        assertThat(executed).isFalse();
    }

    private static Tool tool(
            String name,
            ToolEffect effect,
            AutonomyLevel minimumAutonomy,
            AtomicBoolean executed
    ) {
        return new Tool() {
            @Override
            public ToolDefinition definition() {
                return new ToolDefinition(name, "1.0.0", name, effect, minimumAutonomy, Duration.ofSeconds(5), Set.of());
            }

            @Override
            public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
                executed.set(true);
                return ToolResult.success(name, context.correlationId(), Map.of("ok", true));
            }
        };
    }

    private static ToolExecutionContext context(AutonomyLevel autonomyLevel) {
        return new ToolExecutionContext(UUID.randomUUID(), UUID.randomUUID(), "development", autonomyLevel);
    }
}
