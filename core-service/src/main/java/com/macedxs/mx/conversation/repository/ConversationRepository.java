package com.macedxs.mx.conversation.repository;

import com.macedxs.mx.conversation.entity.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @Query("select c from ConversationEntity c where c.user.id = :userId and c.deletedAt is null order by c.lastMessageAt desc")
    List<ConversationEntity> findActiveByUserId(@Param("userId") UUID userId);

    @Query("select c from ConversationEntity c where c.id = :conversationId and c.user.id = :userId and c.deletedAt is null")
    Optional<ConversationEntity> findActiveByIdAndUserId(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );

    @Query("select c from ConversationEntity c where c.id = :conversationId and c.user.id = :userId")
    Optional<ConversationEntity> findOwnedById(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );

    @Query("select c from ConversationEntity c where c.user.id = :userId and c.deletedAt is not null order by c.deletedAt desc")
    List<ConversationEntity> findDeletedByUserId(@Param("userId") UUID userId);

    @Query("select c from ConversationEntity c where c.user.id = :userId and c.deletedAt is null order by c.lastMessageAt desc")
    Page<ConversationEntity> findActiveByUserIdOrderByLastMessageAtDesc(UUID userId, Pageable pageable);

    @Query("select c from ConversationEntity c where c.user.id = :userId and c.deletedAt is null and lower(c.topic) = lower(:topic) order by c.lastMessageAt desc")
    Page<ConversationEntity> findActiveByUserIdAndTopicOrderByLastMessageAtDesc(
            UUID userId,
            String topic,
            Pageable pageable
    );

    @Query("select c from ConversationEntity c where c.user.id = :userId and c.deletedAt is null and (lower(c.title) like lower(concat('%', :query, '%')) or lower(coalesce(c.summary, '')) like lower(concat('%', :query, '%'))) order by c.lastMessageAt desc")
    Page<ConversationEntity> findActiveByUserIdAndQueryOrderByLastMessageAtDesc(
            UUID userId,
            String query,
            Pageable pageable
    );

    @Query("select c from ConversationEntity c where c.user.id = :userId and c.deletedAt is null and lower(c.topic) = lower(:topic) and (lower(c.title) like lower(concat('%', :query, '%')) or lower(coalesce(c.summary, '')) like lower(concat('%', :query, '%'))) order by c.lastMessageAt desc")
    Page<ConversationEntity> findActiveByUserIdAndTopicAndQueryOrderByLastMessageAtDesc(
            UUID userId,
            String topic,
            String query,
            Pageable pageable
    );
}
