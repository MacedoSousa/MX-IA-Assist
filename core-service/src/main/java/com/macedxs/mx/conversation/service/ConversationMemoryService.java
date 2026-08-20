package com.macedxs.mx.conversation.service;

import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.entity.ConversationMessageEntity;
import com.macedxs.mx.conversation.repository.ConversationMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationMemoryService {

    private final ConversationMessageRepository messageRepository;

    public ConversationMemoryService(ConversationMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ConversationMessageEntity addMessage(ConversationEntity conversation,
                                               ConversationMessageEntity.MessageRole role,
                                               String content) {
        if (conversation == null || conversation.getId() == null) {
            throw new IllegalArgumentException("Conversation is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be blank");
        }

        ConversationMessageEntity message = new ConversationMessageEntity();
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ConversationMessageEntity> getHistory(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAt(conversationId);
    }

    @Transactional(readOnly = true)
    public String getFormattedHistory(UUID conversationId) {
        List<ConversationMessageEntity> messages = getHistory(conversationId);
        StringBuilder history = new StringBuilder();

        for (ConversationMessageEntity msg : messages) {
            history.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }

        return history.toString();
    }
}
