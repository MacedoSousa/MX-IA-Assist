package com.macedxs.mx.api.v1.run;

import com.macedxs.mx.core.application.run.ExecutionRunSnapshot;

import java.time.Instant;
import java.util.UUID;

public record ExecutionRunV1Response(
        UUID runId,
        UUID correlationId,
        String status,
        String skillName,
        String pendingApproval,
        String pendingApprovalArguments,
        Instant approvalExpiresAt,
        boolean approvalNonceRequired,
        String output,
        String errorCode,
        Instant receivedAt,
        Instant finishedAt,
        Instant updatedAt,
        String idempotencyKey
) {

    public ExecutionRunV1Response(
            UUID runId,
            UUID correlationId,
            String status,
            String skillName,
            String pendingApproval,
            String output,
            String errorCode,
            Instant receivedAt,
            Instant finishedAt
    ) {
        this(runId, correlationId, status, skillName, pendingApproval, null, null,
                false, output, errorCode, receivedAt, finishedAt, receivedAt, null);
    }

    public static ExecutionRunV1Response from(ExecutionRunSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Run snapshot is required");
        }
        return new ExecutionRunV1Response(
                snapshot.runId(),
                snapshot.correlationId(),
                snapshot.status().name(),
                snapshot.skillName(),
                snapshot.pendingApproval(),
                snapshot.pendingApprovalArguments(),
                snapshot.approvalExpiresAt(),
                snapshot.approvalNonceHash() != null,
                snapshot.output(),
                snapshot.errorCode(),
                snapshot.receivedAt(),
                snapshot.finishedAt(),
                snapshot.effectiveUpdatedAt(),
                snapshot.idempotencyKey()
        );
    }
}
