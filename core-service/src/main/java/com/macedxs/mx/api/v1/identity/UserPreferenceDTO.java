package com.macedxs.mx.api.v1.identity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserPreferenceDTO(
        UUID userId,
        String learningStyle,
        String knowledgeLevel,
        Set<String> topicsOfInterest,
        String preferredLanguage,
        String communicationStyle,
        Set<String> vocabularyHints,
        LocalDateTime lastActiveAt
) {

    public UserPreferenceDTO(
            UUID userId,
            String learningStyle,
            String knowledgeLevel,
            Set<String> topicsOfInterest,
            LocalDateTime lastActiveAt
    ) {
        this(userId, learningStyle, knowledgeLevel, topicsOfInterest, "pt-BR", "natural", Set.of(), lastActiveAt);
    }
}
