package com.macedxs.mx.agent.skill.teaching;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillDefinition;
import com.macedxs.mx.agent.skill.general.StudyKnowledgeContext;
import com.macedxs.mx.agent.skill.specialist.StudySpecialistSkill;
import com.macedxs.mx.conversation.application.port.ModelGateway;

import java.time.Duration;
import java.util.Set;

public class TeachingSkill extends StudySpecialistSkill {

    public TeachingSkill(ModelGateway modelGateway) {
        this(modelGateway, StudyKnowledgeContext.fromClasspath());
    }

    public TeachingSkill(ModelGateway modelGateway, StudyKnowledgeContext studyKnowledgeContext) {
        super(modelGateway, studyKnowledgeContext);
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "teaching",
                "1.0.0",
                "Ensino, didática, exercícios, avaliação e aprendizagem adaptativa",
                Set.of(
                        "ensino", "teaching", "didática", "didatica", "aula", "lesson", "exercício", "exercicio",
                        "exercise", "avaliação", "avaliacao", "assessment", "aprendizagem", "aprendizado", "study",
                        "estudo", "curso", "course", "explicar", "explicação", "explicacao"
                ),
                Set.of(),
                AutonomyLevel.RESPOND,
                Duration.ofSeconds(90)
        );
    }

    @Override
    protected String specialistGuidance() {
        return "Atue como especialista em ensino personalizado. Descubra ou infira com cautela objetivo, nível, " +
                "ritmo e estilo do estudante; explique do simples ao complexo; use exemplos, analogias, perguntas " +
                "de verificação, exercícios graduais, feedback acionável e critérios de avaliação. Adapte o tom " +
                "a sinais de formalidade, coloquialidade e vocabulário do usuário sem caricaturar gírias. Quando " +
                "o idioma alternar entre português e inglês, acompanhe a preferência mais recente e preserve " +
                "termos técnicos úteis nos dois idiomas.";
    }
}
