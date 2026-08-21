package com.macedxs.mx.agent.skill.development;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.Skill;
import com.macedxs.mx.agent.application.SkillDefinition;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.application.SkillResult;
import com.macedxs.mx.conversation.application.ModelGenerationException;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolExecutionResult;
import com.macedxs.mx.tool.application.ToolExecutor;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Converts a self-improvement request into a bounded, declarative job.
 * It never executes shell, Docker or Git commands inside the JVM.
 */
public final class SelfImprovementSkill implements Skill {

    private static final String SYSTEM_PROMPT = "Você é a capacidade interna de autoextensão do MX. " +
            "Converta a solicitação em uma mudança pequena, testável e reversível para criar um agent, skill ou tool. " +
            "Não invente resultados e não execute comandos. Gere somente uma chamada fenced com o marcador exato " +
            "[MX_TOOL_CALL] e [/MX_TOOL_CALL]. O JSON deve conter toolName self_extension.submit e arguments com " +
            "type (AGENT, SKILL ou TOOL), slug kebab-case, description, files (lista de path/content), " +
            "validations (somente maven_test, web_typecheck, web_export, compose_config, docker_build_core ou docker_build_web), " +
            "commitMessage e allowPush false. Use apenas caminhos dentro de core-service/src/main/, " +
            "core-service/src/test/, clients/mx-app/, skills/, scripts/, docs/ ou infrastructure/docker/. " +
            "Não inclua segredos, .env, .git, dados runtime, comandos shell ou conteúdo fora do JSON.\n\n";

    private final ModelGateway modelGateway;
    private final ToolExecutor toolExecutor;
    private final ToolCallParser toolCallParser;

    public SelfImprovementSkill(ModelGateway modelGateway, ToolExecutor toolExecutor) {
        this(modelGateway, toolExecutor, new ObjectMapper());
    }

    public SelfImprovementSkill(ModelGateway modelGateway, ToolExecutor toolExecutor, ObjectMapper objectMapper) {
        if (modelGateway == null || toolExecutor == null || objectMapper == null) {
            throw new IllegalArgumentException("Self-improvement dependencies are required");
        }
        this.modelGateway = modelGateway;
        this.toolExecutor = toolExecutor;
        this.toolCallParser = new ToolCallParser(objectMapper);
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "self-improvement",
                "1.0.0",
                "Criação controlada de agents, skills e tools com fila, testes, Docker e Git auditáveis",
                Set.of("criar agent", "criar agente", "criar skill", "criar ferramenta", "auto implementar",
                        "autoimplementar", "melhorar o mx", "self improve", "new agent", "new skill"),
                Set.of("self_extension.submit"),
                AutonomyLevel.EXECUTE_AUTONOMOUSLY,
                Duration.ofMinutes(5)
        );
    }

    @Override
    public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
        ModelGateway.ModelResponse response = modelGateway.complete(
                new ModelGateway.ModelRequest(context.userId(), SYSTEM_PROMPT + "Solicitação:\n" + request.prompt())
        );
        return resolve(context, response);
    }

    @Override
    public SkillResult stream(SkillRequest request, SkillExecutionContext context, Consumer<String> chunkConsumer) {
        return Skill.super.stream(request, context, chunkConsumer);
    }

    private SkillResult resolve(SkillExecutionContext context, ModelGateway.ModelResponse response) {
        validateResponse(response);
        return toolCallParser.parse(response.answer())
                .map(call -> submit(context, call))
                .orElseGet(() -> new SkillResult(
                        definition().name(),
                        "A autoextensão não gerou um plano estruturado; nenhuma alteração foi enfileirada.",
                        true,
                        context.correlationId(),
                        Map.of("planRejected", "STRUCTURED_PLAN_REQUIRED")
                ));
    }

    private SkillResult submit(SkillExecutionContext context, ToolCallParser.ParsedToolCall call) {
        ToolExecutionResult execution = toolExecutor.execute(
                new ToolRequest(call.toolName(), call.arguments()),
                new ToolExecutionContext(
                        context.userId(),
                        context.correlationId(),
                        definition().name(),
                        context.grantedAutonomy()
                ),
                false
        );
        if (execution.toolResult() == null || !execution.toolResult().success()) {
            String reason = execution.policyDecision() == null
                    ? "SELF_EXTENSION_NOT_QUEUED"
                    : execution.policyDecision().reason();
            return new SkillResult(
                    definition().name(),
                    "A proposta de autoextensão foi recusada pela política: " + reason,
                    true,
                    context.correlationId(),
                    Map.of("selfExtensionRejected", reason)
            );
        }
        ToolResult result = execution.toolResult();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("selfExtensionQueued", true);
        metadata.putAll(result.output());
        return new SkillResult(
                definition().name(),
                "Plano de autoextensão enfileirado com segurança. O runner local executará as validações declaradas, fará rollback em caso de falha e criará um commit somente se tudo passar.",
                true,
                context.correlationId(),
                metadata
        );
    }

    private void validateResponse(ModelGateway.ModelResponse response) {
        if (response == null || response.answer() == null || response.answer().isBlank()) {
            throw new ModelGenerationException("Self-improvement skill returned an empty plan");
        }
    }
}
