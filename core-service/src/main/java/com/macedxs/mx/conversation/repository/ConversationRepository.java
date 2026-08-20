package com.macedxs.mx.conversation.repository;

import com.macedxs.mx.conversation.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {
    List<ConversationEntity> findByUserId(UUID userId);
}
