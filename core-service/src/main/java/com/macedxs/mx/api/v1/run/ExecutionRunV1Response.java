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
        String output,
        String errorCode,
        Instant receivedAt,
        Instant finishedAt
) {

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
                snapshot.output(),
                snapshot.errorCode(),
                snapshot.receivedAt(),
                snapshot.finishedAt()
        );
    }
}
