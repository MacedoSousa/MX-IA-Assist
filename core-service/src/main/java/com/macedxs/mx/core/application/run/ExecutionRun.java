package com.macedxs.mx.core.application.run;

import java.time.Instant;
import java.util.UUID;

public class ExecutionRun {

    private final UUID runId;
    private final UUID userId;
    private final UUID correlationId;
    private final String input;
    private Instant receivedAt;
    private RunStatus status;
    private String skillName;
    private String pendingApproval;
    private String output;
    private String errorCode;
    private Instant finishedAt;

    private ExecutionRun(
            UUID runId,
            UUID userId,
            UUID correlationId,
            String input
    ) {
        this.runId = require(runId, "Run id");
        this.userId = require(userId, "User id");
        this.correlationId = require(correlationId, "Correlation id");
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Run input is required");
        }
        this.input = input.trim();
        this.receivedAt = Instant.now();
        this.status = RunStatus.RECEIVED;
    }

    public static ExecutionRun receive(UUID runId, UUID userId, UUID correlationId, String input) {
        return new ExecutionRun(runId, userId, correlationId, input);
    }

    public static ExecutionRun restore(ExecutionRunSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Run snapshot is required");
        }
        ExecutionRun run = new ExecutionRun(
                snapshot.runId(),
                snapshot.userId(),
                snapshot.correlationId(),
                snapshot.input()
        );
        run.receivedAt = snapshot.receivedAt();
        run.status = snapshot.status();
        run.skillName = snapshot.skillName();
        run.pendingApproval = snapshot.pendingApproval();
        run.output = snapshot.output();
        run.errorCode = snapshot.errorCode();
        run.finishedAt = snapshot.finishedAt();
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
                finishedAt
        );
    }

    public void route(String skillName) {
        requireStatus(RunStatus.RECEIVED, "route");
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        this.skillName = skillName.trim();
        this.status = RunStatus.ROUTED;
    }

    public void startExecution() {
        requireStatus(RunStatus.ROUTED, "start execution");
        this.status = RunStatus.EXECUTING;
    }

    public void requireApproval(String toolName) {
        requireStatus(RunStatus.EXECUTING, "require approval");
        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException("Tool name is required");
        }
        this.pendingApproval = toolName.trim();
        this.status = RunStatus.AWAITING_APPROVAL;
    }

    public void resumeAfterApproval() {
        requireStatus(RunStatus.AWAITING_APPROVAL, "resume after approval");
        this.pendingApproval = null;
        this.status = RunStatus.EXECUTING;
    }

    public void rejectApproval(String reason) {
        requireStatus(RunStatus.AWAITING_APPROVAL, "reject approval");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        this.pendingApproval = null;
        this.errorCode = "APPROVAL_REJECTED";
        this.status = RunStatus.CANCELLED;
        this.finishedAt = Instant.now();
    }

    public void beginVerification() {
        requireStatus(RunStatus.EXECUTING, "begin verification");
        this.status = RunStatus.VERIFYING;
    }

    public void complete(String output) {
        requireStatus(RunStatus.VERIFYING, "complete");
        if (output == null || output.isBlank()) {
            throw new IllegalArgumentException("Run output is required");
        }
        this.output = output.trim();
        this.status = RunStatus.COMPLETED;
        this.finishedAt = Instant.now();
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
        this.finishedAt = Instant.now();
    }

    public void cancel() {
        if (status == RunStatus.COMPLETED || status == RunStatus.CANCELLED || status == RunStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel a terminal run");
        }
        this.status = RunStatus.CANCELLED;
        this.finishedAt = Instant.now();
    }

    private void requireStatus(RunStatus expected, String operation) {
        if (status != expected) {
            throw new IllegalStateException("Cannot " + operation + " from status " + status);
        }
    }

    private static <T> T require(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    public UUID runId() {
        return runId;
    }

    public UUID userId() {
        return userId;
    }

    public UUID correlationId() {
        return correlationId;
    }

    public String input() {
        return input;
    }

    public Instant receivedAt() {
        return receivedAt;
    }

    public RunStatus status() {
        return status;
    }

    public String skillName() {
        return skillName;
    }

    public String pendingApproval() {
        return pendingApproval;
    }

    public String output() {
        return output;
    }

    public String errorCode() {
        return errorCode;
    }

    public Instant finishedAt() {
        return finishedAt;
    }
}
