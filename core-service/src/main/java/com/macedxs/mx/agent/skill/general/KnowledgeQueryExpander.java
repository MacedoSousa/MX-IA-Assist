package com.macedxs.mx.agent.skill.general;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Expande consultas de estudo com aliases controlados em PT-BR e inglês.
 * A expansão é determinística, local e limitada para preservar latência e auditabilidade.
 */
public final class KnowledgeQueryExpander {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^\\p{L}\\p{Nd}]+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Map<String, Set<String>> ALIASES = aliases();

    public ExpandedQuery expand(String query) {
        Set<String> original = tokens(query);
        Set<String> expanded = new LinkedHashSet<>(original);
        String normalizedQuery = normalize(query);
        ALIASES.forEach((term, related) -> {
            if (containsTerm(normalizedQuery, term)) {
                related.stream().map(this::normalize).flatMap(value -> tokens(value).stream()).forEach(expanded::add);
            }
        });
        return new ExpandedQuery(original, expanded);
    }

    Set<String> tokens(String text) {
        Set<String> result = new LinkedHashSet<>();
        for (String token : TOKEN_SPLIT.split(normalize(text))) {
            if (token.length() >= 3) {
                result.add(token);
            }
        }
        return result;
    }

    String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return Normalizer.normalize(decomposed, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsTerm(String query, String term) {
        String normalizedTerm = normalize(term);
        if (normalizedTerm.contains(" ")) {
            return query.contains(normalizedTerm);
        }
        return tokens(query).contains(normalizedTerm);
    }

    private static Map<String, Set<String>> aliases() {
        Map<String, Set<String>> aliases = new LinkedHashMap<>();
        put(aliases, Set.of("aprendizado", "aprendizagem", "learning"),
                "machine learning", "ml", "modelo", "model", "treino", "training");
        put(aliases, Set.of("ia", "inteligencia artificial", "artificial intelligence"),
                "ai", "llm", "modelo de linguagem", "generative ai");
        put(aliases, Set.of("agente", "agentes", "agent", "agents"),
                "workflow", "tool", "tool call", "orquestracao", "orchestration");
        put(aliases, Set.of("busca", "pesquisa", "retrieval", "recuperacao", "recuperação"),
                "rag", "chunk", "embedding", "fonte", "source", "retrieval");
        put(aliases, Set.of("qualidade", "quality", "teste", "testes", "testing"),
                "tdd", "automacao", "automation", "bug", "defeito", "coverage");
        put(aliases, Set.of("docker", "container", "containers", "infraestrutura", "infrastructure"),
                "compose", "deploy", "cpu", "gpu", "ollama", "performance");
        put(aliases, Set.of("ensino", "didatica", "didática", "teaching"),
                "explicacao", "explicação", "exercise", "exercicio", "exercício", "avaliacao", "avaliação");
        return aliases;
    }

    private static void put(Map<String, Set<String>> aliases, Collection<String> keys, String... related) {
        Set<String> values = Set.of(related);
        keys.forEach(key -> aliases.put(key, values));
    }

    public record ExpandedQuery(Set<String> originalTokens, Set<String> expandedTokens) {
        public ExpandedQuery {
            originalTokens = Set.copyOf(originalTokens);
            expandedTokens = Set.copyOf(expandedTokens);
        }

        public Set<String> expandedOnly() {
            Set<String> result = new LinkedHashSet<>(expandedTokens);
            result.removeAll(originalTokens);
            return Set.copyOf(result);
        }
    }
}
