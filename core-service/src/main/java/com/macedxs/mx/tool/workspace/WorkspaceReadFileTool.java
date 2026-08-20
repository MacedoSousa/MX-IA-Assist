package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.Tool;
import com.macedxs.mx.tool.application.ToolDefinition;
import com.macedxs.mx.tool.application.ToolEffect;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Read-only text access constrained to the configured workspace root. */
public final class WorkspaceReadFileTool implements Tool {

    private final Path authorizedRoot;
    private final long maxBytes;

    public WorkspaceReadFileTool(Path authorizedRoot, long maxBytes) {
        if (authorizedRoot == null) {
            throw new IllegalArgumentException("Authorized workspace root is required");
        }
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("Max bytes must be positive");
        }
        this.authorizedRoot = authorizedRoot.toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    @Override
    public ToolDefinition definition() {
        return new ToolDefinition(
                "workspace.read_file",
                "1.0.0",
                "Lê um arquivo textual dentro do workspace autorizado",
                ToolEffect.READ_ONLY,
                AutonomyLevel.EXECUTE_READ_ONLY,
                Duration.ofSeconds(10),
                Set.of("path")
        );
    }

    @Override
    public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
        UUID correlationId = context == null ? UUID.randomUUID() : context.correlationId();
        if (context == null || context.grantedAutonomy().ordinal() < AutonomyLevel.EXECUTE_READ_ONLY.ordinal()) {
            return ToolResult.failure(definition().name(), correlationId, "TOOL_AUTONOMY_DENIED");
        }
        if (request == null || !(request.arguments().get("path") instanceof String requestedPath)
                || requestedPath.isBlank()) {
            return ToolResult.failure(definition().name(), correlationId, "INVALID_ARGUMENTS");
        }

        try {
            Path root = authorizedRoot.toRealPath();
            Path target = root.resolve(requestedPath).normalize();
            if (!target.startsWith(root)) {
                return ToolResult.failure(definition().name(), correlationId, "WORKSPACE_PATH_FORBIDDEN");
            }
            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                return ToolResult.failure(definition().name(), correlationId, "WORKSPACE_PATH_NOT_FOUND");
            }
            if (Files.isSymbolicLink(target)) {
                return ToolResult.failure(definition().name(), correlationId, "WORKSPACE_SYMLINK_FORBIDDEN");
            }
            if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                return ToolResult.failure(definition().name(), correlationId, "WORKSPACE_NOT_A_FILE");
            }

            byte[] bytes = Files.readAllBytes(target);
            if (containsBinaryMarker(bytes)) {
                return ToolResult.failure(definition().name(), correlationId, "WORKSPACE_BINARY_FILE");
            }
            String content = decodeUtf8(bytes);
            boolean truncated = bytes.length > maxBytes;
            if (truncated) {
                content = decodeUtf8(java.util.Arrays.copyOf(bytes, (int) maxBytes))
                        + "\n[MX: conteúdo truncado por limite de " + maxBytes + " bytes]";
            }
            return ToolResult.success(definition().name(), correlationId, Map.of(
                    "path", root.relativize(target).toString().replace('\\', '/'),
                    "content", content,
                    "bytes", bytes.length,
                    "truncated", truncated
            ));
        } catch (IOException | IllegalArgumentException exception) {
            return ToolResult.failure(definition().name(), correlationId, "WORKSPACE_READ_FAILED");
        }
    }

    private boolean containsBinaryMarker(byte[] bytes) {
        int inspected = (int) Math.min(bytes.length, maxBytes);
        for (int index = 0; index < inspected; index++) {
            if (bytes[index] == 0) {
                return true;
            }
        }
        return false;
    }

    private String decodeUtf8(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(bytes))
                .toString();
    }
}

