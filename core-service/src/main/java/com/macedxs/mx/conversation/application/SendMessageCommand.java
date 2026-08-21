package com.macedxs.mx.conversation.application;

import java.util.List;
import java.util.UUID;

public record SendMessageCommand(
        UUID ownerId,
        UUID conversationId,
        String prompt,
        String idempotencyKey,
        List<UUID> attachmentIds
) {

    public SendMessageCommand(UUID ownerId, UUID conversationId, String prompt, String idempotencyKey) {
        this(ownerId, conversationId, prompt, idempotencyKey, List.of());
    }

    public SendMessageCommand(UUID ownerId, UUID conversationId, String prompt) {
        this(ownerId, conversationId, prompt, null, List.of());
    }

    public SendMessageCommand {
        if (idempotencyKey != null && idempotencyKey.isBlank()) {
            idempotencyKey = null;
        } else if (idempotencyKey != null) {
            idempotencyKey = idempotencyKey.trim();
        }
        attachmentIds = attachmentIds == null ? List.of() : List.copyOf(attachmentIds);
    }
}
