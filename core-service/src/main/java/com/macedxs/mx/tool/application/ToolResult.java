package com.macedxs.mx.tool.application;

import java.util.Map;
import java.util.UUID;

public record ToolResult(
        String toolName,
        UUID correlationId,
        boolean success,
        Map<String, Object> output,
        String errorCode
) {

    public ToolResult {
        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException("Tool name is required");
        }
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        output = output == null ? Map.of() : Map.copyOf(output);
    }

    public static ToolResult success(String toolName, UUID correlationId, Map<String, Object> output) {
        return new ToolResult(toolName, correlationId, true, output, null);
    }

    public static ToolResult failure(String toolName, UUID correlationId, String errorCode) {
        return new ToolResult(toolName, correlationId, false, Map.of(), errorCode);
    }
}
