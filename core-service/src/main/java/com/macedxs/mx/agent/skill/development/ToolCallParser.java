package com.macedxs.mx.agent.skill.development;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

/** Parses only the explicit, fenced tool-call protocol emitted by a skill model. */
public final class ToolCallParser {

    private static final String START = "[MX_TOOL_CALL]";
    private static final String END = "[/MX_TOOL_CALL]";

    private final ObjectMapper objectMapper;

    public ToolCallParser(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new IllegalArgumentException("Object mapper is required");
        }
        this.objectMapper = objectMapper;
    }

    public Optional<ParsedToolCall> parse(String answer) {
        if (answer == null || answer.isBlank()) {
            return Optional.empty();
        }

        String normalized = answer.trim();
        if (!normalized.startsWith(START) || !normalized.endsWith(END)) {
            return Optional.empty();
        }

        String json = normalized.substring(START.length(), normalized.length() - END.length()).trim();
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null || !root.isObject()) {
                return Optional.empty();
            }
            JsonNode toolName = root.get("toolName");
            JsonNode arguments = root.get("arguments");
            if (toolName == null || !toolName.isTextual() || toolName.asText().isBlank()
                    || arguments == null || !arguments.isObject()) {
                return Optional.empty();
            }
            Map<String, Object> parsedArguments = objectMapper.convertValue(arguments, Map.class);
            return Optional.of(new ParsedToolCall(toolName.asText().trim(), parsedArguments));
        } catch (Exception invalidJson) {
            return Optional.empty();
        }
    }

    public record ParsedToolCall(String toolName, Map<String, Object> arguments) {
        public ParsedToolCall {
            if (toolName == null || toolName.isBlank()) {
                throw new IllegalArgumentException("Tool name is required");
            }
            arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        }
    }
}
