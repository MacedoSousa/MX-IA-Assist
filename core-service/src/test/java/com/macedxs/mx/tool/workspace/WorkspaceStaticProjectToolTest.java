package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceStaticProjectToolTest {

    @TempDir
    Path workspace;

    @Test
    void shouldCreateOnlyAValidStaticProjectInsideTheWorkspaceAfterApproval() throws Exception {
        WorkspaceInitializeStaticProjectTool tool = new WorkspaceInitializeStaticProjectTool(workspace);

        var result = tool.execute(
                new ToolRequest("workspace.initialize_static_project", Map.of("project", "portfolio-local")),
                approvedContext()
        );

        assertThat(result.success()).isTrue();
        assertThat(result.output()).containsEntry("project", "portfolio-local");
        assertThat(Files.readString(workspace.resolve("portfolio-local/index.html"))).contains("MX Workspace");
        assertThat(Files.readString(workspace.resolve("portfolio-local/README.md"))).contains("Preview local");
    }

    @Test
    void shouldRejectAProjectNameThatCouldEscapeTheWorkspace() {
        WorkspaceInitializeStaticProjectTool tool = new WorkspaceInitializeStaticProjectTool(workspace);

        var result = tool.execute(
                new ToolRequest("workspace.initialize_static_project", Map.of("project", "../outside")),
                approvedContext()
        );

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("WORKSPACE_PROJECT_INVALID");
        assertThat(Files.exists(workspace.getParent().resolve("outside"))).isFalse();
    }

    @Test
    void shouldQueueALoopbackOnlyStaticPreviewForAnExistingProject() throws Exception {
        WorkspaceInitializeStaticProjectTool initializer = new WorkspaceInitializeStaticProjectTool(workspace);
        initializer.execute(
                new ToolRequest("workspace.initialize_static_project", Map.of("project", "previewable")),
                approvedContext()
        );
        WorkspaceStaticPreviewRequestTool preview = new WorkspaceStaticPreviewRequestTool(workspace);

        var result = preview.execute(
                new ToolRequest("workspace.preview_static", Map.of("project", "previewable")),
                approvedContext()
        );

        assertThat(result.success()).isTrue();
        String requestId = String.valueOf(result.output().get("requestId"));
        Path job = workspace.resolve(".mx/preview-requests/pending/" + requestId + ".json");
        assertThat(Files.readString(job)).contains("previewable", "static-http", "127.0.0.1");
    }

    private ToolExecutionContext approvedContext() {
        return new ToolExecutionContext(
                UUID.randomUUID(), UUID.randomUUID(), "development", AutonomyLevel.EXECUTE_WITH_APPROVAL
        );
    }
}
