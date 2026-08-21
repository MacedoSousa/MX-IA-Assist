package com.macedxs.mx.tool.evolution;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.evolution.application.SelfExtensionService;
import com.macedxs.mx.tool.application.Tool;
import com.macedxs.mx.tool.application.ToolDefinition;
import com.macedxs.mx.tool.application.ToolEffect;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

public final class SelfExtensionSubmitTool implements Tool {

    private final SelfExtensionService service;
    private final ToolDefinition definition = new ToolDefinition(
            "self_extension.submit",
            "1.0.0",
            "Enfileira um plano validado para criar ou alterar agent, skill ou tool",
            ToolEffect.SELF_MODIFICATION,
            AutonomyLevel.EXECUTE_AUTONOMOUSLY,
            Duration.ofSeconds(30),
            Set.of("type", "slug", "description", "files", "validations", "commitMessage")
    );

    public SelfExtensionSubmitTool(SelfExtensionService service) {
        if (service == null) {
            throw new IllegalArgumentException("Self-extension service is required");
        }
        this.service = service;
    }

    @Override
    public ToolDefinition definition() {
        return definition;
    }

    @Override
    public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
        if (request == null || context == null) {
            throw new IllegalArgumentException("Self-extension tool request and context are required");
        }
        if (context.grantedAutonomy() != AutonomyLevel.EXECUTE_AUTONOMOUSLY) {
            return ToolResult.failure(definition.name(), context.correlationId(), "SELF_EXTENSION_REQUIRES_APPROVAL_AUTONOMY");
        }
        try {
            SelfExtensionService.SubmissionResult result = service.submit(
                    context.userId(),
                    context.correlationId(),
                    request.arguments()
            );
            return ToolResult.success(definition.name(), context.correlationId(), result.asMap());
        } catch (IllegalArgumentException rejected) {
            return ToolResult.failure(definition.name(), context.correlationId(), "SELF_EXTENSION_REJECTED: " + rejected.getMessage());
        } catch (RuntimeException failure) {
            return ToolResult.failure(definition.name(), context.correlationId(), "SELF_EXTENSION_PERSISTENCE_FAILED");
        }
    }
}
