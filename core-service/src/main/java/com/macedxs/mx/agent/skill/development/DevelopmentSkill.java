package com.macedxs.mx.agent.skill.development;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.Skill;
import com.macedxs.mx.agent.application.SkillDefinition;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.application.SkillResult;
import com.macedxs.mx.conversation.application.ModelGenerationException;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.conversation.application.port.StreamingModelGateway;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DevelopmentSkill implements Skill {

    private final ModelGateway modelGateway;

    public DevelopmentSkill(ModelGateway modelGateway) {
        this.modelGateway = modelGateway;
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "development",
                "1.0.0",
                "Desenvolvimento fullstack, arquitetura, debugging e testes",
                Set.of("código", "codigo", "bug", "erro", "java", "spring", "maven", "teste", "arquitetura", "programar"),
                Set.of("workspace.read_file", "workspace.list", "git.status"),
                AutonomyLevel.EXECUTE_READ_ONLY,
                Duration.ofSeconds(90)
        );
    }

    @Override
    public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
        ModelGateway.ModelResponse response = modelGateway.complete(new ModelGateway.ModelRequest(
                "Você é a skill especialista em desenvolvimento do MX. " +
                        "Analise problemas como um desenvolvedor fullstack sênior e PO técnico. " +
                        "Priorize diagnóstico verificável, Clean Architecture, SOLID, TDD, segurança e simplicidade. " +
                        "Não invente arquivos, testes ou resultados. Quando faltar contexto, peça somente o necessário. " +
                        "Não execute ações de escrita: apenas proponha ou explique até o MX conceder autorização explícita.\n\n" +
                        "Solicitação:\n" + request.prompt()
        ));

        if (response == null || response.answer() == null || response.answer().isBlank()) {
            throw new ModelGenerationException("Development skill returned an empty answer");
        }

        return new SkillResult(
                definition().name(),
                response.answer().trim(),
                true,
                context.correlationId(),
                Map.of(
                        "model", response.model() == null ? "unknown" : response.model(),
                        "durationMs", response.durationMs(),
                        "autonomy", context.grantedAutonomy().name()
                )
        );
    }

    @Override
    public SkillResult stream(
            SkillRequest request,
            SkillExecutionContext context,
            Consumer<String> chunkConsumer
    ) {
        if (!(modelGateway instanceof StreamingModelGateway streamingModelGateway)) {
            return Skill.super.stream(request, context, chunkConsumer);
        }

        ModelGateway.ModelResponse response = streamingModelGateway.stream(
                new ModelGateway.ModelRequest(context.userId(), buildPrompt(request.prompt())),
                chunkConsumer
        );
        if (response == null || response.answer() == null || response.answer().isBlank()) {
            throw new ModelGenerationException("Development skill returned an empty answer");
        }

        return new SkillResult(
                definition().name(),
                response.answer().trim(),
                true,
                context.correlationId(),
                Map.of(
                        "model", response.model() == null ? "unknown" : response.model(),
                        "durationMs", response.durationMs(),
                        "autonomy", context.grantedAutonomy().name()
                )
        );
    }

    private String buildPrompt(String prompt) {
        return "Você é a skill especialista em desenvolvimento do MX. " +
                "Analise problemas como um desenvolvedor fullstack sênior e PO técnico. " +
                "Priorize diagnóstico verificável, Clean Architecture, SOLID, TDD, segurança e simplicidade. " +
                "Não invente arquivos, testes ou resultados. Quando faltar contexto, peça somente o necessário. " +
                "Não execute ações de escrita: apenas proponha ou explique até o MX conceder autorização explícita.\n\n" +
                "Solicitação:\n" + prompt;
    }
}
