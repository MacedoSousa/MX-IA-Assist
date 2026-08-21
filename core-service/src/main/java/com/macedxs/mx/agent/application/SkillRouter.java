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
        return route(new SkillRequest(prompt));
    }

    public RouteDecision route(SkillRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Skill request is required");
        }

        String normalizedPrompt = normalize(request.prompt());
        if (normalizedPrompt.contains("[mx_document_output]")) {
            return new RouteDecision(
                    registry.getRequired("general"),
                    1.0d,
                    "Internal document rendering prompt routed to the central general skill",
                    false
            );
        }

        var mediaIntent = MediaIntentDetector.detect(request.prompt());
        if (mediaIntent.isPresent()) {
            return new RouteDecision(
                    registry.getRequired("media"),
                    1.0d,
                    "Explicit " + mediaIntent.get().name().toLowerCase(Locale.ROOT) + " generation request",
                    false
            );
        }

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
        if (!request.images().isEmpty()) {
            return new RouteDecision(
                    fallback,
                    1.0d,
                    "Vision attachment routed through the central general skill",
                    false
            );
        }
        return new RouteDecision(fallback, 0.0d, "No specialist trigger matched", false);
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }
}
