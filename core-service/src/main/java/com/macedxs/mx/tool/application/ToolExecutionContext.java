package com.macedxs.mx.tool.application;

import com.macedxs.mx.agent.application.AutonomyLevel;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public record ToolExecutionContext(
        UUID userId,
        UUID correlationId,
        String skillName,
        AutonomyLevel grantedAutonomy,
        Set<String> allowedTools
) {

    public ToolExecutionContext {
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        if (grantedAutonomy == null) {
            throw new IllegalArgumentException("Granted autonomy is required");
        }
        allowedTools = normalizeAllowedTools(allowedTools);
    }

    public ToolExecutionContext(
            UUID userId,
            UUID correlationId,
            String skillName,
            AutonomyLevel grantedAutonomy
    ) {
        this(userId, correlationId, skillName, grantedAutonomy, null);
    }

    private static Set<String> normalizeAllowedTools(Set<String> tools) {
        if (tools == null) {
            return null;
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String tool : tools) {
            if (tool != null && !tool.isBlank()) {
                normalized.add(tool.trim().toLowerCase(Locale.ROOT));
            }
        }
        return Set.copyOf(normalized);
    }
}
