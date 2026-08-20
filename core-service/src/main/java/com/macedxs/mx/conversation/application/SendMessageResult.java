package com.macedxs.mx.conversation.application;

import java.time.Instant;
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
        UUID runId,
        String status,
        UUID approvalRunId,
        String approvalNonce,
        Instant approvalExpiresAt
) {
    public SendMessageResult(
            UUID conversationId,
            UUID userMessageId,
            UUID assistantMessageId,
            String answer,
            String model,
            long durationMs
    ) {
        this(conversationId, userMessageId, assistantMessageId, answer, model, durationMs,
                null, null, null, "COMPLETED", null, null, null);
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
        this(conversationId, userMessageId, assistantMessageId, answer, model, durationMs,
                correlationId, skillName, null, "COMPLETED", null, null, null);
    }

    public SendMessageResult(
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
        this(conversationId, userMessageId, assistantMessageId, answer, model, durationMs,
                correlationId, skillName, runId, "COMPLETED", null, null, null);
    }

    public SendMessageResult {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        if (approvalRunId == null && (approvalNonce != null || approvalExpiresAt != null)) {
            throw new IllegalArgumentException("Approval metadata requires an approval run");
        }
    }
}
