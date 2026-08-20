package com.macedxs.mx.tool.application;

import java.util.Map;

public record ToolRequest(String toolName, Map<String, Object> arguments) {

    public ToolRequest {
        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException("Tool name is required");
        }
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}
