package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceReadFileToolTest {

    @TempDir
    Path workspace;

    @Test
    void shouldReadUtf8TextInsideAuthorizedWorkspace() throws Exception {
        Files.writeString(workspace.resolve("README.md"), "MX local\nqualidade");

        ToolResult result = tool(1024).execute(request("README.md"), context());

        assertThat(result.success()).isTrue();
        assertThat(result.output()).containsEntry("content", "MX local\nqualidade");
        assertThat(result.output()).containsEntry("truncated", false);
        assertThat(result.output().get("path")).isEqualTo("README.md");
    }

    @Test
    void shouldBlockPathTraversal() {
        ToolResult result = tool(1024).execute(request("../outside.txt"), context());

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("WORKSPACE_PATH_FORBIDDEN");
    }

    @Test
    void shouldRejectBinaryFile() throws Exception {
        Files.write(workspace.resolve("image.bin"), new byte[]{0x01, 0x00, 0x02});

        ToolResult result = tool(1024).execute(request("image.bin"), context());

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("WORKSPACE_BINARY_FILE");
    }

    @Test
    void shouldTruncateLargeTextAndReportIt() throws Exception {
        Files.writeString(workspace.resolve("large.txt"), "0123456789");

        ToolResult result = tool(5).execute(request("large.txt"), context());

        assertThat(result.success()).isTrue();
        assertThat(result.output().get("truncated")).isEqualTo(true);
        assertThat(result.output().get("content").toString()).contains("truncado");
        assertThat(result.output().get("bytes")).isEqualTo(10);
    }

    @Test
    void shouldRejectExecutionWithoutReadOnlyAutonomy() throws Exception {
        Files.writeString(workspace.resolve("README.md"), "MX");

        ToolResult result = tool(1024).execute(
                request("README.md"),
                new ToolExecutionContext(UUID.randomUUID(), UUID.randomUUID(), "development", AutonomyLevel.RESPOND)
        );

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("TOOL_AUTONOMY_DENIED");
    }

    private WorkspaceReadFileTool tool(long maxBytes) {
        return new WorkspaceReadFileTool(workspace, maxBytes);
    }

    private ToolRequest request(String path) {
        return new ToolRequest("workspace.read_file", Map.of("path", path));
    }

    private ToolExecutionContext context() {
        return new ToolExecutionContext(UUID.randomUUID(), UUID.randomUUID(), "development", AutonomyLevel.EXECUTE_READ_ONLY);
    }
}
