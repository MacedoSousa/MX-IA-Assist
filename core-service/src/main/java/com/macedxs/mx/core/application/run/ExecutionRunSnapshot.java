package com.macedxs.mx.core.application.run;

import java.time.Instant;
import java.util.UUID;

public record ExecutionRunSnapshot(
        UUID runId,
        UUID userId,
        UUID correlationId,
        String input,
        RunStatus status,
        String skillName,
        String pendingApproval,
        String output,
        String errorCode,
        Instant receivedAt,
        Instant finishedAt,
        String pendingApprovalArguments,
        String approvalNonceHash,
        Instant approvalExpiresAt,
        Instant updatedAt,
        String idempotencyKey
) {

    public ExecutionRunSnapshot(
            UUID runId,
            UUID userId,
            UUID correlationId,
            String input,
            RunStatus status,
            String skillName,
            String pendingApproval,
            String output,
            String errorCode,
            Instant receivedAt,
            Instant finishedAt
    ) {
        this(runId, userId, correlationId, input, status, skillName, pendingApproval,
                output, errorCode, receivedAt, finishedAt, null, null, null,
                receivedAt, null);
    }

    public Instant effectiveUpdatedAt() {
        return updatedAt == null ? receivedAt : updatedAt;
    }
}
