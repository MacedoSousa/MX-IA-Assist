package com.macedxs.mx.api.v1.conversation;

import com.macedxs.mx.conversation.application.SendMessageResult;
import com.macedxs.mx.conversation.application.SendMessageUseCase;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatV1SseControllerTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldStartStreamingForTheAuthenticatedUser() {
        SendMessageUseCase useCase = mock(SendMessageUseCase.class);
        UserRepository userRepository = mock(UserRepository.class);
        UUID userId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setEmail("mx@example.com");
        when(userRepository.findByEmail("mx@example.com")).thenReturn(java.util.Optional.of(user));

        SendMessageResult result = new SendMessageResult(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Resposta final",
                "qwen3",
                25L,
                correlationId,
                "general",
                runId
        );
        when(useCase.executeStreamingWithObserver(any(), any())).thenAnswer(invocation -> {
            com.macedxs.mx.conversation.application.port.ModelStreamObserver observer = invocation.getArgument(1);
            observer.onStarted(runId, correlationId);
            observer.onChunk("Resposta final");
            return result;
        });

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("mx@example.com", "n/a")
        );
        Executor directExecutor = Runnable::run;
        ChatV1SseController controller = new ChatV1SseController(useCase, userRepository, directExecutor);

        SseEmitter emitter = controller.streamMessage(new ChatV1Request(null, "Olá MX", null));

        assertThat(emitter).isNotNull();
        verify(useCase).executeStreamingWithObserver(any(), any());
    }
}
