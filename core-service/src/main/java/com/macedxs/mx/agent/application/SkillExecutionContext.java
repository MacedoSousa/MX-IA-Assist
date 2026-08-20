package com.macedxs.mx.agent.application;

import java.util.UUID;

public record SkillExecutionContext(
        UUID userId,
        UUID correlationId,
        AutonomyLevel grantedAutonomy
) {

    public SkillExecutionContext {
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        if (grantedAutonomy == null) {
            throw new IllegalArgumentException("Granted autonomy is required");
        }
    }
}
