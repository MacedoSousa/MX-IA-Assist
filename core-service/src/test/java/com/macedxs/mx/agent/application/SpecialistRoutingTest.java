package com.macedxs.mx.agent.application;

import com.macedxs.mx.agent.skill.data.DataSkill;
import com.macedxs.mx.agent.skill.general.StudyKnowledgeContext;
import com.macedxs.mx.agent.skill.infrastructure.InfrastructureSkill;
import com.macedxs.mx.agent.skill.teaching.TeachingSkill;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialistRoutingTest {

    private final ModelGateway gateway = request -> new ModelGateway.ModelResponse("ok", "test", 1);
    private final StudyKnowledgeContext knowledge = new StudyKnowledgeContext("Resumo local");

    @Test
    void shouldRouteInfrastructurePrompts() {
        assertThat(router().route("Como ajustar Docker, Ollama e Tailscale?").skill().definition().name())
                .isEqualTo("infrastructure");
    }

    @Test
    void shouldRouteDataPrompts() {
        assertThat(router().route("Como organizar meu pipeline de machine learning e dataset?").skill().definition().name())
                .isEqualTo("data");
    }

    @Test
    void shouldRouteTeachingPrompts() {
        assertThat(router().route("Crie um exercício de ensino sobre testes").skill().definition().name())
                .isEqualTo("teaching");
    }

    private SkillRouter router() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new NamedFallbackSkill());
        registry.register(new InfrastructureSkill(gateway, knowledge));
        registry.register(new DataSkill(gateway, knowledge));
        registry.register(new TeachingSkill(gateway, knowledge));
        return new SkillRouter(registry);
    }

    private static final class NamedFallbackSkill implements Skill {
        @Override
        public SkillDefinition definition() {
            return new SkillDefinition("general", "1.0.0", "fallback", java.util.Set.of(), java.util.Set.of(), AutonomyLevel.RESPOND, java.time.Duration.ofSeconds(30));
        }

        @Override
        public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
            return SkillResult.completed("general", "ok", context.correlationId());
        }
    }
}
