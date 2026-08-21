package com.macedxs.mx.core.infrastructure;

import com.macedxs.mx.conversation.application.port.ModelGateway.ModelImage;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelRequest;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelResponse;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;
import com.macedxs.mx.core.application.MxCoreService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MxCoreModelGatewayTest {

    @Test
    void streamsImageAttachmentsThroughTheVisionGateway() {
        MxCoreService coreService = mock(MxCoreService.class);
        StreamingModelGateway visionGateway = mock(StreamingModelGateway.class);
        MxCoreModelGateway gateway = new MxCoreModelGateway(coreService, visionGateway);
        ModelRequest request = new ModelRequest(
                UUID.randomUUID(),
                "Leia o código exibido na imagem.",
                "vision-test",
                List.of(new ModelImage("image/png", "aGVsbG8="))
        );
        ModelResponse expected = new ModelResponse("ORION-73", "gemma4:e4b", 42L);
        ModelStreamObserver observer = mock(ModelStreamObserver.class);

        when(visionGateway.streamWithObserver(same(request), same(observer))).thenReturn(expected);

        ModelResponse response = gateway.streamWithObserver(request, observer);

        assertThat(response).isEqualTo(expected);
        verify(visionGateway).streamWithObserver(same(request), same(observer));
        verifyNoInteractions(coreService);
    }
}
