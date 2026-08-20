package com.macedxs.mx.tool.application;

import com.macedxs.mx.agent.application.AutonomyLevel;

import java.util.UUID;

public record ToolExecutionContext(
        UUID userId,
        UUID correlationId,
        String skillName,
        AutonomyLevel grantedAutonomy
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
    }
}
