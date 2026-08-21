package com.macedxs.mx.conversation.dto;

import com.macedxs.mx.conversation.entity.ConversationEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationDTO(
        UUID id,
        String title,
        String summary,
        LocalDateTime createdAt,
        String topic,
        String language,
        LocalDateTime lastMessageAt
) {

    public ConversationDTO(UUID id, String title, String summary, LocalDateTime createdAt) {
        this(id, title, summary, createdAt, "geral", "pt-BR", createdAt);
    }

    public static ConversationDTO from(ConversationEntity conversation) {
        return new ConversationDTO(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getSummary(),
                conversation.getCreatedAt(),
                conversation.getTopic(),
                conversation.getLanguage(),
                conversation.getLastMessageAt()
        );
    }
}
