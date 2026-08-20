package com.macedxs.mx.conversation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationDTO(
        UUID id,
        String title,
        String summary,
        LocalDateTime createdAt
) {}
