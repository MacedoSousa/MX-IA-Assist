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
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

public class WorkspaceWriteTool implements Tool {

    private static final String NAME = "workspace.write_file";

    private final Path workspaceRoot;
    private final long maxBytes;
    private final ToolDefinition definition;

    public WorkspaceWriteTool(Path workspaceRoot, long maxBytes) {
        if (workspaceRoot == null) {
            throw new IllegalArgumentException("Workspace root is required");
        }
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("Maximum content size must be positive");
        }
        this.workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        this.definition = new ToolDefinition(
                NAME,
                "1.0.0",
                "Escreve texto em um arquivo relativo ao workspace após aprovação humana.",
                ToolEffect.WRITE,
                AutonomyLevel.EXECUTE_WITH_APPROVAL,
                Duration.ofSeconds(10),
                Set.of("path", "content")
        );
    }

    @Override
    public ToolDefinition definition() {
        return definition;
    }

    @Override
    public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
        if (request == null) {
            throw new IllegalArgumentException("Tool request is required");
        }
        if (context == null) {
            throw new IllegalArgumentException("Tool execution context is required");
        }
        if (context.grantedAutonomy() != AutonomyLevel.EXECUTE_WITH_APPROVAL) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_WRITE_APPROVAL_REQUIRED");
        }

        String relativePath;
        String content;
        try {
            relativePath = requireString(request.arguments().get("path"), "path");
            content = requireString(request.arguments().get("content"), "content");
        } catch (IllegalArgumentException exception) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_ARGUMENT_INVALID");
        }

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxBytes) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_CONTENT_TOO_LARGE");
        }

        Path target;
        try {
            Path requested = Path.of(relativePath);
            if (requested.isAbsolute() || relativePath.indexOf('\u0000') >= 0) {
                return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PATH_FORBIDDEN");
            }
            target = workspaceRoot.resolve(requested).normalize();
        } catch (InvalidPathException exception) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PATH_INVALID");
        }

        if (!target.startsWith(workspaceRoot) || target.equals(workspaceRoot)) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PATH_FORBIDDEN");
        }

        try {
            Files.createDirectories(workspaceRoot);
            if (containsSymbolicLink(workspaceRoot, target.getParent())
                    || Files.isSymbolicLink(target)) {
                return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PATH_FORBIDDEN");
            }

            Path parent = target.getParent();
            if (parent == null) {
                return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PATH_INVALID");
            }
            Files.createDirectories(parent);
            if (Files.isSymbolicLink(parent) || Files.isDirectory(target, LinkOption.NOFOLLOW_LINKS)) {
                return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PATH_INVALID");
            }

            Path temporary = Files.createTempFile(parent, ".mx-write-", ".tmp");
            try {
                Files.write(temporary, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException atomicMoveNotSupported) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }

            String normalizedPath = workspaceRoot.relativize(target).toString().replace('\\', '/');
            return ToolResult.success(NAME, context.correlationId(), Map.of(
                    "path", normalizedPath,
                    "bytes", (long) bytes.length
            ));
        } catch (IOException exception) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_WRITE_FAILED");
        }
    }

    private boolean containsSymbolicLink(Path root, Path candidate) throws IOException {
        if (candidate == null || !candidate.startsWith(root)) {
            return true;
        }
        Path current = root;
        Path relative = root.relativize(candidate);
        for (Path segment : relative) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                return true;
            }
        }
        return false;
    }

    private String requireString(Object value, String name) {
        if (!(value instanceof String string) || string.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return string;
    }
}
