package com.macedxs.mx.conversation.infrastructure.persistence;

import com.macedxs.mx.conversation.application.port.ConversationStore;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.entity.ConversationMessageEntity;
import com.macedxs.mx.conversation.service.ConversationMemoryService;
import com.macedxs.mx.conversation.service.ConversationService;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JpaConversationStore implements ConversationStore {

    private final ConversationService conversationService;
    private final ConversationMemoryService memoryService;
    private final UserRepository userRepository;

    public JpaConversationStore(
            ConversationService conversationService,
            ConversationMemoryService memoryService,
            UserRepository userRepository
    ) {
        this.conversationService = conversationService;
        this.memoryService = memoryService;
        this.userRepository = userRepository;
    }

    @Override
    public ConversationRef findOrCreate(UUID ownerId, UUID requestedConversationId) {
        requireOwner(ownerId);

        if (requestedConversationId == null) {
            UserEntity owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            ConversationEntity created = conversationService.createConversation(owner, "Nova conversa");
            return new ConversationRef(created.getId());
        }

        ConversationEntity existing = conversationService.findActiveById(requestedConversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (existing.getUser() == null || !ownerId.equals(existing.getUser().getId())) {
            throw new IllegalArgumentException("Conversation not found");
        }

        return new ConversationRef(existing.getId());
    }

    @Override
    public String recentHistoryContext(UUID conversationId, int maxMessages) {
        return memoryService.getRecentFormattedHistory(conversationId, maxMessages);
    }

    @Override
    public void updateMetadata(UUID conversationId, String prompt) {
        conversationService.updateMetadata(conversationId, prompt);
    }

    @Override
    public UUID appendMessage(UUID conversationId, MessageRole role, String content) {
        ConversationEntity conversation = conversationService.findActiveById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        ConversationMessageEntity.MessageRole persistenceRole = ConversationMessageEntity.MessageRole
                .valueOf(role.name());

        return memoryService.addMessage(conversation, persistenceRole, content).getId();
    }

    private void requireOwner(UUID ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner is required");
        }
    }
}
