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
        Instant finishedAt
) {
}
