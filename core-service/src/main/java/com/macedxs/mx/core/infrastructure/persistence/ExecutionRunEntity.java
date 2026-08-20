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

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(name = "error_code", length = 120)
    private String errorCode;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

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
        entity.output = snapshot.output();
        entity.errorCode = snapshot.errorCode();
        entity.receivedAt = snapshot.receivedAt();
        entity.finishedAt = snapshot.finishedAt();
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
                finishedAt
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public String getInput() {
        return input;
    }

    public RunStatus getStatus() {
        return status;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getPendingApproval() {
        return pendingApproval;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }
}
