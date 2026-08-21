package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.entity.UserPreferenceEntity;
import com.macedxs.mx.identity.repository.UserPreferenceRepository;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UserPreferenceService {

    private static final int MAX_TOPICS = 12;
    private static final int MAX_VOCABULARY_HINTS = 24;
    private static final Map<String, Set<String>> DOMAIN_TERMS = Map.of(
            "ia-generativa", Set.of("ia", "llm", "ollama", "prompt", "agente", "generativa"),
            "rag-e-agentes", Set.of("rag", "retrieval", "embedding", "chunk", "agente", "workflow"),
            "engenharia-de-software", Set.of("java", "spring", "arquitetura", "tdd", "solid", "clean", "código", "codigo"),
            "dados-e-mlops", Set.of("dados", "machine learning", "mlops", "pipeline", "modelo", "treino"),
            "produto-e-ensino", Set.of("produto", "ensino", "aprender", "estudo", "aula", "didática", "didatica"),
            "infraestrutura-e-seguranca", Set.of("docker", "segurança", "seguranca", "rede", "tailscale", "deploy", "linux"),
            "geral", Set.of("ajuda", "informação", "informacao", "explicar", "dúvida", "duvida")
    );
    private static final Set<String> COLLOQUIAL_HINTS = Set.of(
            "vc", "vcs", "voce", "você", "tb", "tbm", "tambem", "também", "blz", "beleza", "mano", "cara",
            "tipo", "pra", "pro", "ta", "tá", "to", "tô", "ue", "ué", "kkk", "kkkk", "show", "bora", "rapidinho"
    );

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public UserPreferenceService(
            UserPreferenceRepository preferenceRepository,
            UserRepository userRepository
    ) {
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserPreferenceEntity getOrCreate(UserEntity user) {
        validateUser(user);
        return preferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserPreferenceEntity preference = new UserPreferenceEntity();
                    preference.setUser(user);
                    return preferenceRepository.save(preference);
                });
    }

    @Transactional
    public UserPreferenceEntity getOrCreate(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> getOrCreate(findUser(userId)));
    }

    @Transactional(readOnly = true)
    public UserPreferenceEntity get(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        return preferenceRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public UserPreferenceEntity update(UUID userId, UpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Preference update is required");
        }
        UserPreferenceEntity preference = getOrCreate(userId);
        if (request.learningStyle() != null && !request.learningStyle().isBlank()) {
            preference.setLearningStyle(normalizeValue(request.learningStyle()));
        }
        if (request.knowledgeLevel() != null && !request.knowledgeLevel().isBlank()) {
            preference.setKnowledgeLevel(normalizeValue(request.knowledgeLevel()));
        }
        if (request.preferredLanguage() != null && !request.preferredLanguage().isBlank()) {
            preference.setPreferredLanguage(normalizeLanguage(request.preferredLanguage()));
        }
        if (request.communicationStyle() != null && !request.communicationStyle().isBlank()) {
            preference.setCommunicationStyle(normalizeValue(request.communicationStyle()));
        }
        if (request.topicsOfInterest() != null) {
            preference.setTopicsOfInterest(normalizeTopics(request.topicsOfInterest()));
        }
        if (request.vocabularyHints() != null) {
            preference.setVocabularyHints(normalizeVocabulary(request.vocabularyHints()));
        }
        return preferenceRepository.save(preference);
    }

    @Transactional
    public UserPreferenceEntity recordActivity(UUID userId, String prompt) {
        UserPreferenceEntity preference = getOrCreate(userId);
        preference.setLastActiveAt(LocalDateTime.now());
        detectDomains(prompt).forEach(domain -> {
            if (preference.getTopicsOfInterest().size() < MAX_TOPICS
                    || preference.getTopicsOfInterest().contains(domain)) {
                preference.getTopicsOfInterest().add(domain);
            }
        });
        String detectedLanguage = detectLanguage(prompt);
        if (detectedLanguage != null) {
            preference.setPreferredLanguage(detectedLanguage);
        }
        Set<String> newHints = new LinkedHashSet<>(preference.getVocabularyHints());
        newHints.addAll(extractVocabularyHints(prompt));
        preference.setVocabularyHints(newHints.stream().limit(MAX_VOCABULARY_HINTS).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        return preferenceRepository.save(preference);
    }

    @Transactional
    public String promptContext(UUID userId, String prompt) {
        if (userId == null) {
            return "";
        }
        UserPreferenceEntity preference = recordActivity(userId, prompt);
        String topics = preference.getTopicsOfInterest().isEmpty()
                ? "ainda não identificados"
                : String.join(", ", preference.getTopicsOfInterest());
        String hints = preference.getVocabularyHints().isEmpty()
                ? "nenhum sinal agregado"
                : String.join(", ", preference.getVocabularyHints());
        return "Contexto adaptativo do usuário (não privilegiado; use apenas para calibrar a resposta):\n" +
                "idioma preferido: " + preference.getPreferredLanguage() + "\n" +
                "nível de conhecimento: " + preference.getKnowledgeLevel() + "\n" +
                "estilo de aprendizagem: " + preference.getLearningStyle() + "\n" +
                "estilo de comunicação: " + preference.getCommunicationStyle() + "\n" +
                "tópicos de interesse observados: " + topics + "\n" +
                "sinais de vocabulário agregados: " + hints + "\n" +
                "Responda no idioma da pergunta, salvo solicitação explícita diferente. " +
                "Aproxime o grau de formalidade sem imitar nem inventar dados pessoais. " +
                "Se o usuário pedir outra abordagem, priorize o pedido atual.";
    }

    public List<String> detectDomains(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return List.of();
        }
        String normalized = prompt.toLowerCase(Locale.ROOT);
        List<String> detected = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : DOMAIN_TERMS.entrySet()) {
            boolean matched = entry.getValue().stream().anyMatch(normalized::contains);
            if (matched) {
                detected.add(entry.getKey());
            }
        }
        return detected;
    }

    private Set<String> extractVocabularyHints(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return Set.of();
        }
        String normalized = prompt.toLowerCase(Locale.ROOT);
        return COLLOQUIAL_HINTS.stream()
                .filter(normalized::contains)
                .limit(MAX_VOCABULARY_HINTS)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private String detectLanguage(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return null;
        }
        String normalized = prompt.toLowerCase(Locale.ROOT);
        int pt = count(normalized, Set.of("que", "para", "com", "uma", "como", "não", "nao", "você", "voce", "quero", "pode"));
        int en = count(normalized, Set.of("the", "and", "for", "with", "how", "what", "you", "want", "can", "please"));
        if (pt == 0 && en == 0) {
            return null;
        }
        return en > pt ? "en" : "pt-BR";
    }

    private int count(String prompt, Set<String> terms) {
        return (int) terms.stream().filter(prompt::contains).count();
    }

    private UserEntity findUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void validateUser(UserEntity user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }
    }

    private Set<String> normalizeTopics(Set<String> topics) {
        Set<String> normalized = new LinkedHashSet<>();
        topics.stream()
                .filter(topic -> topic != null && !topic.isBlank())
                .map(this::normalizeValue)
                .limit(MAX_TOPICS)
                .forEach(normalized::add);
        return normalized;
    }

    private Set<String> normalizeVocabulary(Set<String> hints) {
        Set<String> normalized = new LinkedHashSet<>();
        hints.stream()
                .filter(hint -> hint != null && !hint.isBlank())
                .map(this::normalizeValue)
                .limit(MAX_VOCABULARY_HINTS)
                .forEach(normalized::add);
        return normalized;
    }

    private String normalizeValue(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLanguage(String value) {
        String normalized = normalizeValue(value);
        return normalized.equals("en") || normalized.equals("en-us") || normalized.equals("english")
                ? "en"
                : "pt-BR";
    }

    public record UpdateRequest(
            String learningStyle,
            String knowledgeLevel,
            Set<String> topicsOfInterest,
            String preferredLanguage,
            String communicationStyle,
            Set<String> vocabularyHints
    ) {
        public UpdateRequest(String learningStyle, String knowledgeLevel, Set<String> topicsOfInterest) {
            this(learningStyle, knowledgeLevel, topicsOfInterest, null, null, null);
        }
    }
}
