package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.PolicyEngine;
import com.macedxs.mx.tool.application.PolicyOutcome;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolExecutionResult;
import com.macedxs.mx.tool.application.ToolExecutor;
import com.macedxs.mx.tool.application.ToolRegistry;
import com.macedxs.mx.tool.application.ToolRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceWriteToolPolicyTest {

    @TempDir
    Path workspace;

    @Test
    void shouldRequireApprovalAndNeverExecuteBeforeApproval() {
        WorkspaceWriteTool tool = new WorkspaceWriteTool(workspace, 1024);
        ToolRegistry registry = new ToolRegistry();
        registry.register(tool);
        ToolExecutor executor = new ToolExecutor(registry, new PolicyEngine());
        ToolRequest request = request();

        ToolExecutionResult result = executor.execute(
                request,
                context(AutonomyLevel.PROPOSE),
                false
        );

        assertThat(result.policyDecision().outcome()).isEqualTo(PolicyOutcome.REQUIRE_APPROVAL);
        assertThat(result.toolResult()).isNull();
        assertThat(Files.exists(workspace.resolve("approved.txt"))).isFalse();
    }

    @Test
    void shouldExecuteOnlyAfterApprovalAndSufficientAutonomy() throws Exception {
        WorkspaceWriteTool tool = new WorkspaceWriteTool(workspace, 1024);
        ToolRegistry registry = new ToolRegistry();
        registry.register(tool);
        ToolExecutor executor = new ToolExecutor(registry, new PolicyEngine());

        ToolExecutionResult result = executor.execute(
                request(),
                context(AutonomyLevel.EXECUTE_WITH_APPROVAL),
                true
        );

        assertThat(result.policyDecision().outcome()).isEqualTo(PolicyOutcome.ALLOW);
        assertThat(result.toolResult()).isNotNull();
        assertThat(result.toolResult().success()).isTrue();
        assertThat(Files.readString(workspace.resolve("approved.txt"))).isEqualTo("approved");
    }

    private ToolRequest request() {
        return new ToolRequest("workspace.write_file", Map.of(
                "path", "approved.txt",
                "content", "approved"
        ));
    }

    private ToolExecutionContext context(AutonomyLevel autonomy) {
        return new ToolExecutionContext(UUID.randomUUID(), UUID.randomUUID(), "development", autonomy);
    }
}
