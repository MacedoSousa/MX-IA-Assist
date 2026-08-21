package com.macedxs.mx.agent.skill.infrastructure;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillDefinition;
import com.macedxs.mx.agent.skill.general.StudyKnowledgeContext;
import com.macedxs.mx.agent.skill.specialist.StudySpecialistSkill;
import com.macedxs.mx.conversation.application.port.ModelGateway;

import java.time.Duration;
import java.util.Set;

public class InfrastructureSkill extends StudySpecialistSkill {

    public InfrastructureSkill(ModelGateway modelGateway) {
        this(modelGateway, StudyKnowledgeContext.fromClasspath());
    }

    public InfrastructureSkill(ModelGateway modelGateway, StudyKnowledgeContext studyKnowledgeContext) {
        super(modelGateway, studyKnowledgeContext);
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "infrastructure",
                "1.0.0",
                "Infraestrutura local, Docker, segurança, rede, deploy, Ollama e desempenho",
                Set.of(
                        "infraestrutura", "infrastructure", "docker", "container", "containers", "compose",
                        "segurança", "seguranca", "security", "rede", "network", "tailscale", "deploy", "linux",
                        "ollama", "desempenho", "performance", "escalonamento", "autoscaling"
                ),
                Set.of(),
                AutonomyLevel.RESPOND,
                Duration.ofSeconds(90)
        );
    }

    @Override
    protected String specialistGuidance() {
        return "Analise arquitetura e operação local com foco em Docker Compose, limites de CPU e memória, " +
                "health checks, restart policies, persistência, isolamento de portas, Tailscale, allowlists, " +
                "Ollama, latência, concorrência e observabilidade. Explique quando autoscaling real não existe " +
                "em Docker Desktop local e substitua a promessa por limites, filas, cache, backpressure e " +
                "reinício automático verificável. Nunca recomende expor serviços diretamente à internet sem " +
                "autenticação, criptografia e controle de rede.";
    }
}
