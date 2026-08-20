package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.Tool;
import com.macedxs.mx.tool.application.ToolDefinition;
import com.macedxs.mx.tool.application.ToolEffect;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class WorkspaceListTool implements Tool {

    private final Path authorizedRoot;
    private final int maxEntries;
    private final int maxDepth;

    public WorkspaceListTool(Path authorizedRoot, int maxEntries, int maxDepth) {
        if (authorizedRoot == null) {
            throw new IllegalArgumentException("Authorized workspace root is required");
        }
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("Max entries must be positive");
        }
        if (maxDepth < 0) {
            throw new IllegalArgumentException("Max depth cannot be negative");
        }
        this.authorizedRoot = authorizedRoot.toAbsolutePath().normalize();
        this.maxEntries = maxEntries;
        this.maxDepth = maxDepth;
    }

    @Override
    public ToolDefinition definition() {
        return new ToolDefinition(
                "workspace.list",
                "1.0.0",
                "Lista entradas de um workspace autorizado",
                ToolEffect.READ_ONLY,
                AutonomyLevel.EXECUTE_READ_ONLY,
                java.time.Duration.ofSeconds(10),
                Set.of("path")
        );
    }

    @Override
    public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
        if (context == null || context.grantedAutonomy().ordinal() < AutonomyLevel.EXECUTE_READ_ONLY.ordinal()) {
            return ToolResult.failure(definition().name(), correlationId(context), "TOOL_AUTONOMY_DENIED");
        }
        if (request == null || !(request.arguments().get("path") instanceof String requestedPath)
                || requestedPath.isBlank()) {
            return ToolResult.failure(definition().name(), context.correlationId(), "INVALID_ARGUMENTS");
        }

        try {
            Path root = authorizedRoot.toRealPath();
            Path target = root.resolve(requestedPath).normalize();
            if (!target.startsWith(root)) {
                return ToolResult.failure(definition().name(), context.correlationId(), "WORKSPACE_PATH_FORBIDDEN");
            }
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                return ToolResult.failure(definition().name(), context.correlationId(), "WORKSPACE_PATH_NOT_FOUND");
            }
            if (Files.isSymbolicLink(target)) {
                return ToolResult.failure(definition().name(), context.correlationId(), "WORKSPACE_SYMLINK_FORBIDDEN");
            }

            List<Map<String, Object>> entries = new ArrayList<>();
            try (Stream<Path> paths = Files.walk(target, maxDepth)) {
                List<Path> candidates = paths
                        .filter(path -> !path.equals(target))
                        .filter(path -> !Files.isSymbolicLink(path))
                        .sorted(Comparator.comparing(path -> target.relativize(path).toString()))
                        .limit((long) maxEntries + 1)
                        .toList();

                boolean truncated = candidates.size() > maxEntries;
                candidates.stream()
                        .limit(maxEntries)
                        .forEach(path -> entries.add(toEntry(root, path)));

                return new ToolResult(
                        definition().name(),
                        context.correlationId(),
                        true,
                        Map.of("entries", List.copyOf(entries), "truncated", truncated),
                        null
                );
            }
        } catch (IOException exception) {
            return ToolResult.failure(definition().name(), context.correlationId(), "WORKSPACE_LIST_FAILED");
        }
    }

    private Map<String, Object> toEntry(Path root, Path path) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("path", root.relativize(path).toString().replace('\\', '/'));
        entry.put("type", Files.isDirectory(path) ? "DIRECTORY" : "FILE");
        if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            try {
                entry.put("size", Files.size(path));
            } catch (IOException ignored) {
                entry.put("size", -1L);
            }
        }
        return Map.copyOf(entry);
    }

    private java.util.UUID correlationId(ToolExecutionContext context) {
        return context == null ? java.util.UUID.randomUUID() : context.correlationId();
    }
}
