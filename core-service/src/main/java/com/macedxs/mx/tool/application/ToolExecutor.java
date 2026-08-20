package com.macedxs.mx.tool.application;

import com.macedxs.mx.agent.application.AutonomyLevel;

public class ToolExecutor {

    private final ToolRegistry registry;
    private final PolicyEngine policyEngine;

    public ToolExecutor(ToolRegistry registry, PolicyEngine policyEngine) {
        if (registry == null) {
            throw new IllegalArgumentException("Tool registry is required");
        }
        if (policyEngine == null) {
            throw new IllegalArgumentException("Policy engine is required");
        }
        this.registry = registry;
        this.policyEngine = policyEngine;
    }

    public ToolExecutionResult execute(
            ToolRequest request,
            ToolExecutionContext context,
            boolean approved
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Tool request is required");
        }
        if (context == null) {
            throw new IllegalArgumentException("Tool execution context is required");
        }

        Tool tool = registry.getRequired(request.toolName());
        PolicyDecision decision = policyEngine.evaluate(
                tool.definition(),
                request,
                context.grantedAutonomy(),
                approved
        );

        if (decision.outcome() != PolicyOutcome.ALLOW) {
            return new ToolExecutionResult(decision, null);
        }

        ToolResult result = tool.execute(request, context);
        return new ToolExecutionResult(decision, result);
    }
}
