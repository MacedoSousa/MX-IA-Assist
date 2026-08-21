package com.macedxs.mx.conversation.service;

import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.entity.ConversationMessageEntity;
import com.macedxs.mx.conversation.repository.ConversationMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationMemoryService {

    private static final int MAX_RECENT_MESSAGES = 50;

    private final ConversationMessageRepository messageRepository;

    public ConversationMemoryService(ConversationMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ConversationMessageEntity addMessage(
            ConversationEntity conversation,
            ConversationMessageEntity.MessageRole role,
            String content
    ) {
        if (conversation == null || conversation.getId() == null) {
            throw new IllegalArgumentException("Conversation is required");
        }
        if (role == null) {
            throw new IllegalArgumentException("Message role is required");
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
    public Page<ConversationMessageEntity> getHistory(UUID conversationId, org.springframework.data.domain.Pageable pageable) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation is required");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable is required");
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(conversationId, pageable);
    }

    @Transactional(readOnly = true)
    public String getRecentFormattedHistory(UUID conversationId, int maxMessages) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation is required");
        }
        if (maxMessages <= 0) {
            return "";
        }
        int boundedLimit = Math.min(maxMessages, MAX_RECENT_MESSAGES);
        Page<ConversationMessageEntity> page = messageRepository
                .findByConversationIdOrderByCreatedAtDescIdDesc(
                        conversationId,
                        PageRequest.of(0, boundedLimit)
                );
        List<ConversationMessageEntity> messages = new ArrayList<>(page.getContent());
        Collections.reverse(messages);
        return format(messages);
    }

    @Transactional(readOnly = true)
    public String getFormattedHistory(UUID conversationId) {
        return format(getHistory(conversationId));
    }

    private String format(List<ConversationMessageEntity> messages) {
        StringBuilder history = new StringBuilder();
        for (ConversationMessageEntity msg : messages) {
            history.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        return history.toString();
    }
}
