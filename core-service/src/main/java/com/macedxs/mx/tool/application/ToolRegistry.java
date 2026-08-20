package com.macedxs.mx.tool.application;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public void register(Tool tool) {
        if (tool == null || tool.definition() == null) {
            throw new IllegalArgumentException("Tool is required");
        }

        String key = normalize(tool.definition().name());
        if (tools.containsKey(key)) {
            throw new IllegalArgumentException("Tool already registered: " + tool.definition().name());
        }
        tools.put(key, tool);
    }

    public Tool getRequired(String name) {
        Tool tool = tools.get(normalize(name));
        if (tool == null) {
            throw new IllegalArgumentException("Tool not registered: " + name);
        }
        return tool;
    }

    public Collection<Tool> all() {
        return Collections.unmodifiableCollection(tools.values());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tool name is required");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
