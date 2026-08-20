package com.macedxs.mx.api.v1.conversation;

import com.macedxs.mx.conversation.application.SendMessageCommand;
import com.macedxs.mx.conversation.application.SendMessageResult;
import com.macedxs.mx.conversation.application.SendMessageUseCase;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/v1/conversations")
public class ChatV1SseController {

    private final SendMessageUseCase sendMessageUseCase;
    private final UserRepository userRepository;
    private final Executor executor;

    public ChatV1SseController(
            SendMessageUseCase sendMessageUseCase,
            UserRepository userRepository,
            @Qualifier("mxStreamingExecutor") Executor executor
    ) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.userRepository = userRepository;
        this.executor = executor;
    }

    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(@Valid @RequestBody ChatV1Request request) {
        UserEntity user = currentUser();
        SseEmitter emitter = new SseEmitter(0L);
        SecurityContext securityContext = SecurityContextHolder.getContext();

        emitter.onTimeout(emitter::complete);
        emitter.onError(ignored -> emitter.complete());
        executor.execute(() -> {
            SecurityContext previous = SecurityContextHolder.getContext();
            SecurityContextHolder.setContext(securityContext);
            try {
                SendMessageResult result = sendMessageUseCase.executeStreamingWithObserver(
                        new SendMessageCommand(
                                user.getId(),
                                request.conversationId(),
                                request.prompt(),
                                request.idempotencyKey()
                        ),
                        new ModelStreamObserver() {
                            @Override
                            public void onStarted(UUID runId, UUID correlationId) {
                                send(emitter, "started", new ChatV1StreamStarted(runId, correlationId));
                            }

                            @Override
                            public void onChunk(String chunk) {
                                send(emitter, "token", new ChatV1StreamToken(chunk));
                            }
                        }
                );

                send(emitter, "completed", ChatV1ResponseMapper.completed(result));
                emitter.complete();
            } catch (RuntimeException failure) {
                send(emitter, "error", new ChatV1StreamError("STREAM_FAILED", safeMessage(failure)));
                emitter.complete();
            } finally {
                SecurityContextHolder.setContext(previous);
            }
        });

        return emitter;
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void send(SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
        } catch (IOException exception) {
            throw new IllegalStateException("SSE connection failed", exception);
        }
    }

    private String safeMessage(RuntimeException failure) {
        if (failure.getMessage() == null || failure.getMessage().isBlank()) {
            return "Unexpected streaming failure";
        }
        return failure.getMessage();
    }

    public record ChatV1StreamStarted(UUID runId, UUID correlationId) {
    }

    public record ChatV1StreamToken(String delta) {
    }

    public record ChatV1StreamError(String code, String message) {
    }
}
