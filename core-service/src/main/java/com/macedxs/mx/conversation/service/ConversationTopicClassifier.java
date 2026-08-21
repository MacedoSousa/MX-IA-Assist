package com.macedxs.mx.conversation.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class ConversationTopicClassifier {

    private static final Map<String, Set<String>> TOPIC_TERMS = new LinkedHashMap<>();

    static {
        TOPIC_TERMS.put("engenharia-de-software", Set.of(
                "java", "spring", "tdd", "solid", "clean architecture", "arquitetura", "codigo", "código",
                "software", "backend", "frontend", "typescript", "refatoracao", "refatoração"
        ));
        TOPIC_TERMS.put("ia-generativa", Set.of(
                "ia", "llm", "ollama", "prompt", "agente", "generativa", "modelo de linguagem", "qwen"
        ));
        TOPIC_TERMS.put("rag-e-agentes", Set.of(
                "rag", "retrieval", "embedding", "chunk", "vector", "vetorial", "workflow", "tool call"
        ));
        TOPIC_TERMS.put("dados-e-mlops", Set.of(
                "dados", "machine learning", "mlops", "pipeline", "modelo", "treino", "dataset", "metricas", "métricas"
        ));
        TOPIC_TERMS.put("infraestrutura-e-seguranca", Set.of(
                "docker", "seguranca", "segurança", "rede", "tailscale", "deploy", "linux", "compose", "gpu", "cpu"
        ));
        TOPIC_TERMS.put("qualidade-de-software", Set.of(
                "qualidade", "teste", "testes", "qualidade de software", "iso", "bug", "defeito", "qa"
        ));
        TOPIC_TERMS.put("produto-e-ensino", Set.of(
                "produto", "ensino", "aprender", "estudo", "aula", "didatica", "didática", "curso", "alura"
        ));
    }

    public Classification classify(String prompt) {
        String normalized = normalize(prompt);
        String topic = TOPIC_TERMS.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), score(normalized, entry.getValue())))
                .filter(entry -> entry.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("geral");
        return new Classification(topic, detectLanguage(normalized));
    }

    public String detectLanguage(String prompt) {
        String normalized = normalize(prompt);
        if (normalized.isBlank()) {
            return "pt-BR";
        }
        int portuguese = count(normalized, Set.of(
                "que", "para", "com", "uma", "como", "nao", "não", "voce", "você", "quero", "pode", "isso", "sobre"
        ));
        int english = count(normalized, Set.of(
                "the", "and", "for", "with", "how", "what", "you", "want", "can", "this", "about", "please"
        ));
        return english > portuguese ? "en" : "pt-BR";
    }

    private int score(String prompt, Set<String> terms) {
        return (int) terms.stream().filter(prompt::contains).count();
    }

    private int count(String prompt, Set<String> terms) {
        return (int) terms.stream().filter(prompt::contains).count();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record Classification(String topic, String language) {
    }
}
