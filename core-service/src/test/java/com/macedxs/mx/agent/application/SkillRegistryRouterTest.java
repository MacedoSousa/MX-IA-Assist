package com.macedxs.mx.agent.application;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillRegistryRouterTest {

    @Test
    void shouldRegisterSkillsAndRouteDevelopmentRequestsToDevelopmentSkill() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of()));
        registry.register(skill("development", Set.of("bug", "java", "spring", "código")));

        SkillRouter router = new SkillRouter(registry);
        RouteDecision decision = router.route("Preciso corrigir um bug no Spring");

        assertThat(decision.skill().definition().name()).isEqualTo("development");
        assertThat(decision.confidence()).isGreaterThanOrEqualTo(0.75d);
        assertThat(decision.requiresClarification()).isFalse();
    }

    @Test
    void shouldRouteQualityRequestsToQualitySkill() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of()));
        registry.register(skill("development", Set.of("bug", "java", "spring", "código")));
        registry.register(skill("quality", Set.of("qualidade", "testes", "métricas", "cmmi")));

        RouteDecision decision = new SkillRouter(registry).route("Avalie a qualidade e a cobertura dos testes");

        assertThat(decision.skill().definition().name()).isEqualTo("quality");
        assertThat(decision.confidence()).isGreaterThan(0.0d);
        assertThat(decision.requiresClarification()).isFalse();
    }

    @Test
    void shouldRouteGameRequestsToGameDevelopmentSkill() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of()));
        registry.register(skill("game-development", Set.of("jogo", "godot", "unity", "gamedev")));

        RouteDecision decision = new SkillRouter(registry).route("Quero criar um jogo de estratégia no Godot");

        assertThat(decision.skill().definition().name()).isEqualTo("game-development");
        assertThat(decision.requiresClarification()).isFalse();
    }

    @Test
    void shouldUseGeneralSkillWhenNoSpecialistMatches() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of()));
        registry.register(skill("development", Set.of("bug", "java", "spring", "código")));

        RouteDecision decision = new SkillRouter(registry).route("Quero organizar minhas ideias");

        assertThat(decision.skill().definition().name()).isEqualTo("general");
        assertThat(decision.confidence()).isEqualTo(0.0d);
        assertThat(decision.requiresClarification()).isFalse();
    }

    @Test
    void shouldRouteExplicitImageGenerationRequestToMediaSkill() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of()));
        registry.register(skill("media", Set.of("imagem", "video", "documento")));

        RouteDecision decision = new SkillRouter(registry).route("Gere um hamster em um carro, imagem");

        assertThat(decision.skill().definition().name()).isEqualTo("media");
        assertThat(decision.confidence()).isEqualTo(1.0d);
        assertThat(decision.requiresClarification()).isFalse();
    }

    @Test
    void shouldNotRouteInternalDocumentRenderPromptBackToMediaSkill() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("general", Set.of()));
        registry.register(skill("media", Set.of("imagem", "video", "documento")));

        RouteDecision decision = new SkillRouter(registry).route("[MX_DOCUMENT_OUTPUT] Gere o corpo do documento");

        assertThat(decision.skill().definition().name()).isEqualTo("general");
    }

    @Test
    void shouldRejectDuplicateSkillNames() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(skill("development", Set.of("java")));

        assertThatThrownBy(() -> registry.register(skill("development", Set.of("spring"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Skill already registered: development");
    }

    @Test
    void shouldExecuteARegisteredSkillWithCentralContext() {
        Skill development = new Skill() {
            @Override
            public SkillDefinition definition() {
                return skillDefinition("development", Set.of("java"));
            }

            @Override
            public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
                return SkillResult.completed(definition().name(), "Plano técnico", context.correlationId());
            }
        };

        SkillRegistry registry = new SkillRegistry();
        registry.register(development);

        UUID correlationId = UUID.randomUUID();
        SkillResult result = registry.getRequired("development")
                .execute(new SkillRequest("Analise meu código"),
                        new SkillExecutionContext(UUID.randomUUID(), correlationId, AutonomyLevel.RESPOND));

        assertThat(result.skillName()).isEqualTo("development");
        assertThat(result.answer()).isEqualTo("Plano técnico");
        assertThat(result.correlationId()).isEqualTo(correlationId);
    }

    private static Skill skill(String name, Set<String> triggers) {
        return new Skill() {
            @Override
            public SkillDefinition definition() {
                return skillDefinition(name, triggers);
            }

            @Override
            public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
                return SkillResult.completed(name, "ok", context.correlationId());
            }
        };
    }

    private static SkillDefinition skillDefinition(String name, Set<String> triggers) {
        return new SkillDefinition(
                name,
                "1.0.0",
                name + " specialist",
                triggers,
                Set.of(),
                AutonomyLevel.RESPOND,
                Duration.ofSeconds(30)
        );
    }
}
