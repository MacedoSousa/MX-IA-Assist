package com.macedxs.mx.api.v1.conversation;

import com.macedxs.mx.conversation.application.SendMessageResult;

public final class ChatV1ResponseMapper {

    private ChatV1ResponseMapper() {
    }

    public static ChatV1Response completed(SendMessageResult result) {
        if (result == null) {
            throw new IllegalArgumentException("Conversation result is required");
        }

        return new ChatV1Response(
                "COMPLETED",
                result.correlationId(),
                result.conversationId(),
                result.userMessageId(),
                result.assistantMessageId(),
                result.skillName(),
                result.answer(),
                result.model(),
                result.durationMs(),
                result.runId()
        );
    }
}
