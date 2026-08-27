package com.macedxs.mx.agent.skill.quality;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.application.SkillResult;
import com.macedxs.mx.conversation.application.ModelGenerationException;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QualitySkillTest {

    @Test
    void shouldExposeQualitySpecialistContractWithReadOnlyAutonomy() {
        QualitySkill skill = new QualitySkill(mock(ModelGateway.class));

        assertThat(skill.definition().name()).isEqualTo("quality");
        assertThat(skill.definition().description()).contains("Qualidade de software");
        assertThat(skill.definition().triggers()).contains("qualidade", "testes", "métricas", "cmmi");
        assertThat(skill.definition().allowedTools())
                .containsExactlyInAnyOrder("workspace.read_file", "workspace.list");
        assertThat(skill.definition().maximumAutonomy()).isEqualTo(AutonomyLevel.EXECUTE_READ_ONLY);
        assertThat(skill.definition().timeout()).isEqualTo(Duration.ofSeconds(90));
    }

    @Test
    void shouldUseQualitySystemPromptAndPreserveExecutionContext() {
        ModelGateway gateway = mock(ModelGateway.class);
        when(gateway.complete(argThat(request ->
                request.prompt().contains("QualitySkill")
                        && request.prompt().contains("McCall")
                        && request.prompt().contains("NISTIR 8397")
                        && request.prompt().contains("Avalie a cobertura dos testes")
        ))).thenReturn(new ModelGateway.ModelResponse("Diagnóstico de qualidade", "qwen", 18));

        UUID correlationId = UUID.randomUUID();
        QualitySkill skill = new QualitySkill(gateway);
        SkillResult result = skill.execute(
                new SkillRequest("Avalie a cobertura dos testes"),
                new SkillExecutionContext(UUID.randomUUID(), correlationId, AutonomyLevel.EXECUTE_READ_ONLY)
        );

        assertThat(result.skillName()).isEqualTo("quality");
        assertThat(result.answer())
                .startsWith("Diagnóstico de qualidade")
                .contains("Fontes documentais do MX:");
        assertThat(result.complete()).isTrue();
        assertThat(result.correlationId()).isEqualTo(correlationId);
        assertThat(result.metadata()).containsEntry("autonomy", "EXECUTE_READ_ONLY");
    }

    @Test
    void shouldAppendVersionedStudyCitationsForRagAndQualityRequests() {
        ModelGateway gateway = mock(ModelGateway.class);
        when(gateway.complete(org.mockito.ArgumentMatchers.any(ModelGateway.ModelRequest.class)))
                .thenReturn(new ModelGateway.ModelResponse("Diagnóstico fundamentado", "qwen", 18));
        QualitySkill skill = new QualitySkill(
                gateway,
                com.macedxs.mx.agent.skill.general.StudyKnowledgeContext.fromClasspath()
        );

        SkillResult result = skill.execute(
                new SkillRequest("Explique RAG documental e avaliação de qualidade."),
                new SkillExecutionContext(UUID.randomUUID(), UUID.randomUUID(), AutonomyLevel.EXECUTE_READ_ONLY)
        );

        assertThat(result.answer())
                .contains("Diagnóstico fundamentado")
                .contains("Citação:")
                .contains("versão=sha256:");
    }

    @Test
    void shouldRejectEmptyModelAnswer() {
        ModelGateway gateway = mock(ModelGateway.class);
        when(gateway.complete(org.mockito.ArgumentMatchers.any(ModelGateway.ModelRequest.class)))
                .thenReturn(new ModelGateway.ModelResponse(" ", "qwen", 10));

        QualitySkill skill = new QualitySkill(gateway);

        assertThatThrownBy(() -> skill.execute(
                new SkillRequest("Explique SQA"),
                new SkillExecutionContext(UUID.randomUUID(), UUID.randomUUID(), AutonomyLevel.RESPOND)
        ))
                .isInstanceOf(ModelGenerationException.class)
                .hasMessage("Quality skill returned an empty answer");
    }
}
