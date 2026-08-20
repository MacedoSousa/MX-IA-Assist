package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceWriteToolTest {

    @TempDir
    Path workspace;

    @Test
    void shouldWriteFileInsideTheSandboxAfterApproval() throws Exception {
        WorkspaceWriteTool tool = new WorkspaceWriteTool(workspace, 1024);
        UUID correlationId = UUID.randomUUID();

        ToolResult result = tool.execute(
                new ToolRequest("workspace.write_file", Map.of(
                        "path", "notes/today.txt",
                        "content", "MX aprovado"
                )),
                context(correlationId, AutonomyLevel.EXECUTE_WITH_APPROVAL)
        );

        assertThat(result.success()).isTrue();
        assertThat(result.errorCode()).isNull();
        assertThat(Files.readString(workspace.resolve("notes/today.txt"))).isEqualTo("MX aprovado");
        assertThat(result.output()).containsEntry("path", "notes/today.txt");
        assertThat(result.output()).containsEntry("bytes", 11L);
    }

    @Test
    void shouldRejectTraversalWithoutTouchingTheFilesystem() {
        WorkspaceWriteTool tool = new WorkspaceWriteTool(workspace, 1024);

        ToolResult result = tool.execute(
                new ToolRequest("workspace.write_file", Map.of(
                        "path", "../outside.txt",
                        "content", "não deve sair"
                )),
                context(UUID.randomUUID(), AutonomyLevel.EXECUTE_WITH_APPROVAL)
        );

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("WORKSPACE_PATH_FORBIDDEN");
    }

    @Test
    void shouldRejectPayloadAboveConfiguredLimit() {
        WorkspaceWriteTool tool = new WorkspaceWriteTool(workspace, 4);

        ToolResult result = tool.execute(
                new ToolRequest("workspace.write_file", Map.of(
                        "path", "too-large.txt",
                        "content", "12345"
                )),
                context(UUID.randomUUID(), AutonomyLevel.EXECUTE_WITH_APPROVAL)
        );

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("WORKSPACE_CONTENT_TOO_LARGE");
    }

    @Test
    void shouldRejectExecutionWithoutWriteAutonomy() {
        WorkspaceWriteTool tool = new WorkspaceWriteTool(workspace, 1024);

        ToolResult result = tool.execute(
                new ToolRequest("workspace.write_file", Map.of(
                        "path", "blocked.txt",
                        "content", "blocked"
                )),
                context(UUID.randomUUID(), AutonomyLevel.EXECUTE_READ_ONLY)
        );

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("WORKSPACE_WRITE_APPROVAL_REQUIRED");
    }

    private ToolExecutionContext context(UUID correlationId, AutonomyLevel autonomy) {
        return new ToolExecutionContext(UUID.randomUUID(), correlationId, "development", autonomy);
    }
}
