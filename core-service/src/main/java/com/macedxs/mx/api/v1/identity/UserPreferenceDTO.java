package com.macedxs.mx.api.v1.identity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserPreferenceDTO(
        UUID userId,
        String learningStyle,
        String knowledgeLevel,
        Set<String> topicsOfInterest,
        LocalDateTime lastActiveAt
) {
}
