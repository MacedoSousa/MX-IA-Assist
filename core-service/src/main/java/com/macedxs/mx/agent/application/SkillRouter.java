package com.macedxs.mx.agent.application;

import java.text.Normalizer;
import java.util.Locale;

public class SkillRouter {

    private final SkillRegistry registry;

    public SkillRouter(SkillRegistry registry) {
        if (registry == null) {
            throw new IllegalArgumentException("Skill registry is required");
        }
        this.registry = registry;
    }

    public RouteDecision route(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }

        String normalizedPrompt = normalize(prompt);
        Skill selected = null;
        int selectedMatches = 0;
        double selectedScore = 0.0d;

        for (Skill skill : registry.all()) {
            int matches = (int) skill.definition().triggers().stream()
                    .filter(trigger -> normalizedPrompt.contains(normalize(trigger)))
                    .count();
            if (matches == 0) {
                continue;
            }

            double score = matches / (double) skill.definition().triggers().size();
            if (matches >= 2) {
                score = Math.min(1.0d, score + 0.25d);
            }

            if (selected == null || score > selectedScore) {
                selected = skill;
                selectedMatches = matches;
                selectedScore = score;
            }
        }

        if (selected != null) {
            return new RouteDecision(
                    selected,
                    selectedScore,
                    "Matched " + selectedMatches + " skill trigger(s)",
                    selectedScore < 0.5d
            );
        }

        Skill fallback = registry.getRequired("general");
        return new RouteDecision(fallback, 0.0d, "No specialist trigger matched", false);
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }
}
