package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceListToolTest {

    @TempDir
    Path workspace;

    @Test
    void shouldListOnlyRelativeEntriesInsideAuthorizedWorkspace() throws Exception {
        Files.createDirectories(workspace.resolve("src/main"));
        Files.writeString(workspace.resolve("README.md"), "MX");
        Files.writeString(workspace.resolve("src/main/App.java"), "class App {}");

        ToolResult result = tool(10, 3).execute(
                request("."),
                context()
        );

        assertThat(result.success()).isTrue();
        assertThat(result.errorCode()).isNull();
        assertThat(result.output().get("entries").toString())
                .contains("README.md", "src", "src/main");
        assertThat(result.output().toString()).doesNotContain(workspace.toString());
    }

    @Test
    void shouldBlockPathTraversal() {
        ToolResult result = tool(10, 3).execute(
                request("../outside"),
                context()
        );

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("WORKSPACE_PATH_FORBIDDEN");
    }

    @Test
    void shouldLimitNumberOfEntriesAndReportTruncation() throws Exception {
        Files.writeString(workspace.resolve("a.txt"), "a");
        Files.writeString(workspace.resolve("b.txt"), "b");
        Files.writeString(workspace.resolve("c.txt"), "c");

        ToolResult result = tool(2, 1).execute(
                request("."),
                context()
        );

        assertThat(result.success()).isTrue();
        assertThat((List<?>) result.output().get("entries")).hasSize(2);
        assertThat(result.output().get("truncated")).isEqualTo(true);
    }

    @Test
    void shouldRejectExecutionWithoutReadOnlyAutonomy() {
        ToolResult result = tool(10, 3).execute(
                request("."),
                new ToolExecutionContext(UUID.randomUUID(), UUID.randomUUID(), "development", AutonomyLevel.RESPOND)
        );

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("TOOL_AUTONOMY_DENIED");
    }

    private WorkspaceListTool tool(int maxEntries, int maxDepth) {
        return new WorkspaceListTool(workspace, maxEntries, maxDepth);
    }

    private ToolRequest request(String path) {
        return new ToolRequest("workspace.list", Map.of("path", path));
    }

    private ToolExecutionContext context() {
        return new ToolExecutionContext(UUID.randomUUID(), UUID.randomUUID(), "development", AutonomyLevel.EXECUTE_READ_ONLY);
    }
}
