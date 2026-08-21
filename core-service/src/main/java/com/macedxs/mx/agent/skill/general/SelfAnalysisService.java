package com.macedxs.mx.agent.skill.general;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orquestra a cobertura local e uma busca pública somente como fallback.
 * Conteúdo externo é armazenado como evidência não privilegiada e não vira instrução.
 */
public final class SelfAnalysisService {

    private final StudyKnowledgeContext knowledgeContext;
    private final ExternalSearchClient searchClient;
    private final boolean enabled;
    private final int maxResults;
    private final Path auditPath;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SelfAnalysisService(
            StudyKnowledgeContext knowledgeContext,
            ExternalSearchClient searchClient,
            boolean enabled,
            int maxResults,
            Path auditPath
    ) {
        this(knowledgeContext, searchClient, enabled, maxResults, auditPath, new ObjectMapper(), Clock.systemUTC());
    }

    SelfAnalysisService(
            StudyKnowledgeContext knowledgeContext,
            ExternalSearchClient searchClient,
            boolean enabled,
            int maxResults,
            Path auditPath,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.knowledgeContext = knowledgeContext;
        this.searchClient = searchClient;
        this.enabled = enabled;
        this.maxResults = Math.max(1, Math.min(maxResults, 5));
        this.auditPath = auditPath;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public AnalysisResult analyze(String userPrompt) {
        StudyKnowledgeContext.Coverage coverage = knowledgeContext.assessCoverage(userPrompt);
        if (coverage.sufficient()) {
            AnalysisResult result = new AnalysisResult(false, 0, coverage, "cobertura local suficiente");
            audit(result, userPrompt);
            return result;
        }
        if (!enabled) {
            AnalysisResult result = new AnalysisResult(false, 0, coverage, "busca externa desativada");
            audit(result, userPrompt);
            return result;
        }
        String query = safeQuery(userPrompt);
        if (query.isBlank() || coverage.queryTokenCount() < 2) {
            AnalysisResult result = new AnalysisResult(false, 0, coverage, "consulta não elegível para busca externa");
            audit(result, userPrompt);
            return result;
        }

        List<ExternalSearchClient.SearchHit> hits;
        try {
            hits = searchClient.search(query, maxResults);
        } catch (RuntimeException exception) {
            AnalysisResult result = new AnalysisResult(false, 0, coverage, "busca pública indisponível");
            audit(result, userPrompt);
            return result;
        }
        int learned = 0;
        for (ExternalSearchClient.SearchHit hit : hits) {
            if (hit == null || hit.url() == null || hit.url().isBlank()) {
                continue;
            }
            String evidence = (hit.title() == null ? "" : hit.title().trim()) + "\n" +
                    (hit.snippet() == null ? "" : hit.snippet().trim());
            if (knowledgeContext.learnExternal(
                    "busca-externa:" + hit.url().trim(),
                    hit.title(),
                    evidence,
                    Set.of("busca-externa")
            )) {
                learned++;
            }
        }
        AnalysisResult result = new AnalysisResult(true, learned, coverage, hits.isEmpty() ? "nenhum resultado público" : "busca pública incorporada como evidência");
        audit(result, userPrompt);
        return result;
    }

    private String safeQuery(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+", "[token-redacted]")
                .replaceAll("(?i)(senha|password|secret|api[_-]?key|token)\\s*[:=]\\s*\\S+", "$1=[secret-redacted]")
                .replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[email-redacted]")
                .replaceAll("https?://\\S+", "[url-redacted]")
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .transform(value -> value.length() <= 320 ? value : value.substring(0, 320).trim());
    }

    private void audit(AnalysisResult result, String prompt) {
        if (auditPath == null) {
            return;
        }
        try {
            Path parent = auditPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("timestamp", Instant.now(clock).toString());
            event.put("event", "self-analysis");
            event.put("prompt_sha256", sha256(prompt == null ? "" : prompt));
            event.put("searched", result.searched());
            event.put("learned", result.learned());
            event.put("coverage_sufficient", result.coverage().sufficient());
            event.put("coverage_score", result.coverage().bestScore());
            event.put("reason", result.reason());
            Files.writeString(
                    auditPath,
                    objectMapper.writeValueAsString(event) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException | RuntimeException ignored) {
            // Falha de auditoria não interrompe uma resposta conversacional.
        }
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                hex.append(String.format("%02x", item));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }

    public record AnalysisResult(
            boolean searched,
            int learned,
            StudyKnowledgeContext.Coverage coverage,
            String reason
    ) {
    }
}
