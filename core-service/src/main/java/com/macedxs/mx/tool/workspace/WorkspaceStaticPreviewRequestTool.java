package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.Tool;
import com.macedxs.mx.tool.application.ToolDefinition;
import com.macedxs.mx.tool.application.ToolEffect;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Queues a host-side static preview recipe; it never starts an arbitrary process from the Java application. */
public final class WorkspaceStaticPreviewRequestTool implements Tool {

    public static final String NAME = "workspace.preview_static";
    private final Path workspaceRoot;
    private final ToolDefinition definition = new ToolDefinition(
            NAME,
            "1.0.0",
            "Enfileira um preview estático local em loopback após aprovação humana.",
            ToolEffect.WRITE,
            AutonomyLevel.EXECUTE_WITH_APPROVAL,
            Duration.ofSeconds(10),
            Set.of("project")
    );

    public WorkspaceStaticPreviewRequestTool(Path workspaceRoot) {
        if (workspaceRoot == null) {
            throw new IllegalArgumentException("Workspace root is required");
        }
        this.workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
    }

    @Override
    public ToolDefinition definition() {
        return definition;
    }

    @Override
    public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
        if (!WorkspaceToolSupport.isApprovedContext(context)) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PREVIEW_APPROVAL_REQUIRED");
        }
        String project = WorkspaceToolSupport.projectName(request);
        if (project == null) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PROJECT_INVALID");
        }
        try {
            Path projectRoot = workspaceRoot.resolve(project).normalize();
            if (!projectRoot.startsWith(workspaceRoot)
                    || Files.isSymbolicLink(workspaceRoot)
                    || Files.isSymbolicLink(projectRoot)
                    || !Files.isRegularFile(projectRoot.resolve("index.html"), LinkOption.NOFOLLOW_LINKS)) {
                return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PROJECT_NOT_PREVIEWABLE");
            }
            String requestId = UUID.randomUUID().toString();
            Path pending = workspaceRoot.resolve(".mx/preview-requests/pending").normalize();
            if (!pending.startsWith(workspaceRoot) || WorkspaceToolSupport.containsSymbolicLink(workspaceRoot, pending)) {
                return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PATH_FORBIDDEN");
            }
            Files.createDirectories(pending);
            String payload = "{\n" +
                    "  \"requestId\": \"" + requestId + "\",\n" +
                    "  \"recipe\": \"static-http\",\n" +
                    "  \"project\": \"" + project + "\",\n" +
                    "  \"host\": \"127.0.0.1\",\n" +
                    "  \"portRange\": \"48000-48099\",\n" +
                    "  \"requestedAt\": \"" + Instant.now() + "\"\n" +
                    "}\n";
            Files.writeString(pending.resolve(requestId + ".json"), payload, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return ToolResult.success(NAME, context.correlationId(), Map.of(
                    "requestId", requestId,
                    "project", project,
                    "state", "QUEUED",
                    "host", "127.0.0.1"
            ));
        } catch (IOException exception) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PREVIEW_QUEUE_FAILED");
        }
    }
}
