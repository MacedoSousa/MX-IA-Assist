package com.macedxs.mx.core.infrastructure;

import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;
import com.macedxs.mx.core.application.MxCoreResponse;
import com.macedxs.mx.core.application.MxCoreService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class MxCoreModelGateway implements StreamingModelGateway {

    private final MxCoreService mxCoreService;
    private final StreamingModelGateway visualModelGateway;

    public MxCoreModelGateway(
            MxCoreService mxCoreService,
            @Qualifier("ollamaModelGateway") StreamingModelGateway visualModelGateway
    ) {
        this.mxCoreService = mxCoreService;
        this.visualModelGateway = visualModelGateway;
    }

    @Override
    public ModelResponse complete(ModelRequest request) {
        if (hasImages(request)) {
            return visualModelGateway.complete(request);
        }
        return toModelResponse(mxCoreService.handle(
                request.userId(),
                request.prompt(),
                request.idempotencyKey()
        ));
    }

    @Override
    public ModelResponse stream(ModelRequest request, Consumer<String> chunkConsumer) {
        if (hasImages(request)) {
            return visualModelGateway.stream(request, chunkConsumer);
        }
        return toModelResponse(mxCoreService.handleStreaming(
                request.userId(),
                request.prompt(),
                request.idempotencyKey(),
                chunkConsumer
        ));
    }

    @Override
    public ModelResponse streamWithObserver(ModelRequest request, ModelStreamObserver observer) {
        if (hasImages(request)) {
            return visualModelGateway.streamWithObserver(request, observer);
        }
        return toModelResponse(mxCoreService.handleStreamingWithObserver(
                request.userId(),
                request.prompt(),
                request.idempotencyKey(),
                observer
        ));
    }

    private boolean hasImages(ModelRequest request) {
        return request != null && request.images() != null && !request.images().isEmpty();
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
