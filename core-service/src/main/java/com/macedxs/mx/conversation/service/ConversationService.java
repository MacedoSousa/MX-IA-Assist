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
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }

        ConversationEntity conversation = new ConversationEntity();
        conversation.setUser(user);
        conversation.setTitle(title == null || title.isBlank() ? "Nova conversa" : title.trim());
        conversation.setTopic("geral");
        conversation.setLanguage("pt-BR");
        conversation.setLastMessageAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Optional<ConversationEntity> findById(UUID conversationId) {
        return conversationRepository.findById(conversationId);
    }

    @Transactional(readOnly = true)
    public List<ConversationEntity> findByUser(UUID userId) {
        return conversationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<ConversationEntity> findPageByUser(
            UUID userId,
            String topic,
            String query,
            Pageable pageable
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        String normalizedTopic = normalizeFilter(topic);
        String normalizedQuery = normalizeFilter(query);
        if (normalizedTopic.isBlank() && normalizedQuery.isBlank()) {
            return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId, pageable);
        }
        if (normalizedQuery.isBlank()) {
            return conversationRepository.findByUserIdAndTopicIgnoreCaseOrderByLastMessageAtDesc(
                    userId,
                    normalizedTopic,
                    pageable
            );
        }
        if (normalizedTopic.isBlank()) {
            return conversationRepository
                    .findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndSummaryContainingIgnoreCaseOrderByLastMessageAtDesc(
                            userId,
                            normalizedQuery,
                            userId,
                            normalizedQuery,
                            pageable
                    );
        }
        return conversationRepository
                .findByUserIdAndTopicIgnoreCaseAndTitleContainingIgnoreCaseOrUserIdAndTopicIgnoreCaseAndSummaryContainingIgnoreCaseOrderByLastMessageAtDesc(
                        userId,
                        normalizedTopic,
                        normalizedQuery,
                        userId,
                        normalizedTopic,
                        normalizedQuery,
                        pageable
                );
    }

    @Transactional
    public ConversationEntity updateMetadata(UUID conversationId, String prompt) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
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
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        return ollamaService.generateText(prompt);
    }

    private boolean isDefaultTitle(String title) {
        return title == null || title.isBlank() || title.equalsIgnoreCase("Nova conversa");
    }

    private String buildTitle(String prompt) {
        String compact = prompt.trim().replaceAll("\\s+", " ");
        if (compact.length() <= 80) {
            return compact;
        }
        return compact.substring(0, 77) + "...";
    }

    private String normalizeFilter(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
