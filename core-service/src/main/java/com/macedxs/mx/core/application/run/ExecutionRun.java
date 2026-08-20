package com.macedxs.mx.core.application.run;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class ExecutionRun {

    private final UUID runId;
    private final UUID userId;
    private final UUID correlationId;
    private final String input;
    private final Clock clock;
    private Instant receivedAt;
    private RunStatus status;
    private String skillName;
    private String pendingApproval;
    private String output;
    private String errorCode;
    private Instant finishedAt;
    private String pendingApprovalArguments;
    private String approvalNonceHash;
    private Instant approvalExpiresAt;
    private Instant updatedAt;
    private final String idempotencyKey;

    private ExecutionRun(
            UUID runId,
            UUID userId,
            UUID correlationId,
            String input,
            String idempotencyKey,
            Clock clock
    ) {
        this.runId = require(runId, "Run id");
        this.userId = require(userId, "User id");
        this.correlationId = require(correlationId, "Correlation id");
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Run input is required");
        }
        this.input = input.trim();
        this.idempotencyKey = normalize(idempotencyKey);
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.receivedAt = Instant.now(this.clock);
        this.updatedAt = this.receivedAt;
        this.status = RunStatus.RECEIVED;
    }

    public static ExecutionRun receive(UUID runId, UUID userId, UUID correlationId, String input) {
        return receive(runId, userId, correlationId, input, null);
    }

    public static ExecutionRun receive(
            UUID runId,
            UUID userId,
            UUID correlationId,
            String input,
            String idempotencyKey
    ) {
        return new ExecutionRun(runId, userId, correlationId, input, idempotencyKey, Clock.systemUTC());
    }

    public static ExecutionRun restore(ExecutionRunSnapshot snapshot) {
        return restore(snapshot, Clock.systemUTC());
    }

    static ExecutionRun restore(ExecutionRunSnapshot snapshot, Clock clock) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Run snapshot is required");
        }
        ExecutionRun run = new ExecutionRun(
                snapshot.runId(),
                snapshot.userId(),
                snapshot.correlationId(),
                snapshot.input(),
                snapshot.idempotencyKey(),
                clock
        );
        run.receivedAt = snapshot.receivedAt();
        run.status = snapshot.status();
        run.skillName = snapshot.skillName();
        run.pendingApproval = snapshot.pendingApproval();
        run.output = snapshot.output();
        run.errorCode = snapshot.errorCode();
        run.finishedAt = snapshot.finishedAt();
        run.pendingApprovalArguments = snapshot.pendingApprovalArguments();
        run.approvalNonceHash = snapshot.approvalNonceHash();
        run.approvalExpiresAt = snapshot.approvalExpiresAt();
        run.updatedAt = snapshot.effectiveUpdatedAt();
        return run;
    }

    public ExecutionRunSnapshot snapshot() {
        return new ExecutionRunSnapshot(
                runId,
                userId,
                correlationId,
                input,
                status,
                skillName,
                pendingApproval,
                output,
                errorCode,
                receivedAt,
                finishedAt,
                pendingApprovalArguments,
                approvalNonceHash,
                approvalExpiresAt,
                updatedAt,
                idempotencyKey
        );
    }

    public void route(String skillName) {
        requireStatus(RunStatus.RECEIVED, "route");
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        this.skillName = skillName.trim();
        this.status = RunStatus.ROUTED;
        touch();
    }

    public void startExecution() {
        requireStatus(RunStatus.ROUTED, "start execution");
        this.status = RunStatus.EXECUTING;
        touch();
    }

    public void requireApproval(String toolName) {
        requireApproval(toolName, null, null, null);
    }

    public void requireApproval(
            String toolName,
            String argumentsSnapshot,
            String nonceHash,
            Instant expiresAt
    ) {
        requireStatus(RunStatus.EXECUTING, "require approval");
        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException("Tool name is required");
        }
        if (nonceHash != null && nonceHash.isBlank()) {
            throw new IllegalArgumentException("Approval nonce hash cannot be blank");
        }
        if (expiresAt != null && !expiresAt.isAfter(Instant.now(clock))) {
            throw new IllegalArgumentException("Approval expiration must be in the future");
        }
        this.pendingApproval = toolName.trim();
        this.pendingApprovalArguments = normalize(argumentsSnapshot);
        this.approvalNonceHash = normalize(nonceHash);
        this.approvalExpiresAt = expiresAt;
        this.status = RunStatus.AWAITING_APPROVAL;
        touch();
    }

    public void resumeAfterApproval() {
        resumeAfterApproval(null);
    }

    public void resumeAfterApproval(String presentedNonce) {
        requireStatus(RunStatus.AWAITING_APPROVAL, "resume after approval");
        if (approvalExpiresAt != null && !approvalExpiresAt.isAfter(Instant.now(clock))) {
            throw new ExecutionRunApprovalException("Approval has expired");
        }
        if (approvalNonceHash != null) {
            if (presentedNonce == null || presentedNonce.isBlank()
                    || !ApprovalNonce.matches(presentedNonce, approvalNonceHash)) {
                throw new ExecutionRunApprovalException("Invalid or already used approval nonce");
            }
        }
        this.pendingApproval = null;
        this.pendingApprovalArguments = null;
        this.approvalNonceHash = null;
        this.approvalExpiresAt = null;
        this.status = RunStatus.EXECUTING;
        touch();
    }

    public void rejectApproval(String reason) {
        requireStatus(RunStatus.AWAITING_APPROVAL, "reject approval");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        this.pendingApproval = null;
        this.pendingApprovalArguments = null;
        this.approvalNonceHash = null;
        this.approvalExpiresAt = null;
        this.errorCode = "APPROVAL_REJECTED";
        this.status = RunStatus.CANCELLED;
        this.finishedAt = Instant.now(clock);
        touch();
    }

    public void beginVerification() {
        requireStatus(RunStatus.EXECUTING, "begin verification");
        this.status = RunStatus.VERIFYING;
        touch();
    }

    public void complete(String output) {
        requireStatus(RunStatus.VERIFYING, "complete");
        if (output == null || output.isBlank()) {
            throw new IllegalArgumentException("Run output is required");
        }
        this.output = output.trim();
        this.status = RunStatus.COMPLETED;
        this.finishedAt = Instant.now(clock);
        touch();
    }

    public void fail(String errorCode) {
        if (status == RunStatus.COMPLETED || status == RunStatus.CANCELLED || status == RunStatus.FAILED) {
            throw new IllegalStateException("Cannot fail a terminal run");
        }
        if (errorCode == null || errorCode.isBlank()) {
            throw new IllegalArgumentException("Error code is required");
        }
        this.errorCode = errorCode.trim();
        this.status = RunStatus.FAILED;
        this.finishedAt = Instant.now(clock);
        touch();
    }

    public void cancel() {
        if (status == RunStatus.COMPLETED || status == RunStatus.CANCELLED || status == RunStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel a terminal run");
        }
        this.status = RunStatus.CANCELLED;
        this.finishedAt = Instant.now(clock);
        touch();
    }

    private void requireStatus(RunStatus expected, String operation) {
        if (status != expected) {
            throw new IllegalStateException("Cannot " + operation + " from status " + status);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now(clock);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static <T> T require(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    public UUID runId() { return runId; }
    public UUID userId() { return userId; }
    public UUID correlationId() { return correlationId; }
    public String input() { return input; }
    public Instant receivedAt() { return receivedAt; }
    public RunStatus status() { return status; }
    public String skillName() { return skillName; }
    public String pendingApproval() { return pendingApproval; }
    public String output() { return output; }
    public String errorCode() { return errorCode; }
    public Instant finishedAt() { return finishedAt; }
    public String pendingApprovalArguments() { return pendingApprovalArguments; }
    public String approvalNonceHash() { return approvalNonceHash; }
    public Instant approvalExpiresAt() { return approvalExpiresAt; }
    public Instant updatedAt() { return updatedAt; }
    public String idempotencyKey() { return idempotencyKey; }
}
