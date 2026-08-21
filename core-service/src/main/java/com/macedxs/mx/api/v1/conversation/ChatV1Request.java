package com.macedxs.mx.api.v1.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ChatV1Request(
        UUID conversationId,
        @NotBlank(message = "Prompt cannot be blank")
        @Size(max = 12000, message = "Prompt cannot exceed 12000 characters")
        String prompt,
        @Size(max = 120, message = "Idempotency key cannot exceed 120 characters")
        String idempotencyKey,
        @Size(max = 8, message = "At most 8 attachments can be sent")
        List<UUID> attachmentIds
) {
    public ChatV1Request(UUID conversationId, String prompt, String idempotencyKey) {
        this(conversationId, prompt, idempotencyKey, List.of());
    }

    public ChatV1Request {
        attachmentIds = attachmentIds == null ? List.of() : List.copyOf(attachmentIds);
    }
}
