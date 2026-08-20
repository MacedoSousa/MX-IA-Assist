package com.macedxs.mx.conversation.application;

import java.util.UUID;

public record SendMessageCommand(UUID ownerId, UUID conversationId, String prompt, String idempotencyKey) {

    public SendMessageCommand(UUID ownerId, UUID conversationId, String prompt) {
        this(ownerId, conversationId, prompt, null);
    }

    public SendMessageCommand {
        if (idempotencyKey != null && idempotencyKey.isBlank()) {
            idempotencyKey = null;
        } else if (idempotencyKey != null) {
            idempotencyKey = idempotencyKey.trim();
        }
    }
}
