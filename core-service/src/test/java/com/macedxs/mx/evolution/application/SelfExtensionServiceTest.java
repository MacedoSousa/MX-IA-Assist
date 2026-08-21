package com.macedxs.mx.evolution.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelfExtensionServiceTest {

    @Test
    void queuesValidatedStructuredSubmissionWithoutPromptText() {
        InMemoryStore store = new InMemoryStore();
        SelfExtensionService service = new SelfExtensionService(
                new ObjectMapper(),
                new SelfExtensionPolicy(),
                store
        );
        UUID correlationId = UUID.randomUUID();

        SelfExtensionService.SubmissionResult result = service.submit(
                UUID.randomUUID(),
                correlationId,
                Map.of(
                        "type", "SKILL",
                        "slug", "safe-skill",
                        "description", "Skill criada por plano declarativo",
                        "files", List.of(Map.of(
                                "path", "skills/safe/Skill.java",
                                "content", "package skills.safe;"
                        )),
                        "validations", List.of("maven_test"),
                        "commitMessage", "feat(mx): add safe skill",
                        "allowPush", false
                )
        );

        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.files()).containsExactly("skills/safe/Skill.java");
        assertThat(store.saved).isNotNull();
        assertThat(store.saved.correlationId()).isEqualTo(correlationId);
        assertThat(store.saved.submission().files().getFirst().path()).isEqualTo("skills/safe/Skill.java");
    }

    @Test
    void rejectsUnknownToolArgumentsBeforePersisting() {
        InMemoryStore store = new InMemoryStore();
        SelfExtensionService service = new SelfExtensionService(
                new ObjectMapper(),
                new SelfExtensionPolicy(),
                store
        );

        assertThatThrownBy(() -> service.submit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Map.of(
                        "type", "SKILL",
                        "slug", "unsafe",
                        "description", "Plano",
                        "files", List.of(Map.of("path", "/tmp/run.sh", "content", "rm -rf /")),
                        "validations", List.of("maven_test"),
                        "commitMessage", "feat(mx): unsafe"
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsafe");

        assertThat(store.saved).isNull();
    }

    private static final class InMemoryStore implements SelfExtensionJobStore {
        private SelfExtensionJob saved;

        @Override
        public SelfExtensionJob save(SelfExtensionJob job) {
            this.saved = job;
            return job;
        }
    }
}
