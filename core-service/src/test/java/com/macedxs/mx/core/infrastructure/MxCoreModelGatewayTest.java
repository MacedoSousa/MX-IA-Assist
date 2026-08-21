package com.macedxs.mx.core.infrastructure;

import com.macedxs.mx.conversation.application.port.ModelGateway.ModelImage;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelRequest;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelResponse;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.core.application.MxCoreResponse;
import com.macedxs.mx.core.application.MxCoreService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MxCoreModelGatewayTest {

    @Test
    void streamsImageAttachmentsThroughMxCoreWithoutBypassingSkills() {
        MxCoreService coreService = mock(MxCoreService.class);
        MxCoreModelGateway gateway = new MxCoreModelGateway(coreService);
        ModelRequest request = new ModelRequest(
                UUID.randomUUID(),
                "Leia o código exibido na imagem.",
                "vision-test",
                List.of(new ModelImage("image/png", "aGVsbG8="))
        );
        UUID correlationId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        MxCoreResponse coreResponse = new MxCoreResponse(
                correlationId,
                "general",
                1.0d,
                false,
                "ORION-73",
                runId
        );
        ModelStreamObserver observer = mock(ModelStreamObserver.class);

        when(coreService.handleStreamingWithObserver(
                request.userId(),
                request.prompt(),
                request.idempotencyKey(),
                request.images(),
                observer
        )).thenReturn(coreResponse);

        ModelResponse response = gateway.streamWithObserver(request, observer);

        assertThat(response.answer()).isEqualTo("ORION-73");
        assertThat(response.skillName()).isEqualTo("general");
        assertThat(response.runId()).isEqualTo(runId);
        verify(coreService).handleStreamingWithObserver(
                request.userId(),
                request.prompt(),
                request.idempotencyKey(),
                request.images(),
                observer
        );
    }
}
