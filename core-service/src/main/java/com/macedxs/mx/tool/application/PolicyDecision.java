package com.macedxs.mx.tool.application;

public record PolicyDecision(
        PolicyOutcome outcome,
        String reason
) {

    public PolicyDecision {
        if (outcome == null) {
            throw new IllegalArgumentException("Policy outcome is required");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Policy reason is required");
        }
    }

    public static PolicyDecision allow(String reason) {
        return new PolicyDecision(PolicyOutcome.ALLOW, reason);
    }

    public static PolicyDecision deny(String reason) {
        return new PolicyDecision(PolicyOutcome.DENY, reason);
    }

    public static PolicyDecision requireApproval(String reason) {
        return new PolicyDecision(PolicyOutcome.REQUIRE_APPROVAL, reason);
    }
}
