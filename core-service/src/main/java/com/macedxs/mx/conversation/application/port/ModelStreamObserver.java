package com.macedxs.mx.conversation.application.port;

import java.util.UUID;
import java.util.function.Consumer;

public interface ModelStreamObserver {

    default void onStarted(UUID runId, UUID correlationId) {
        // Gateways que não controlam o lifecycle podem omitir o evento inicial.
    }

    void onChunk(String chunk);

    static ModelStreamObserver from(Consumer<String> chunkConsumer) {
        return new ModelStreamObserver() {
            @Override
            public void onChunk(String chunk) {
                chunkConsumer.accept(chunk);
            }
        };
    }
}
