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

/** Queues one fixed static workspace recipe; it never accepts or starts a model-provided command. */
public final class WorkspaceStaticRecipeRequestTool implements Tool {

    public static final String VALIDATE_NAME = "workspace.validate_static";
    public static final String BUILD_NAME = "workspace.build_static";
    private static final String PROFILE = "static-html-v1";

    private final Path workspaceRoot;
    private final String recipe;
    private final ToolDefinition definition;

    public WorkspaceStaticRecipeRequestTool(Path workspaceRoot, String name, String recipe, String description) {
        if (workspaceRoot == null || !Set.of(VALIDATE_NAME, BUILD_NAME).contains(name)
                || !Set.of("static-validate", "static-build").contains(recipe)) {
            throw new IllegalArgumentException("Static recipe configuration is invalid");
        }
        this.workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
        this.recipe = recipe;
        this.definition = new ToolDefinition(name, "1.0.0", description, ToolEffect.WRITE,
                AutonomyLevel.EXECUTE_WITH_APPROVAL, Duration.ofSeconds(10), Set.of("project"));
    }

    @Override
    public ToolDefinition definition() {
        return definition;
    }

    @Override
    public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
        if (!WorkspaceToolSupport.isApprovedContext(context)) {
            return ToolResult.failure(definition.name(), context.correlationId(), "WORKSPACE_RECIPE_APPROVAL_REQUIRED");
        }
        String project = WorkspaceToolSupport.projectName(request);
        if (project == null) {
            return ToolResult.failure(definition.name(), context.correlationId(), "WORKSPACE_PROJECT_INVALID");
        }
        try {
            Path projectRoot = workspaceRoot.resolve(project).normalize();
            if (!projectRoot.startsWith(workspaceRoot) || Files.isSymbolicLink(workspaceRoot)
                    || Files.isSymbolicLink(projectRoot)
                    || !Files.isRegularFile(projectRoot.resolve("index.html"), LinkOption.NOFOLLOW_LINKS)) {
                return ToolResult.failure(definition.name(), context.correlationId(), "WORKSPACE_PROJECT_NOT_STATIC");
            }
            Path pending = workspaceRoot.resolve(".mx/recipe-requests/pending").normalize();
            if (!pending.startsWith(workspaceRoot) || WorkspaceToolSupport.containsSymbolicLink(workspaceRoot, pending)) {
                return ToolResult.failure(definition.name(), context.correlationId(), "WORKSPACE_PATH_FORBIDDEN");
            }
            Files.createDirectories(pending);
            String requestId = UUID.randomUUID().toString();
            String payload = "{\n" +
                    "  \"requestId\": \"" + requestId + "\",\n" +
                    "  \"recipe\": \"" + recipe + "\",\n" +
                    "  \"profile\": \"" + PROFILE + "\",\n" +
                    "  \"project\": \"" + project + "\",\n" +
                    "  \"requestedAt\": \"" + Instant.now() + "\"\n" +
                    "}\n";
            Files.writeString(pending.resolve(requestId + ".json"), payload, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return ToolResult.success(definition.name(), context.correlationId(), Map.of(
                    "requestId", requestId, "project", project, "recipe", recipe, "profile", PROFILE, "state", "QUEUED"
            ));
        } catch (IOException exception) {
            return ToolResult.failure(definition.name(), context.correlationId(), "WORKSPACE_RECIPE_QUEUE_FAILED");
        }
    }
}
