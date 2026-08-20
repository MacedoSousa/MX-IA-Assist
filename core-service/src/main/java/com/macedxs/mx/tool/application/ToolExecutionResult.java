package com.macedxs.mx.tool.application;

public record ToolExecutionResult(
        PolicyDecision policyDecision,
        ToolResult toolResult
) {

    public ToolExecutionResult {
        if (policyDecision == null) {
            throw new IllegalArgumentException("Policy decision is required");
        }
        if (policyDecision.outcome() == PolicyOutcome.ALLOW && toolResult == null) {
            throw new IllegalArgumentException("Allowed execution requires a tool result");
        }
        if (policyDecision.outcome() != PolicyOutcome.ALLOW && toolResult != null) {
            throw new IllegalArgumentException("Blocked execution cannot have a tool result");
        }
    }
}
