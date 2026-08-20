package com.macedxs.mx.tool.application;

import com.macedxs.mx.agent.application.AutonomyLevel;

import java.time.Duration;
import java.util.Set;

public record ToolDefinition(
        String name,
        String version,
        String description,
        ToolEffect effect,
        AutonomyLevel minimumAutonomy,
        Duration timeout,
        Set<String> requiredArguments
) {

    public ToolDefinition {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tool name is required");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("Tool version is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Tool description is required");
        }
        if (effect == null) {
            throw new IllegalArgumentException("Tool effect is required");
        }
        if (minimumAutonomy == null) {
            throw new IllegalArgumentException("Tool minimum autonomy is required");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Tool timeout must be positive");
        }
        requiredArguments = requiredArguments == null ? Set.of() : Set.copyOf(requiredArguments);
    }
}
