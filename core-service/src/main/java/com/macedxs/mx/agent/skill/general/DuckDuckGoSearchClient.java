package com.macedxs.mx.agent.skill.general;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Integração pública e somente leitura com o DuckDuckGo Instant Answer API.
 * Não segue links dos resultados e não executa conteúdo remoto.
 */
public final class DuckDuckGoSearchClient implements ExternalSearchClient {

    private static final String ALLOWED_HOST = "api.duckduckgo.com";
    private static final int MAX_QUERY_CHARS = 320;
    private static final int MAX_RESULT_CHARS = 700;

    private final URI endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestTimeout;

    public DuckDuckGoSearchClient(URI endpoint) {
        this(endpoint, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build(), new ObjectMapper());
    }

    DuckDuckGoSearchClient(URI endpoint, HttpClient httpClient, ObjectMapper objectMapper) {
        this(endpoint, httpClient, objectMapper, Duration.ofSeconds(8));
    }

    DuckDuckGoSearchClient(URI endpoint, HttpClient httpClient, ObjectMapper objectMapper, Duration requestTimeout) {
        this.endpoint = validateEndpoint(endpoint);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public List<SearchHit> search(String query, int maxResults) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        int limit = Math.max(1, Math.min(maxResults, 5));
        String encoded = URLEncoder.encode(sanitize(query, MAX_QUERY_CHARS), StandardCharsets.UTF_8);
        URI target = URI.create(endpoint + "?q=" + encoded + "&format=json&no_html=1&skip_disambig=1");
        HttpRequest request = HttpRequest.newBuilder(target)
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .header("User-Agent", "MX-local-assistant/1.0")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                return List.of();
            }
            JsonNode root = objectMapper.readTree(response.body());
            List<SearchHit> hits = new ArrayList<>();
            addHit(root, hits, limit);
            collectRelatedTopics(root.path("RelatedTopics"), hits, limit);
            return List.copyOf(hits);
        } catch (IOException | InterruptedException | RuntimeException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return List.of();
        }
    }

    private void addHit(JsonNode node, List<SearchHit> hits, int limit) {
        if (hits.size() >= limit) {
            return;
        }
        String text = sanitize(node.path("Text").asText(""), MAX_RESULT_CHARS);
        String url = safeUrl(node.path("FirstURL").asText(""));
        if (!text.isBlank() && !url.isBlank()) {
            String title = text.contains(" - ") ? text.substring(0, text.indexOf(" - ")) : text;
            hits.add(new SearchHit(title.trim(), url, text));
        }
    }

    private void collectRelatedTopics(JsonNode topics, List<SearchHit> hits, int limit) {
        if (!topics.isArray()) {
            return;
        }
        for (JsonNode topic : topics) {
            if (hits.size() >= limit) {
                return;
            }
            if (topic.has("Topics")) {
                collectRelatedTopics(topic.path("Topics"), hits, limit);
            } else {
                addHit(topic, hits, limit);
            }
        }
    }

    private URI validateEndpoint(URI value) {
        if (value == null || !"https".equalsIgnoreCase(value.getScheme())
                || !ALLOWED_HOST.equalsIgnoreCase(value.getHost())) {
            throw new IllegalArgumentException("Endpoint de busca externa não permitido");
        }
        return value;
    }

    private String safeUrl(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            URI uri = URI.create(value.trim());
            if (("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null) {
                return uri.toString();
            }
        } catch (IllegalArgumentException ignored) {
            // Resultado malformado é descartado como dado não confiável.
        }
        return "";
    }

    private String sanitize(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .replaceAll("\\s+", " ")
                .trim()
                .transform(text -> text.length() <= maxChars ? text : text.substring(0, maxChars).trim());
    }
}
