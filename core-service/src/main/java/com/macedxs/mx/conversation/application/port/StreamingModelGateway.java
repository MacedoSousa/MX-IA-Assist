package com.macedxs.mx.conversation.application.port;

import java.util.Objects;
import java.util.function.Consumer;

public interface StreamingModelGateway extends ModelGateway {

    ModelResponse stream(ModelRequest request, Consumer<String> chunkConsumer);

    default ModelResponse streamWithObserver(ModelRequest request, ModelStreamObserver observer) {
        Objects.requireNonNull(observer, "Stream observer is required");
        return stream(request, observer::onChunk);
    }
}
