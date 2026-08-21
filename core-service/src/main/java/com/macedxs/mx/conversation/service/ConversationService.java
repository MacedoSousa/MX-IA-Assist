package com.macedxs.mx.conversation.service;

import com.macedxs.mx.ai.service.OllamaService;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.repository.ConversationRepository;
import com.macedxs.mx.identity.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {

    private static final int MAX_TITLE_LENGTH = 200;

    private final ConversationRepository conversationRepository;
    private final OllamaService ollamaService;
    private final ConversationTopicClassifier topicClassifier;

    public ConversationService(ConversationRepository conversationRepository, OllamaService ollamaService) {
        this(conversationRepository, ollamaService, new ConversationTopicClassifier());
    }

    @Autowired
    public ConversationService(
            ConversationRepository conversationRepository,
            OllamaService ollamaService,
            ConversationTopicClassifier topicClassifier
    ) {
        this.conversationRepository = conversationRepository;
        this.ollamaService = ollamaService;
        this.topicClassifier = topicClassifier;
    }

    @Transactional
    public ConversationEntity createConversation(UserEntity user, String title) {
        requireUser(user);
        ConversationEntity conversation = new ConversationEntity();
        conversation.setUser(user);
        conversation.setTitle(normalizeTitle(title));
        conversation.setTopic("geral");
        conversation.setLanguage("pt-BR");
        conversation.setLastMessageAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Optional<ConversationEntity> findById(UUID conversationId) {
        requireConversationId(conversationId);
        return conversationRepository.findById(conversationId);
    }

    @Transactional(readOnly = true)
    public Optional<ConversationEntity> findActiveById(UUID conversationId) {
        requireConversationId(conversationId);
        return conversationRepository.findById(conversationId)
                .filter(conversation -> conversation.getDeletedAt() == null);
    }

    @Transactional(readOnly = true)
    public Optional<ConversationEntity> findOwnedActive(UUID userId, UUID conversationId) {
        requireUserId(userId);
        requireConversationId(conversationId);
        return conversationRepository.findActiveByIdAndUserId(conversationId, userId);
    }

    @Transactional(readOnly = true)
    public List<ConversationEntity> findByUser(UUID userId) {
        requireUserId(userId);
        return conversationRepository.findActiveByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<ConversationEntity> findDeletedByUser(UUID userId) {
        requireUserId(userId);
        return conversationRepository.findDeletedByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<ConversationEntity> findPageByUser(
            UUID userId,
            String topic,
            String query,
            Pageable pageable
    ) {
        requireUserId(userId);
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable is required");
        }
        String normalizedTopic = normalizeFilter(topic);
        String normalizedQuery = normalizeFilter(query);
        if (normalizedTopic.isBlank() && normalizedQuery.isBlank()) {
            return conversationRepository.findActiveByUserIdOrderByLastMessageAtDesc(userId, pageable);
        }
        if (normalizedQuery.isBlank()) {
            return conversationRepository.findActiveByUserIdAndTopicOrderByLastMessageAtDesc(
                    userId,
                    normalizedTopic,
                    pageable
            );
        }
        if (normalizedTopic.isBlank()) {
            return conversationRepository.findActiveByUserIdAndQueryOrderByLastMessageAtDesc(
                    userId,
                    normalizedQuery,
                    pageable
            );
        }
        return conversationRepository.findActiveByUserIdAndTopicAndQueryOrderByLastMessageAtDesc(
                userId,
                normalizedTopic,
                normalizedQuery,
                pageable
        );
    }

    @Transactional
    public ConversationEntity renameConversation(UUID userId, UUID conversationId, String title) {
        ConversationEntity conversation = ownedConversation(userId, conversationId, true);
        conversation.setTitle(normalizeTitle(title));
        return conversationRepository.save(conversation);
    }

    @Transactional
    public ConversationEntity archiveConversation(UUID userId, UUID conversationId) {
        ConversationEntity conversation = ownedConversation(userId, conversationId, true);
        if (conversation.getArchivedAt() == null) {
            conversation.setArchivedAt(LocalDateTime.now());
        }
        return conversationRepository.save(conversation);
    }

    @Transactional
    public ConversationEntity restoreConversation(UUID userId, UUID conversationId) {
        ConversationEntity conversation = ownedConversation(userId, conversationId, false);
        conversation.setDeletedAt(null);
        conversation.setArchivedAt(null);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public ConversationEntity softDeleteConversation(UUID userId, UUID conversationId) {
        ConversationEntity conversation = ownedConversation(userId, conversationId, true);
        conversation.setDeletedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    @Transactional
    public ConversationEntity updateMetadata(UUID conversationId, String prompt) {
        ConversationEntity conversation = findActiveById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        ConversationTopicClassifier.Classification classification = topicClassifier.classify(prompt);
        if (classification.topic() != null && !classification.topic().equals("geral")) {
            conversation.setTopic(classification.topic());
        }
        if (classification.language() != null && !classification.language().isBlank()) {
            conversation.setLanguage(classification.language());
        }
        conversation.setLastMessageAt(LocalDateTime.now());
        if (isDefaultTitle(conversation.getTitle()) && prompt != null && !prompt.isBlank()) {
            conversation.setTitle(buildTitle(prompt));
        }
        return conversationRepository.save(conversation);
    }

    public String ask(UserEntity user, String prompt) {
        requireUser(user);
        return ollamaService.generateText(prompt);
    }

    private ConversationEntity ownedConversation(UUID userId, UUID conversationId, boolean activeOnly) {
        requireUserId(userId);
        requireConversationId(conversationId);
        Optional<ConversationEntity> result = activeOnly
                ? conversationRepository.findActiveByIdAndUserId(conversationId, userId)
                : conversationRepository.findOwnedById(conversationId, userId);
        return result.orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
    }

    private String normalizeTitle(String title) {
        String normalized = title == null ? "" : title.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            return "Nova conversa";
        }
        return normalized.length() <= MAX_TITLE_LENGTH
                ? normalized
                : normalized.substring(0, MAX_TITLE_LENGTH - 3) + "...";
    }

    private boolean isDefaultTitle(String title) {
        return title == null || title.isBlank() || title.equalsIgnoreCase("Nova conversa");
    }

    private String buildTitle(String prompt) {
        return normalizeTitle(prompt);
    }

    private String normalizeFilter(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private void requireUser(UserEntity user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }
    }

    private void requireUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
    }

    private void requireConversationId(UUID conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation is required");
        }
    }
}
