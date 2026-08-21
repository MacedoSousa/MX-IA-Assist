package com.macedxs.mx.agent.skill.specialist;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.skill.data.DataSkill;
import com.macedxs.mx.agent.skill.infrastructure.InfrastructureSkill;
import com.macedxs.mx.agent.skill.teaching.TeachingSkill;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainSpecialistSkillTest {

    @Test
    void shouldExposeInfrastructureContractAndUseLocalKnowledge() {
        CapturingGateway gateway = new CapturingGateway();
        InfrastructureSkill skill = new InfrastructureSkill(gateway, new com.macedxs.mx.agent.skill.general.StudyKnowledgeContext("Resumo local"));

        var result = skill.execute(request("Como melhorar o Docker e o Ollama?"), context());

        assertThat(skill.definition().name()).isEqualTo("infrastructure");
        assertThat(skill.definition().triggers()).contains("docker", "ollama", "tailscale");
        assertThat(gateway.lastPrompt).contains("Docker Compose", "Ollama", "Solicitação do usuário");
        assertThat(result.skillName()).isEqualTo("infrastructure");
        assertThat(result.metadata()).containsEntry("knowledgeContext", "local-classpath");
    }

    @Test
    void shouldExposeDataContract() {
        DataSkill skill = new DataSkill(new CapturingGateway(), new com.macedxs.mx.agent.skill.general.StudyKnowledgeContext("Resumo local"));

        assertThat(skill.definition().name()).isEqualTo("data");
        assertThat(skill.definition().triggers()).contains("machine learning", "mlops", "dataset");
    }

    @Test
    void shouldExposeTeachingContractAndAdaptLanguage() {
        TeachingSkill skill = new TeachingSkill(new CapturingGateway(), new com.macedxs.mx.agent.skill.general.StudyKnowledgeContext("Resumo local"));

        assertThat(skill.definition().name()).isEqualTo("teaching");
        assertThat(skill.definition().triggers()).contains("ensino", "teaching", "didática");
        assertThat(skill.definition().description()).contains("aprendizagem adaptativa");
    }

    private static SkillRequest request(String prompt) {
        return new SkillRequest(prompt);
    }

    private static SkillExecutionContext context() {
        return new SkillExecutionContext(UUID.randomUUID(), UUID.randomUUID(), AutonomyLevel.RESPOND);
    }

    private static final class CapturingGateway implements ModelGateway {
        private String lastPrompt;

        @Override
        public ModelResponse complete(ModelRequest request) {
            lastPrompt = request.prompt();
            return new ModelResponse("Resposta especializada", "test-model", 5);
        }
    }
}
