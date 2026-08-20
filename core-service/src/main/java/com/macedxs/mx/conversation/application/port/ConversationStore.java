package com.macedxs.mx.conversation.application.port;

import java.util.UUID;

public interface ConversationStore {

    ConversationRef findOrCreate(UUID ownerId, UUID requestedConversationId);

    UUID appendMessage(UUID conversationId, MessageRole role, String content);

    record ConversationRef(UUID id) {
        public ConversationRef {
            if (id == null) {
                throw new IllegalArgumentException("Conversation id is required");
            }
        }
    }

    enum MessageRole {
        USER,
        ASSISTANT,
        SYSTEM
    }
}
