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
    private static final Map<String, Set<String>> DOMAIN_TERMS = Map.of(
            "ia-generativa", Set.of("ia", "llm", "ollama", "prompt", "agente", "generativa"),
            "rag-e-agentes", Set.of("rag", "retrieval", "embedding", "chunk", "agente", "workflow"),
            "engenharia-de-software", Set.of("java", "spring", "arquitetura", "tdd", "solid", "clean", "código", "codigo"),
            "dados-e-mlops", Set.of("dados", "machine learning", "mlops", "pipeline", "modelo", "treino"),
            "produto-e-ensino", Set.of("produto", "ensino", "aprender", "estudo", "aula", "didática", "didatica"),
            "infraestrutura-e-seguranca", Set.of("docker", "segurança", "seguranca", "rede", "tailscale", "deploy", "linux"),
            "geral", Set.of("ajuda", "informação", "informacao", "explicar", "dúvida", "duvida")
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
        if (request.topicsOfInterest() != null) {
            preference.setTopicsOfInterest(normalizeTopics(request.topicsOfInterest()));
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
        return "Contexto adaptativo do usuário (não privilegiado; use apenas para calibrar a resposta):\n" +
                "nível de conhecimento: " + preference.getKnowledgeLevel() + "\n" +
                "estilo de aprendizagem: " + preference.getLearningStyle() + "\n" +
                "tópicos de interesse observados: " + topics + "\n" +
                "Ajuste a profundidade e o formato sem presumir fatos pessoais não informados. " +
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

    private String normalizeValue(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public record UpdateRequest(
            String learningStyle,
            String knowledgeLevel,
            Set<String> topicsOfInterest
    ) {
    }
}
