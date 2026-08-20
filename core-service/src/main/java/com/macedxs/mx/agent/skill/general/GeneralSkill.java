package com.macedxs.mx.agent.skill.general;

import com.macedxs.mx.ai.service.OllamaService;
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

public class GeneralSkill implements Skill {

    private ModelGateway modelGateway;

    public GeneralSkill(ModelGateway modelGateway) {
        this.modelGateway = modelGateway;
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "general",
                "1.0.0",
                "Conversação geral e esclarecimento de solicitações",
                Set.of(),
                Set.of(),
                AutonomyLevel.RESPOND,
                Duration.ofSeconds(45)
        );
    }

    @Override
    public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
        long startedAt = System.nanoTime();
        ModelGateway.ModelResponse response = modelGateway.complete(new ModelGateway.ModelRequest(
                "Você é o MX, um assistente pessoal cuidadoso. Responda em português brasileiro. " +
                        "Se a solicitação estiver ambígua, faça uma pergunta curta antes de assumir.\n\n" +
                        "Solicitação do usuário:\n" + request.prompt()
        ));

        if (response == null || response.answer() == null || response.answer().isBlank()) {
            throw new ModelGenerationException("General skill returned an empty answer");
        }

        return new SkillResult(
                definition().name(),
                response.answer().trim(),
                true,
                context.correlationId(),
                Map.of(
                        "model", response.model() == null ? "unknown" : response.model(),
                        "durationMs", response.durationMs(),
                        "skillDurationMs", (System.nanoTime() - startedAt) / 1_000_000
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

        long startedAt = System.nanoTime();
        ModelGateway.ModelResponse response = streamingModelGateway.stream(
                new ModelGateway.ModelRequest(context.userId(), buildPrompt(request.prompt())),
                chunkConsumer
        );
        if (response == null || response.answer() == null || response.answer().isBlank()) {
            throw new ModelGenerationException("General skill returned an empty answer");
        }

        return new SkillResult(
                definition().name(),
                response.answer().trim(),
                true,
                context.correlationId(),
                Map.of(
                        "model", response.model() == null ? "unknown" : response.model(),
                        "durationMs", response.durationMs(),
                        "skillDurationMs", (System.nanoTime() - startedAt) / 1_000_000
                )
        );
    }

    private String buildPrompt(String prompt) {
        return "Você é o MX, um assistente pessoal cuidadoso. Responda em português brasileiro. " +
                "Se a solicitação estiver ambígua, faça uma pergunta curta antes de assumir.\n\n" +
                "Solicitação do usuário:\n" + prompt;
    }
}
