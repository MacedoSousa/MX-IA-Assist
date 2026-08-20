package com.macedxs.mx.agent.application;

public record RouteDecision(
        Skill skill,
        double confidence,
        String reason,
        boolean requiresClarification
) {

    public RouteDecision {
        if (skill == null) {
            throw new IllegalArgumentException("Selected skill is required");
        }
        if (confidence < 0.0d || confidence > 1.0d) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Route reason is required");
        }
    }
}
