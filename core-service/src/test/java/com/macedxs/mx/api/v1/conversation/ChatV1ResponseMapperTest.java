package com.macedxs.mx.api.v1.conversation;

import com.macedxs.mx.conversation.application.SendMessageResult;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatV1ResponseMapperTest {

    @Test
    void shouldExposeConversationMessageCorrelationAndSkillIdentifiers() {
        UUID conversationId = UUID.randomUUID();
        UUID userMessageId = UUID.randomUUID();
        UUID assistantMessageId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();

        SendMessageResult result = new SendMessageResult(
                conversationId,
                userMessageId,
                assistantMessageId,
                "Resposta central",
                "mx-development",
                42L,
                correlationId,
                "development",
                runId
        );

        ChatV1Response response = ChatV1ResponseMapper.completed(result);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.conversationId()).isEqualTo(conversationId);
        assertThat(response.userMessageId()).isEqualTo(userMessageId);
        assertThat(response.assistantMessageId()).isEqualTo(assistantMessageId);
        assertThat(response.correlationId()).isEqualTo(correlationId);
        assertThat(response.skillName()).isEqualTo("development");
        assertThat(response.answer()).isEqualTo("Resposta central");
        assertThat(response.model()).isEqualTo("mx-development");
        assertThat(response.durationMs()).isEqualTo(42L);
        assertThat(response.runId()).isEqualTo(runId);
    }
}
