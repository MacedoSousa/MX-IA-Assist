package com.macedxs.mx.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OllamaService {

    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(120);
    private static final String DEFAULT_KEEP_ALIVE = "10m";
    private static final int DEFAULT_CONTEXT_SIZE = 8192;
    private static final int DEFAULT_NUM_THREAD = 0;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final Duration requestTimeout;
    private final String model;
    private final String keepAlive;
    private final int contextSize;
    private final int numThread;

    public OllamaService() {
        this("http://localhost:11434", DEFAULT_REQUEST_TIMEOUT, "qwen3:8b", DEFAULT_KEEP_ALIVE, DEFAULT_CONTEXT_SIZE, DEFAULT_NUM_THREAD);
    }

    public OllamaService(String baseUrl) {
        this(baseUrl, DEFAULT_REQUEST_TIMEOUT, "qwen3:8b", DEFAULT_KEEP_ALIVE);
    }

    @Autowired
    public OllamaService(
            @Value("${mx.ollama.url:http://localhost:11434}") String baseUrl,
            @Value("${mx.ollama.timeout-ms:120000}") long timeoutMs,
            @Value("${mx.ollama.model:qwen3:8b}") String model,
            @Value("${mx.ollama.keep-alive:10m}") String keepAlive,
            @Value("${mx.ollama.num-ctx:8192}") int contextSize,
            @Value("${mx.ollama.num-thread:0}") int numThread
    ) {
        this(baseUrl, Duration.ofMillis(timeoutMs), model, keepAlive, contextSize, numThread);
    }

    public OllamaService(String baseUrl, Duration requestTimeout) {
        this(baseUrl, requestTimeout, "qwen3:8b", DEFAULT_KEEP_ALIVE);
    }

    public OllamaService(String baseUrl, Duration requestTimeout, String model) {
        this(baseUrl, requestTimeout, model, DEFAULT_KEEP_ALIVE);
    }

    public OllamaService(String baseUrl, Duration requestTimeout, String model, String keepAlive) {
        this(baseUrl, requestTimeout, model, keepAlive, DEFAULT_CONTEXT_SIZE, DEFAULT_NUM_THREAD);
    }

    public OllamaService(
            String baseUrl,
            Duration requestTimeout,
            String model,
            String keepAlive,
            int contextSize,
            int numThread
    ) {
        if (requestTimeout == null || requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("Ollama request timeout must be positive");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Ollama model is required");
        }
        if (contextSize <= 0 || numThread < 0) {
            throw new IllegalArgumentException("Ollama context and thread settings are invalid");
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(requestTimeout)
                .build();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl != null && !baseUrl.isBlank()
                ? baseUrl.replaceAll("/+$", "")
                : "http://localhost:11434";
        this.requestTimeout = requestTimeout;
        this.model = model.trim();
        this.keepAlive = keepAlive == null || keepAlive.isBlank() ? DEFAULT_KEEP_ALIVE : keepAlive.trim();
        this.contextSize = contextSize;
        this.numThread = numThread;
    }

    public String generateText(String prompt) {
        return generateText(prompt, List.of(), model);
    }

    public String generateText(String prompt, List<ModelImage> images, String requestedModel) {
        validatePrompt(prompt);
        String body = requestBody(prompt, false, images, requestedModel);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
                .timeout(requestTimeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Ollama request failed with status " + response.statusCode() + ": " + response.body());
            }
            JsonNode json = objectMapper.readTree(response.body());
            return json.path("response").asText();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to reach Ollama", ex);
        }
    }

    public String streamText(String prompt, Consumer<String> chunkConsumer) {
        return streamText(prompt, List.of(), model, chunkConsumer);
    }

    public String streamText(
            String prompt,
            List<ModelImage> images,
            String requestedModel,
            Consumer<String> chunkConsumer
    ) {
        validatePrompt(prompt);
        Objects.requireNonNull(chunkConsumer, "Chunk consumer is required");

        String body = requestBody(prompt, true, images, requestedModel);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
                .timeout(requestTimeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<Stream<String>> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofLines()
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody;
                try (Stream<String> lines = response.body()) {
                    errorBody = lines.collect(Collectors.joining("\n"));
                }
                throw new IllegalStateException(
                        "Ollama request failed with status " + response.statusCode() + ": " + errorBody
                );
            }

            StringBuilder answer = new StringBuilder();
            try (Stream<String> lines = response.body()) {
                lines.filter(line -> line != null && !line.isBlank()).forEach(line -> {
                    try {
                        JsonNode json = objectMapper.readTree(line);
                        String chunk = json.path("response").asText("");
                        if (!chunk.isEmpty()) {
                            answer.append(chunk);
                            chunkConsumer.accept(chunk);
                        }
                    } catch (Exception exception) {
                        throw new IllegalStateException("Invalid Ollama streaming response", exception);
                    }
                });
            }
            return answer.toString();
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to reach Ollama", exception);
        }
    }

    public String model() {
        return model;
    }

    private String requestBody(String prompt, boolean stream, List<ModelImage> images, String requestedModel) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", normalizeModel(requestedModel));
        payload.put("prompt", prompt);
        payload.put("stream", stream);
        payload.put("keep_alive", keepAlive);
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("num_ctx", contextSize);
        if (numThread > 0) {
            options.put("num_thread", numThread);
        }
        payload.put("options", options);
        if (images != null && !images.isEmpty()) {
            payload.put("images", images.stream().map(ModelImage::base64Data).toList());
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize Ollama request", exception);
        }
    }

    private String normalizeModel(String requestedModel) {
        return requestedModel == null || requestedModel.isBlank() ? model : requestedModel.trim();
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }
    }
}
