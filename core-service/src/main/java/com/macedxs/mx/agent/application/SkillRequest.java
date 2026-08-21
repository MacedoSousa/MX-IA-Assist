package com.macedxs.mx.agent.application;

import com.macedxs.mx.conversation.application.port.ModelGateway.ModelImage;

import java.util.List;

public record SkillRequest(String prompt, List<ModelImage> images) {

    public SkillRequest {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Skill prompt is required");
        }
        prompt = prompt.trim();
        images = images == null ? List.of() : List.copyOf(images);
    }

    public SkillRequest(String prompt) {
        this(prompt, List.of());
    }
}
