package com.macedxs.mx.core.application;

import java.time.Instant;
import java.util.UUID;

public record MxCoreResponse(
        UUID correlationId,
        String skillName,
        double confidence,
        boolean requiresClarification,
        String answer,
        UUID runId,
        String status,
        UUID approvalRunId,
        String approvalNonce,
        Instant approvalExpiresAt
) {

    public MxCoreResponse(
            UUID correlationId,
            String skillName,
            double confidence,
            boolean requiresClarification,
            String answer
    ) {
        this(correlationId, skillName, confidence, requiresClarification, answer,
                null, "COMPLETED", null, null, null);
    }

    public MxCoreResponse(
            UUID correlationId,
            String skillName,
            double confidence,
            boolean requiresClarification,
            String answer,
            UUID runId
    ) {
        this(correlationId, skillName, confidence, requiresClarification, answer,
                runId, "COMPLETED", null, null, null);
    }

    public MxCoreResponse {
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        if (approvalRunId == null && (approvalNonce != null || approvalExpiresAt != null)) {
            throw new IllegalArgumentException("Approval metadata requires an approval run");
        }
    }

    public boolean awaitingApproval() {
        return "AWAITING_APPROVAL".equals(status);
    }
}
