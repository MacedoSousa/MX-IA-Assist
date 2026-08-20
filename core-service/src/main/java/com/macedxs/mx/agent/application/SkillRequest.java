package com.macedxs.mx.agent.application;

public record SkillRequest(String prompt) {

    public SkillRequest {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Skill prompt is required");
        }
        prompt = prompt.trim();
    }
}
