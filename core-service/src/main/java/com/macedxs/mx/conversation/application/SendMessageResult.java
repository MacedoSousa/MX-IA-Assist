package com.macedxs.mx.conversation.application;

import java.util.UUID;

public record SendMessageResult(
        UUID conversationId,
        UUID userMessageId,
        UUID assistantMessageId,
        String answer,
        String model,
        long durationMs,
        UUID correlationId,
        String skillName,
        UUID runId
) {
    public SendMessageResult(
            UUID conversationId,
            UUID userMessageId,
            UUID assistantMessageId,
            String answer,
            String model,
            long durationMs
    ) {
        this(conversationId, userMessageId, assistantMessageId, answer, model, durationMs, null, null, null);
    }

    public SendMessageResult(
            UUID conversationId,
            UUID userMessageId,
            UUID assistantMessageId,
            String answer,
            String model,
            long durationMs,
            UUID correlationId,
            String skillName
    ) {
        this(conversationId, userMessageId, assistantMessageId, answer, model, durationMs, correlationId, skillName, null);
    }
}
