package com.macedxs.mx.conversation.infrastructure.ai;

import com.macedxs.mx.ai.service.OllamaService;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;

import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class OllamaModelGateway implements StreamingModelGateway {

    private final OllamaService ollamaService;

    public OllamaModelGateway(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @Override
    public ModelResponse complete(ModelRequest request) {
        long startedAt = System.nanoTime();
        try {
            String answer = ollamaService.generateText(request.prompt());
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            return new ModelResponse(answer, "qwen3", durationMs);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Model generation failed", exception);
        }
    }

    @Override
    public ModelResponse stream(ModelRequest request, Consumer<String> chunkConsumer) {
        long startedAt = System.nanoTime();
        try {
            String answer = ollamaService.streamText(request.prompt(), chunkConsumer);
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            return new ModelResponse(answer, "qwen3", durationMs);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Model streaming failed", exception);
        }
    }
}
