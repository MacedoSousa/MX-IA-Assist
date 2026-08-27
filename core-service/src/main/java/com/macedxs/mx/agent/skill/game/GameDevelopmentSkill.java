package com.macedxs.mx.agent.skill.game;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillDefinition;
import com.macedxs.mx.agent.skill.general.StudyKnowledgeContext;
import com.macedxs.mx.agent.skill.specialist.StudySpecialistSkill;
import com.macedxs.mx.conversation.application.port.ModelGateway;

import java.time.Duration;
import java.util.Set;

/**
 * Design: capacidade interna do MX para transformar um pedido de jogo em um plano
 * verificável. A skill não publica, não executa comandos livres e não inventa um build.
 */
public final class GameDevelopmentSkill extends StudySpecialistSkill {

    public GameDevelopmentSkill(ModelGateway modelGateway, StudyKnowledgeContext studyKnowledgeContext) {
        super(modelGateway, studyKnowledgeContext);
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "game-development",
                "1.0.0",
                "Concepção e desenvolvimento de jogos com escopo, loop central, arquitetura e validação local",
                Set.of("jogo", "jogos", "game", "gamedev", "godot", "unity", "unreal", "phaser", "babylon"),
                Set.of(),
                AutonomyLevel.RESPOND,
                Duration.ofSeconds(90)
        );
    }

    @Override
    protected String specialistGuidance() {
        return "Para cada solicitação, diferencie claramente objetivo do jogo, público, plataforma, loop principal, " +
                "mecânicas, regras de vitória ou progresso, direção visual e sonora, modelo de dados, arquitetura, " +
                "marcos de entrega e testes. Prefira um vertical slice jogável a promessas amplas. Quando a tecnologia " +
                "não estiver definida, ofereça no máximo duas alternativas justificadas pelas restrições fornecidas. " +
                "Não afirme que criou arquivos, assets, prévias ou builds sem resultado confirmado por uma ferramenta do MX. " +
                "Quando houver um workspace autorizado, descreva primeiro a receita de prévia e as validações que serão necessárias.";
    }
}
