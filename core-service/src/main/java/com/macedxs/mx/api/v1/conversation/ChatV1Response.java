package com.macedxs.mx.api.v1.conversation;

import java.util.UUID;

public record ChatV1Response(
        String status,
        UUID correlationId,
        UUID conversationId,
        UUID userMessageId,
        UUID assistantMessageId,
        String skillName,
        String answer,
        String model,
        long durationMs,
        UUID runId
) {
}
