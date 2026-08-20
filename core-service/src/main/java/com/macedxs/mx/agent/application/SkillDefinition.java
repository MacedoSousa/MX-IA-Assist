package com.macedxs.mx.agent.application;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record SkillDefinition(
        String name,
        String version,
        String description,
        Set<String> triggers,
        Set<String> allowedTools,
        AutonomyLevel maximumAutonomy,
        Duration timeout
) {

    public SkillDefinition {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("Skill version is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Skill description is required");
        }
        if (maximumAutonomy == null) {
            throw new IllegalArgumentException("Skill autonomy is required");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Skill timeout must be positive");
        }

        triggers = triggers == null ? Set.of() : triggers.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT).trim())
                .collect(Collectors.toUnmodifiableSet());
        allowedTools = allowedTools == null ? Set.of() : Set.copyOf(allowedTools);
    }
}
