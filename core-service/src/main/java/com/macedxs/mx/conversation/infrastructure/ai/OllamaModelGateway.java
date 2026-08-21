package com.macedxs.mx.conversation.infrastructure.ai;

import com.macedxs.mx.ai.service.OllamaService;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class OllamaModelGateway implements StreamingModelGateway {

    private final OllamaService ollamaService;
    private final String visionModel;

    public OllamaModelGateway(OllamaService ollamaService) {
        this(ollamaService, "gemma4:e4b");
    }

    @Autowired
    public OllamaModelGateway(
            OllamaService ollamaService,
            @Value("${mx.ollama.vision-model:gemma4:e4b}") String visionModel
    ) {
        if (ollamaService == null) {
            throw new IllegalArgumentException("Ollama service is required");
        }
        this.ollamaService = ollamaService;
        this.visionModel = visionModel == null || visionModel.isBlank() ? "gemma4:e4b" : visionModel.trim();
    }

    @Override
    public ModelResponse complete(ModelRequest request) {
        long startedAt = System.nanoTime();
        try {
            String selectedModel = selectModel(request.images());
            String answer = ollamaService.generateText(request.prompt(), request.images(), selectedModel);
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            return new ModelResponse(answer, selectedModel, durationMs);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Model generation failed", exception);
        }
    }

    @Override
    public ModelResponse stream(ModelRequest request, Consumer<String> chunkConsumer) {
        long startedAt = System.nanoTime();
        try {
            String selectedModel = selectModel(request.images());
            String answer = ollamaService.streamText(
                    request.prompt(),
                    request.images(),
                    selectedModel,
                    chunkConsumer
            );
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            return new ModelResponse(answer, selectedModel, durationMs);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Model streaming failed", exception);
        }
    }

    private String selectModel(List<ModelGateway.ModelImage> images) {
        return images == null || images.isEmpty() ? ollamaService.model() : visionModel;
    }
}
