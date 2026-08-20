package com.macedxs.mx.agent.application;

import java.util.Map;
import java.util.UUID;

public record SkillResult(
        String skillName,
        String answer,
        boolean complete,
        UUID correlationId,
        Map<String, Object> metadata
) {

    public SkillResult {
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Skill answer is required");
        }
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static SkillResult completed(String skillName, String answer, UUID correlationId) {
        return new SkillResult(skillName, answer, true, correlationId, Map.of());
    }
}
