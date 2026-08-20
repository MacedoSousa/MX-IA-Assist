package com.macedxs.mx.conversation.service;

import com.macedxs.mx.ai.service.OllamaService;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.repository.ConversationRepository;
import com.macedxs.mx.identity.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final OllamaService ollamaService;

    public ConversationService(ConversationRepository conversationRepository, OllamaService ollamaService) {
        this.conversationRepository = conversationRepository;
        this.ollamaService = ollamaService;
    }

    @Transactional
    public ConversationEntity createConversation(UserEntity user, String title) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }

        ConversationEntity conversation = new ConversationEntity();
        conversation.setUser(user);
        conversation.setTitle(title == null || title.isBlank() ? "Nova conversa" : title);
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

    public String ask(UserEntity user, String prompt) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        return ollamaService.generateText(prompt);
    }
}
