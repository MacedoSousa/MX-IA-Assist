package com.macedxs.mx.core.infrastructure.persistence;

import com.macedxs.mx.core.application.run.RunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution_runs")
public class ExecutionRunEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "correlation_id", nullable = false, unique = true)
    private UUID correlationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RunStatus status;

    @Column(name = "skill_name", length = 120)
    private String skillName;

    @Column(name = "pending_approval", length = 160)
    private String pendingApproval;

    @Column(name = "pending_approval_arguments", columnDefinition = "TEXT")
    private String pendingApprovalArguments;

    @Column(name = "approval_nonce_hash", length = 128)
    private String approvalNonceHash;

    @Column(name = "approval_expires_at")
    private Instant approvalExpiresAt;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(name = "error_code", length = 120)
    private String errorCode;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    protected ExecutionRunEntity() {
    }

    public static ExecutionRunEntity fromSnapshot(com.macedxs.mx.core.application.run.ExecutionRunSnapshot snapshot) {
        ExecutionRunEntity entity = new ExecutionRunEntity();
        entity.id = snapshot.runId();
        entity.userId = snapshot.userId();
        entity.correlationId = snapshot.correlationId();
        entity.input = snapshot.input();
        entity.status = snapshot.status();
        entity.skillName = snapshot.skillName();
        entity.pendingApproval = snapshot.pendingApproval();
        entity.pendingApprovalArguments = snapshot.pendingApprovalArguments();
        entity.approvalNonceHash = snapshot.approvalNonceHash();
        entity.approvalExpiresAt = snapshot.approvalExpiresAt();
        entity.output = snapshot.output();
        entity.errorCode = snapshot.errorCode();
        entity.receivedAt = snapshot.receivedAt();
        entity.finishedAt = snapshot.finishedAt();
        entity.updatedAt = snapshot.effectiveUpdatedAt();
        entity.idempotencyKey = snapshot.idempotencyKey();
        return entity;
    }

    public com.macedxs.mx.core.application.run.ExecutionRunSnapshot toSnapshot() {
        return new com.macedxs.mx.core.application.run.ExecutionRunSnapshot(
                id,
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

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCorrelationId() { return correlationId; }
    public String getInput() { return input; }
    public RunStatus getStatus() { return status; }
    public String getSkillName() { return skillName; }
    public String getPendingApproval() { return pendingApproval; }
    public String getPendingApprovalArguments() { return pendingApprovalArguments; }
    public String getApprovalNonceHash() { return approvalNonceHash; }
    public Instant getApprovalExpiresAt() { return approvalExpiresAt; }
    public String getOutput() { return output; }
    public String getErrorCode() { return errorCode; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
