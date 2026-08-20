package com.macedxs.mx.conversation.application;

import com.macedxs.mx.conversation.application.port.ConversationStore;
import com.macedxs.mx.conversation.application.port.ConversationStore.MessageRole;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelRequest;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelResponse;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class SendMessageUseCase {

    private final ConversationStore conversationStore;
    private final ModelGateway modelGateway;

    public SendMessageUseCase(ConversationStore conversationStore, ModelGateway modelGateway) {
        this.conversationStore = conversationStore;
        this.modelGateway = modelGateway;
    }

    public SendMessageResult execute(SendMessageCommand command) {
        validate(command);

        String prompt = command.prompt().trim();
        ConversationStore.ConversationRef conversation = conversationStore
                .findOrCreate(command.ownerId(), command.conversationId());

        UUID userMessageId = conversationStore.appendMessage(
                conversation.id(),
                MessageRole.USER,
                prompt
        );

        ModelResponse response;
        try {
            response = modelGateway.complete(new ModelRequest(command.ownerId(), prompt, command.idempotencyKey()));
        } catch (ModelGenerationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ModelGenerationException("Model generation failed", exception);
        }

        return persistResponse(conversation.id(), userMessageId, response);
    }

    public SendMessageResult executeStreaming(SendMessageCommand command, Consumer<String> chunkConsumer) {
        Objects.requireNonNull(chunkConsumer, "Chunk consumer is required");
        return executeStreamingWithObserver(command, ModelStreamObserver.from(chunkConsumer));
    }

    public SendMessageResult executeStreamingWithObserver(SendMessageCommand command, ModelStreamObserver observer) {
        validate(command);
        Objects.requireNonNull(observer, "Stream observer is required");

        if (!(modelGateway instanceof StreamingModelGateway streamingModelGateway)) {
            SendMessageResult result = execute(command);
            observer.onStarted(result.runId(), result.correlationId());
            observer.onChunk(result.answer());
            return result;
        }

        String prompt = command.prompt().trim();
        ConversationStore.ConversationRef conversation = conversationStore
                .findOrCreate(command.ownerId(), command.conversationId());

        UUID userMessageId = conversationStore.appendMessage(
                conversation.id(),
                MessageRole.USER,
                prompt
        );

        ModelResponse response;
        try {
            response = streamingModelGateway.streamWithObserver(
                    new ModelRequest(command.ownerId(), prompt, command.idempotencyKey()),
                    observer
            );
        } catch (ModelGenerationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ModelGenerationException("Model generation failed", exception);
        }

        return persistResponse(conversation.id(), userMessageId, response);
    }

    private SendMessageResult persistResponse(UUID conversationId, UUID userMessageId, ModelResponse response) {
        if (response == null || response.answer() == null || response.answer().isBlank()) {
            throw new ModelGenerationException("Model returned an empty answer");
        }

        String answer = response.answer().trim();
        UUID assistantMessageId = conversationStore.appendMessage(
                conversationId,
                MessageRole.ASSISTANT,
                answer
        );

        return new SendMessageResult(
                conversationId,
                userMessageId,
                assistantMessageId,
                answer,
                response.model(),
                response.durationMs(),
                response.correlationId(),
                response.skillName(),
                response.runId(),
                response.status(),
                response.approvalRunId(),
                response.approvalNonce(),
                response.approvalExpiresAt()
        );
    }

    private void validate(SendMessageCommand command) {
        if (command == null || command.ownerId() == null) {
            throw new IllegalArgumentException("Owner is required");
        }
        if (command.prompt() == null || command.prompt().isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }
    }
}
