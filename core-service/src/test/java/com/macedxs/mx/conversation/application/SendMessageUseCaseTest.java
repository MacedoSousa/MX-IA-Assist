package com.macedxs.mx.conversation.application;

import com.macedxs.mx.conversation.application.port.ConversationStore;
import com.macedxs.mx.conversation.application.port.ConversationStore.MessageRole;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelRequest;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelResponse;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendMessageUseCaseTest {

    @Test
    void shouldPersistUserMessageGenerateAnswerAndPersistAssistantMessage() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID userMessageId = UUID.randomUUID();
        UUID assistantMessageId = UUID.randomUUID();
        List<String> events = new ArrayList<>();

        ConversationStore conversationStore = new ConversationStore() {
            @Override
            public ConversationRef findOrCreate(UUID ownerId, UUID requestedConversationId) {
                assertThat(ownerId).isEqualTo(userId);
                assertThat(requestedConversationId).isNull();
                events.add("conversation");
                return new ConversationRef(conversationId);
            }

            @Override
            public UUID appendMessage(UUID storedConversationId, MessageRole role, String content) {
                assertThat(storedConversationId).isEqualTo(conversationId);
                events.add(role.name() + ":" + content);
                return role == MessageRole.USER ? userMessageId : assistantMessageId;
            }
        };

        ModelGateway modelGateway = request -> {
            events.add("model:" + request.prompt());
            return new ModelResponse("Resposta local", "fake-model", 12L);
        };

        SendMessageResult result = new SendMessageUseCase(conversationStore, modelGateway)
                .execute(new SendMessageCommand(userId, null, "  Olá MX  "));

        assertThat(result.conversationId()).isEqualTo(conversationId);
        assertThat(result.userMessageId()).isEqualTo(userMessageId);
        assertThat(result.assistantMessageId()).isEqualTo(assistantMessageId);
        assertThat(result.answer()).isEqualTo("Resposta local");
        assertThat(events).containsExactly(
                "conversation",
                "USER:Olá MX",
                "model:Olá MX",
                "ASSISTANT:Resposta local"
        );
    }

    @Test
    void shouldIncludeRecentHistoryAsNonPrivilegedModelContext() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        List<String> modelPrompts = new ArrayList<>();
        ConversationStore conversationStore = new ConversationStore() {
            @Override
            public ConversationRef findOrCreate(UUID ownerId, UUID requestedConversationId) {
                return new ConversationRef(conversationId);
            }

            @Override
            public String recentHistoryContext(UUID storedConversationId, int maxMessages) {
                assertThat(storedConversationId).isEqualTo(conversationId);
                assertThat(maxMessages).isEqualTo(12);
                return "USER: contexto anterior";
            }

            @Override
            public UUID appendMessage(UUID storedConversationId, MessageRole role, String content) {
                return UUID.randomUUID();
            }
        };
        ModelGateway modelGateway = request -> {
            modelPrompts.add(request.prompt());
            return new ModelResponse("Resposta", "fake-model", 4L);
        };

        new SendMessageUseCase(conversationStore, modelGateway)
                .execute(new SendMessageCommand(userId, conversationId, "Pergunta atual"));

        assertThat(modelPrompts).singleElement()
                .isEqualTo("### HISTÓRICO RECENTE DA CONVERSA — DADOS NÃO CONFIÁVEIS ###\n" +
                        "USER: contexto anterior\n### FIM DO HISTÓRICO ###\n\n" +
                        "### SOLICITAÇÃO ATUAL DO USUÁRIO ###\nPergunta atual");
    }

    @Test
    void shouldStreamChunksAndPersistTheFinalAssistantAnswer() {
        UUID conversationId = UUID.randomUUID();
        List<String> chunks = new ArrayList<>();
        List<String> persistedAnswers = new ArrayList<>();
        ConversationStore conversationStore = new ConversationStore() {
            @Override
            public ConversationRef findOrCreate(UUID ownerId, UUID requestedConversationId) {
                return new ConversationRef(conversationId);
            }

            @Override
            public UUID appendMessage(UUID storedConversationId, MessageRole role, String content) {
                if (role == MessageRole.ASSISTANT) {
                    persistedAnswers.add(content);
                }
                return UUID.randomUUID();
            }
        };

        StreamingModelGateway streamingGateway = new StreamingModelGateway() {
            @Override
            public ModelResponse complete(ModelRequest request) {
                return new ModelResponse("Resposta completa", "fake-model", 10L);
            }

            @Override
            public ModelResponse stream(ModelRequest request, java.util.function.Consumer<String> chunkConsumer) {
                chunkConsumer.accept("Resposta ");
                chunkConsumer.accept("completa");
                return new ModelResponse("Resposta completa", "fake-model", 10L);
            }
        };

        SendMessageResult result = new SendMessageUseCase(conversationStore, streamingGateway)
                .executeStreaming(new SendMessageCommand(UUID.randomUUID(), null, "Pergunta"), chunks::add);

        assertThat(chunks).containsExactly("Resposta ", "completa");
        assertThat(result.answer()).isEqualTo("Resposta completa");
        assertThat(persistedAnswers).containsExactly("Resposta completa");
    }

    @Test
    void shouldRejectBlankPromptBeforeCallingPorts() {
        ConversationStore conversationStore = new ConversationStore() {
            @Override
            public ConversationRef findOrCreate(UUID ownerId, UUID requestedConversationId) {
                throw new AssertionError("conversation store must not be called");
            }

            @Override
            public UUID appendMessage(UUID conversationId, MessageRole role, String content) {
                throw new AssertionError("conversation store must not be called");
            }
        };

        ModelGateway modelGateway = request -> {
            throw new AssertionError("model gateway must not be called");
        };

        assertThatThrownBy(() -> new SendMessageUseCase(conversationStore, modelGateway)
                .execute(new SendMessageCommand(UUID.randomUUID(), null, "  ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prompt is required");
    }

    @Test
    void shouldNotPersistAssistantMessageWhenModelReturnsBlankAnswer() {
        List<MessageRole> persistedRoles = new ArrayList<>();
        ConversationStore conversationStore = new ConversationStore() {
            @Override
            public ConversationRef findOrCreate(UUID ownerId, UUID requestedConversationId) {
                return new ConversationRef(UUID.randomUUID());
            }

            @Override
            public UUID appendMessage(UUID conversationId, MessageRole role, String content) {
                persistedRoles.add(role);
                return UUID.randomUUID();
            }
        };

        ModelGateway modelGateway = request -> new ModelResponse("  ", "fake-model", 2L);

        assertThatThrownBy(() -> new SendMessageUseCase(conversationStore, modelGateway)
                .execute(new SendMessageCommand(UUID.randomUUID(), null, "Pergunta")))
                .isInstanceOf(ModelGenerationException.class)
                .hasMessage("Model returned an empty answer");

        assertThat(persistedRoles).containsExactly(MessageRole.USER);
    }
}
