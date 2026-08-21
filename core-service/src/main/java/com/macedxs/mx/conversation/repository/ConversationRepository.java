package com.macedxs.mx.conversation.repository;

import com.macedxs.mx.conversation.entity.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    java.util.List<ConversationEntity> findByUserId(UUID userId);

    Page<ConversationEntity> findByUserIdOrderByLastMessageAtDesc(UUID userId, Pageable pageable);

    Page<ConversationEntity> findByUserIdAndTopicIgnoreCaseOrderByLastMessageAtDesc(
            UUID userId,
            String topic,
            Pageable pageable
    );

    Page<ConversationEntity> findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndSummaryContainingIgnoreCaseOrderByLastMessageAtDesc(
            UUID userId,
            String title,
            UUID sameUserId,
            String summary,
            Pageable pageable
    );

    Page<ConversationEntity> findByUserIdAndTopicIgnoreCaseAndTitleContainingIgnoreCaseOrUserIdAndTopicIgnoreCaseAndSummaryContainingIgnoreCaseOrderByLastMessageAtDesc(
            UUID userId,
            String topic,
            String title,
            UUID sameUserId,
            String sameTopic,
            String summary,
            Pageable pageable
    );
}
