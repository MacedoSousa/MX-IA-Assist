package com.macedxs.mx.identity.service;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.entity.UserPreferenceEntity;
import com.macedxs.mx.identity.repository.UserPreferenceRepository;
import com.macedxs.mx.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserPreferenceServiceTest {

    private final UserPreferenceRepository preferenceRepository = mock(UserPreferenceRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserPreferenceService service = new UserPreferenceService(preferenceRepository, userRepository);

    @Test
    void shouldCreateDefaultPreferencesForAnExistingUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user(userId);
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(UserPreferenceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferenceEntity preference = service.getOrCreate(user);

        assertThat(preference.getUser()).isSameAs(user);
        assertThat(preference.getLearningStyle()).isEqualTo("balanced");
        assertThat(preference.getKnowledgeLevel()).isEqualTo("beginner");
        assertThat(preference.getTopicsOfInterest()).isEmpty();
    }

    @Test
    void shouldUpdateLearningPreferencesAndNormalizeValues() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user(userId);
        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setUser(user);
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(any(UserPreferenceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferenceEntity updated = service.update(
                userId,
                new UserPreferenceService.UpdateRequest(
                        "  visual  ",
                        "  intermediate ",
                        Set.of(" RAG ", "Docker")
                )
        );

        assertThat(updated.getLearningStyle()).isEqualTo("visual");
        assertThat(updated.getKnowledgeLevel()).isEqualTo("intermediate");
        assertThat(updated.getTopicsOfInterest()).containsExactlyInAnyOrder("rag", "docker");
    }

    @Test
    void shouldRecordActivityAndDetectStudyDomains() {
        UUID userId = UUID.randomUUID();
        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setUser(user(userId));
        preference.setTopicsOfInterest(new LinkedHashSet<>());
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(any(UserPreferenceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferenceEntity updated = service.recordActivity(userId, "Quero estudar RAG com agentes e TDD em Java");

        assertThat(updated.getLastActiveAt()).isNotNull();
        assertThat(updated.getTopicsOfInterest())
                .contains("rag-e-agentes", "engenharia-de-software");
    }

    @Test
    void shouldBuildNonPrivilegedAdaptivePromptContext() {
        UUID userId = UUID.randomUUID();
        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setUser(user(userId));
        preference.setLearningStyle("visual");
        preference.setKnowledgeLevel("advanced");
        preference.setTopicsOfInterest(new LinkedHashSet<>(Set.of("ia-generativa")));
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(any(UserPreferenceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String context = service.promptContext(userId, "Explique Ollama");

        assertThat(context)
                .contains("não privilegiado")
                .contains("visual")
                .contains("advanced")
                .contains("ia-generativa");
    }

    private UserEntity user(UUID id) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(id);
        return user;
    }
}
