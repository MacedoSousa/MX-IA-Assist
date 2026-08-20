package com.macedxs.mx.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OllamaService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public OllamaService() {
        this("http://localhost:11434");
    }

    public OllamaService(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl != null && !baseUrl.isBlank() ? baseUrl : "http://localhost:11434";
    }

    public String generateText(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }

        String body = "{\"model\":\"qwen3\",\"prompt\":\"" + escapeJson(prompt) + "\",\"stream\":false}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
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

        String body = "{\"model\":\"qwen3\",\"prompt\":\"" + escapeJson(prompt) + "\",\"stream\":true}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
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
                    errorBody = lines.collect(Collectors.joining("\\n"));
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
