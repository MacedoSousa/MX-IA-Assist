package com.macedxs.mx.core.application;

import java.util.UUID;

public record MxCoreResponse(
        UUID correlationId,
        String skillName,
        double confidence,
        boolean requiresClarification,
        String answer,
        UUID runId
) {

    public MxCoreResponse(
            UUID correlationId,
            String skillName,
            double confidence,
            boolean requiresClarification,
            String answer
    ) {
        this(correlationId, skillName, confidence, requiresClarification, answer, null);
    }

    public MxCoreResponse {
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer is required");
        }
    }
}
