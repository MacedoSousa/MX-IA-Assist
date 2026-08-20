package com.macedxs.mx.tool.application;

import java.time.Instant;
import java.util.UUID;

public record ToolExecutionResult(
        PolicyDecision policyDecision,
        ToolResult toolResult,
        UUID approvalRunId,
        String approvalNonce,
        Instant approvalExpiresAt
) {

    public ToolExecutionResult(PolicyDecision policyDecision, ToolResult toolResult) {
        this(policyDecision, toolResult, null, null, null);
    }

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
        if (approvalRunId == null && (approvalNonce != null || approvalExpiresAt != null)) {
            throw new IllegalArgumentException("Approval metadata requires an approval run");
        }
        if (approvalNonce != null && approvalNonce.isBlank()) {
            throw new IllegalArgumentException("Approval nonce cannot be blank");
        }
        if (approvalExpiresAt != null && !approvalExpiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Approval expiration must be in the future");
        }
    }

    public boolean requiresApproval() {
        return policyDecision.outcome() == PolicyOutcome.REQUIRE_APPROVAL;
    }
}
