package com.macedxs.mx.agent.skill.data;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillDefinition;
import com.macedxs.mx.agent.skill.general.StudyKnowledgeContext;
import com.macedxs.mx.agent.skill.specialist.StudySpecialistSkill;
import com.macedxs.mx.conversation.application.port.ModelGateway;

import java.time.Duration;
import java.util.Set;

public class DataSkill extends StudySpecialistSkill {

    public DataSkill(ModelGateway modelGateway) {
        this(modelGateway, StudyKnowledgeContext.fromClasspath());
    }

    public DataSkill(ModelGateway modelGateway, StudyKnowledgeContext studyKnowledgeContext) {
        super(modelGateway, studyKnowledgeContext);
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "data",
                "1.0.0",
                "Dados, machine learning, MLOps, pipelines e avaliação de modelos",
                Set.of(
                        "dados", "data", "machine learning", "mlops", "pipeline", "pipelines", "dataset",
                        "datasets", "etl", "feature", "features", "estatística", "estatistica", "analytics",
                        "modelo preditivo", "modelagem", "modeling", "treinamento", "training"
                ),
                Set.of(),
                AutonomyLevel.RESPOND,
                Duration.ofSeconds(90)
        );
    }

    @Override
    protected String specialistGuidance() {
        return "Atue como especialista em engenharia de dados e machine learning. Estruture o raciocínio em " +
                "objetivo, dados e qualidade, preparação, baseline, validação, métricas, risco de leakage, " +
                "reprodutibilidade, monitoramento de drift e ciclo MLOps. Diferencie explicação didática de " +
                "recomendação operacional e não afirme desempenho de modelo sem dados de avaliação. Para o MX, " +
                "prefira soluções locais, auditáveis, leves e compatíveis com CPU quando GPU não estiver disponível.";
    }
}
