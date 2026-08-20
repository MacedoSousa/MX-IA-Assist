package com.macedxs.mx.core.application;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.Skill;
import com.macedxs.mx.agent.application.SkillDefinition;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRegistry;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.application.SkillResult;
import com.macedxs.mx.agent.application.SkillRouter;
import com.macedxs.mx.core.application.run.ExecutionRunSnapshot;
import com.macedxs.mx.core.application.run.ExecutionRunStore;
import com.macedxs.mx.core.application.run.RunStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MxCoreServiceTest {

    @Test
    void shouldUseTheSelectedSpecialistAndReturnOneCentralResponse() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of(), "Resposta geral"));
        registry.register(skill("development", Set.of("java", "bug"), "Diagnóstico técnico"));

        MxCoreResponse response = new MxCoreService(new SkillRouter(registry))
                .handle(UUID.randomUUID(), "Tenho um bug em Java");

        assertThat(response.skillName()).isEqualTo("development");
        assertThat(response.answer()).isEqualTo("Diagnóstico técnico");
        assertThat(response.correlationId()).isNotNull();
        assertThat(response.confidence()).isGreaterThan(0.0d);
    }

    @Test
    void shouldPersistCompletedRunAndExposeItsId() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of(), "Resposta geral"));
        List<ExecutionRunSnapshot> snapshots = new ArrayList<>();
        ExecutionRunStore store = new CapturingRunStore(snapshots);
        UUID userId = UUID.randomUUID();

        MxCoreResponse response = new MxCoreService(new SkillRouter(registry), SkillTelemetry.noop(), store)
                .handle(userId, "Ajude-me a organizar minhas ideias");

        assertThat(response.runId()).isNotNull();
        assertThat(snapshots).hasSize(5);
        assertThat(snapshots).allMatch(snapshot -> snapshot.runId().equals(response.runId()));
        assertThat(snapshots.get(0).status()).isEqualTo(RunStatus.RECEIVED);
        assertThat(snapshots.get(1).status()).isEqualTo(RunStatus.ROUTED);
        assertThat(snapshots.get(2).status()).isEqualTo(RunStatus.EXECUTING);
        assertThat(snapshots.get(3).status()).isEqualTo(RunStatus.VERIFYING);
        assertThat(snapshots.get(4).status()).isEqualTo(RunStatus.COMPLETED);
        assertThat(snapshots.get(4).userId()).isEqualTo(userId);
        assertThat(snapshots.get(4).output()).isEqualTo("Resposta geral");
    }

    @Test
    void shouldPersistFailedRunWhenSkillExecutionFails() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(failingSkill("general"));
        List<ExecutionRunSnapshot> snapshots = new ArrayList<>();
        ExecutionRunStore store = new CapturingRunStore(snapshots);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                new MxCoreService(new SkillRouter(registry), SkillTelemetry.noop(), store)
                        .handle(UUID.randomUUID(), "Falhe de propósito"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(snapshots).hasSize(4);
        assertThat(snapshots.get(3).status()).isEqualTo(RunStatus.FAILED);
        assertThat(snapshots.get(3).errorCode()).isEqualTo("SKILL_EXECUTION_FAILED");
    }

    @Test
    void shouldStreamSelectedSkillAndPersistCompletedRun() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(streamingSkill("general", Set.of(), "Resposta em streaming"));
        List<ExecutionRunSnapshot> snapshots = new ArrayList<>();
        List<String> chunks = new ArrayList<>();
        UUID userId = UUID.randomUUID();

        MxCoreResponse response = new MxCoreService(
                new SkillRouter(registry),
                SkillTelemetry.noop(),
                new CapturingRunStore(snapshots)
        ).handleStreaming(userId, "Mostre em partes", chunks::add);

        assertThat(response.answer()).isEqualTo("Resposta em streaming");
        assertThat(chunks).containsExactly("Resposta ", "em streaming");
        assertThat(response.runId()).isNotNull();
        assertThat(snapshots).hasSize(5);
        assertThat(snapshots.get(4).status()).isEqualTo(RunStatus.COMPLETED);
        assertThat(snapshots.get(4).runId()).isEqualTo(response.runId());
    }

    @Test
    void shouldKeepGeneralConversationAsCentralFallback() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of(), "Posso ajudar a organizar isso."));

        MxCoreResponse response = new MxCoreService(new SkillRouter(registry))
                .handle(UUID.randomUUID(), "Ajude-me a organizar minhas ideias");

        assertThat(response.skillName()).isEqualTo("general");
        assertThat(response.answer()).isEqualTo("Posso ajudar a organizar isso.");
    }

    private static Skill streamingSkill(String name, Set<String> triggers, String answer) {
        return new Skill() {
            @Override
            public SkillDefinition definition() {
                return new SkillDefinition(
                        name,
                        "1.0.0",
                        "Especialista " + name,
                        triggers,
                        Set.of(),
                        AutonomyLevel.RESPOND,
                        Duration.ofSeconds(30)
                );
            }

            @Override
            public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
                return SkillResult.completed(name, answer, context.correlationId());
            }

            @Override
            public SkillResult stream(
                    SkillRequest request,
                    SkillExecutionContext context,
                    java.util.function.Consumer<String> chunkConsumer
            ) {
                chunkConsumer.accept("Resposta ");
                chunkConsumer.accept("em streaming");
                return SkillResult.completed(name, answer, context.correlationId());
            }
        };
    }

    private static Skill failingSkill(String name) {
        return new Skill() {
            @Override
            public SkillDefinition definition() {
                return new SkillDefinition(
                        name,
                        "1.0.0",
                        "Especialista " + name,
                        Set.of(),
                        Set.of(),
                        AutonomyLevel.RESPOND,
                        Duration.ofSeconds(30)
                );
            }

            @Override
            public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
                throw new IllegalStateException("skill failure");
            }
        };
    }

    private static Skill skill(String name, Set<String> triggers, String answer) {
        return new Skill() {
            @Override
            public SkillDefinition definition() {
                return new SkillDefinition(
                        name,
                        "1.0.0",
                        "Especialista " + name,
                        triggers,
                        Set.of(),
                        AutonomyLevel.RESPOND,
                        Duration.ofSeconds(30)
                );
            }

            @Override
            public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
                return SkillResult.completed(name, answer, context.correlationId());
            }
        };
    }

    private record CapturingRunStore(List<ExecutionRunSnapshot> snapshots) implements ExecutionRunStore {
        @Override
        public ExecutionRunSnapshot save(com.macedxs.mx.core.application.run.ExecutionRun run) {
            ExecutionRunSnapshot snapshot = run.snapshot();
            snapshots.add(snapshot);
            return snapshot;
        }

        @Override
        public Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId) {
            return snapshots.stream()
                    .filter(snapshot -> snapshot.userId().equals(userId) && snapshot.runId().equals(runId))
                    .reduce((first, second) -> second);
        }
    }
}
