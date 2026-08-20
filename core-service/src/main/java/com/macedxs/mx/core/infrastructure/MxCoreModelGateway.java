package com.macedxs.mx.core.infrastructure;

import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;

import java.util.function.Consumer;
import com.macedxs.mx.core.application.MxCoreResponse;
import com.macedxs.mx.core.application.MxCoreService;
import org.springframework.stereotype.Component;

@Component
public class MxCoreModelGateway implements StreamingModelGateway {

    private final MxCoreService mxCoreService;

    public MxCoreModelGateway(MxCoreService mxCoreService) {
        this.mxCoreService = mxCoreService;
    }

    @Override
    public ModelResponse complete(ModelRequest request) {
        MxCoreResponse response = mxCoreService.handle(request.userId(), request.prompt());
        return new ModelResponse(
                response.answer(),
                response.skillName(),
                0L,
                response.correlationId(),
                response.skillName(),
                response.runId()
        );
    }

    @Override
    public ModelResponse stream(ModelRequest request, Consumer<String> chunkConsumer) {
        MxCoreResponse response = mxCoreService.handleStreaming(
                request.userId(),
                request.prompt(),
                chunkConsumer
        );
        return new ModelResponse(
                response.answer(),
                response.skillName(),
                0L,
                response.correlationId(),
                response.skillName(),
                response.runId()
        );
    }

    @Override
    public ModelResponse streamWithObserver(ModelRequest request, ModelStreamObserver observer) {
        MxCoreResponse response = mxCoreService.handleStreamingWithObserver(
                request.userId(),
                request.prompt(),
                observer
        );
        return new ModelResponse(
                response.answer(),
                response.skillName(),
                0L,
                response.correlationId(),
                response.skillName(),
                response.runId()
        );
    }
}
