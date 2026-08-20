package com.macedxs.mx.conversation.repository;

import com.macedxs.mx.conversation.entity.ConversationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessageEntity, UUID> {
    List<ConversationMessageEntity> findByConversationIdOrderByCreatedAt(UUID conversationId);
}
