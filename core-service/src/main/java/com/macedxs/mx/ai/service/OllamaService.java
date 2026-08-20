package com.macedxs.mx.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OllamaService {

    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(120);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final Duration requestTimeout;
    private final String model;

    public OllamaService() {
        this("http://localhost:11434", DEFAULT_REQUEST_TIMEOUT, "qwen3:8b");
    }

    public OllamaService(String baseUrl) {
        this(baseUrl, DEFAULT_REQUEST_TIMEOUT, "qwen3:8b");
    }

    @Autowired
    public OllamaService(
            @Value("${mx.ollama.url:http://localhost:11434}") String baseUrl,
            @Value("${mx.ollama.timeout-ms:120000}") long timeoutMs,
            @Value("${mx.ollama.model:qwen3:8b}") String model
    ) {
        this(baseUrl, Duration.ofMillis(timeoutMs), model);
    }

    public OllamaService(String baseUrl, Duration requestTimeout) {
        this(baseUrl, requestTimeout, "qwen3:8b");
    }

    public OllamaService(String baseUrl, Duration requestTimeout, String model) {
        if (requestTimeout == null || requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("Ollama request timeout must be positive");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Ollama model is required");
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
    }

    public String generateText(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }

        String body = "{\"model\":\"" + escapeJson(model) + "\",\"prompt\":\"" + escapeJson(prompt) + "\",\"stream\":false}";

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
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }
        Objects.requireNonNull(chunkConsumer, "Chunk consumer is required");

        String body = "{\"model\":\"" + escapeJson(model) + "\",\"prompt\":\"" + escapeJson(prompt) + "\",\"stream\":true}";
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

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
