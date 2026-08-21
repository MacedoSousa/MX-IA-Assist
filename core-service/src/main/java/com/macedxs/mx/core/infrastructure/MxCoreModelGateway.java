package com.macedxs.mx.core.infrastructure;

import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;
import com.macedxs.mx.core.application.MxCoreResponse;
import com.macedxs.mx.core.application.MxCoreService;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class MxCoreModelGateway implements StreamingModelGateway {

    private final MxCoreService mxCoreService;

    public MxCoreModelGateway(MxCoreService mxCoreService) {
        this.mxCoreService = mxCoreService;
    }

    @Override
    public ModelResponse complete(ModelRequest request) {
        return toModelResponse(mxCoreService.handle(
                request.userId(),
                request.prompt(),
                request.idempotencyKey(),
                request.images()
        ));
    }

    @Override
    public ModelResponse stream(ModelRequest request, Consumer<String> chunkConsumer) {
        return toModelResponse(mxCoreService.handleStreaming(
                request.userId(),
                request.prompt(),
                request.idempotencyKey(),
                request.images(),
                chunkConsumer
        ));
    }

    @Override
    public ModelResponse streamWithObserver(ModelRequest request, ModelStreamObserver observer) {
        return toModelResponse(mxCoreService.handleStreamingWithObserver(
                request.userId(),
                request.prompt(),
                request.idempotencyKey(),
                request.images(),
                observer
        ));
    }

    private ModelResponse toModelResponse(MxCoreResponse response) {
        return new ModelResponse(
                response.answer(),
                response.skillName(),
                0L,
                response.correlationId(),
                response.skillName(),
                response.runId(),
                response.status(),
                response.approvalRunId(),
                response.approvalNonce(),
                response.approvalExpiresAt()
        );
    }
}
