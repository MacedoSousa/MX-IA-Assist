package com.macedxs.mx.conversation.application;

import java.util.UUID;

public record SendMessageCommand(UUID ownerId, UUID conversationId, String prompt) {
}
