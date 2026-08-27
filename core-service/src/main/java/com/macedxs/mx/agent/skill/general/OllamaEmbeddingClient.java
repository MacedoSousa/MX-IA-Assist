package com.macedxs.mx.agent.skill.general;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Calls Ollama's local /api/embed contract and returns no content in failures. */
public final class OllamaEmbeddingClient implements SemanticEmbeddingClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI endpoint;
    private final String model;
    private final Duration timeout;

    public OllamaEmbeddingClient(URI ollamaBaseUrl, String model, Duration timeout) {
        if (ollamaBaseUrl == null || model == null || model.isBlank() || timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Ollama embedding configuration is invalid");
        }
        this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.objectMapper = new ObjectMapper();
        this.endpoint = ollamaBaseUrl.resolve("/api/embed");
        this.model = model.trim();
        this.timeout = timeout;
    }

    @Override
    public List<double[]> embed(List<String> inputs) {
        if (inputs == null || inputs.isEmpty() || inputs.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("Embedding inputs are required");
        }
        try {
            byte[] payload = objectMapper.writeValueAsBytes(java.util.Map.of("model", model, "input", inputs, "truncate", true));
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Local embedding endpoint is unavailable");
            }
            JsonNode embeddings = objectMapper.readTree(response.body()).path("embeddings");
            if (!embeddings.isArray() || embeddings.size() != inputs.size()) {
                throw new IllegalStateException("Local embedding response is invalid");
            }
            List<double[]> result = new ArrayList<>();
            for (JsonNode embedding : embeddings) {
                if (!embedding.isArray() || embedding.isEmpty()) {
                    throw new IllegalStateException("Local embedding vector is invalid");
                }
                double[] vector = new double[embedding.size()];
                for (int index = 0; index < embedding.size(); index++) {
                    vector[index] = embedding.get(index).asDouble(Double.NaN);
                    if (!Double.isFinite(vector[index])) {
                        throw new IllegalStateException("Local embedding vector is invalid");
                    }
                }
                result.add(vector);
            }
            return List.copyOf(result);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new IllegalStateException("Local embedding endpoint is unavailable", exception);
        }
    }
}
