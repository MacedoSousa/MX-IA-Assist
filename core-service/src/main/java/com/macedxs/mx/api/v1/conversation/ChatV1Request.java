package com.macedxs.mx.api.v1.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatV1Request(
        UUID conversationId,
        @NotBlank(message = "Prompt cannot be blank")
        @Size(max = 12000, message = "Prompt cannot exceed 12000 characters")
        String prompt,
        @Size(max = 120, message = "Idempotency key cannot exceed 120 characters")
        String idempotencyKey
) {
}
