package com.macedxs.mx.evolution.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SelfExtensionJob(
        UUID jobId,
        UUID userId,
        UUID correlationId,
        Instant createdAt,
        SelfExtensionJobStatus status,
        SelfExtensionSubmission submission
) {

    public SelfExtensionJob {
        if (jobId == null || correlationId == null || createdAt == null || status == null || submission == null) {
            throw new IllegalArgumentException("Self-extension job fields are required");
        }
        submission = new SelfExtensionSubmission(
                submission.type(),
                submission.slug().trim(),
                submission.description().trim(),
                submission.files(),
                submission.validations().stream().map(String::trim).toList(),
                submission.commitMessage().trim(),
                submission.allowPush()
        );
    }

    public static SelfExtensionJob pending(UUID userId, UUID correlationId, SelfExtensionSubmission submission) {
        return new SelfExtensionJob(
                UUID.randomUUID(),
                userId,
                correlationId,
                Instant.now(),
                SelfExtensionJobStatus.PENDING,
                submission
        );
    }

    public enum SelfExtensionJobStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        REJECTED
    }
}
