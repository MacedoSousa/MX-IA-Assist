package com.macedxs.mx.tool.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.core.application.run.ApprovalNonce;
import com.macedxs.mx.core.application.run.ExecutionRun;
import com.macedxs.mx.core.application.run.ExecutionRunSnapshot;
import com.macedxs.mx.core.application.run.ExecutionRunStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class ToolExecutor {

    private static final ObjectMapper SNAPSHOT_MAPPER = new ObjectMapper();

    private final ToolRegistry registry;
    private final PolicyEngine policyEngine;
    private final ExecutionRunStore executionRunStore;
    private final Duration approvalExpiration;

    public ToolExecutor(ToolRegistry registry, PolicyEngine policyEngine) {
        this(registry, policyEngine, null, Duration.ofMinutes(15));
    }

    public ToolExecutor(
            ToolRegistry registry,
            PolicyEngine policyEngine,
            ExecutionRunStore executionRunStore,
            Duration approvalExpiration
    ) {
        if (registry == null) {
            throw new IllegalArgumentException("Tool registry is required");
        }
        if (policyEngine == null) {
            throw new IllegalArgumentException("Policy engine is required");
        }
        if (approvalExpiration == null || approvalExpiration.isNegative() || approvalExpiration.isZero()) {
            throw new IllegalArgumentException("Approval expiration must be positive");
        }
        this.registry = registry;
        this.policyEngine = policyEngine;
        this.executionRunStore = executionRunStore;
        this.approvalExpiration = approvalExpiration;
    }

    public ToolExecutionResult execute(
            ToolRequest request,
            ToolExecutionContext context,
            boolean approved
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Tool request is required");
        }
        if (context == null) {
            throw new IllegalArgumentException("Tool execution context is required");
        }

        Tool tool = registry.getRequired(request.toolName());
        PolicyDecision decision = policyEngine.evaluate(
                tool.definition(),
                request,
                context.grantedAutonomy(),
                approved,
                context.allowedTools()
        );

        if (decision.outcome() == PolicyOutcome.REQUIRE_APPROVAL) {
            return createApprovalRun(request, context, decision);
        }
        if (decision.outcome() != PolicyOutcome.ALLOW) {
            return new ToolExecutionResult(decision, null);
        }

        ToolResult result = tool.execute(request, context);
        return new ToolExecutionResult(decision, result);
    }

    private ToolExecutionResult createApprovalRun(
            ToolRequest request,
            ToolExecutionContext context,
            PolicyDecision decision
    ) {
        if (executionRunStore == null || context.userId() == null) {
            return new ToolExecutionResult(decision, null);
        }

        ApprovalNonce.Issued nonce = ApprovalNonce.issue();
        Instant expiresAt = Instant.now().plus(approvalExpiration);
        ExecutionRun run = ExecutionRun.receive(
                UUID.randomUUID(),
                context.userId(),
                context.correlationId(),
                request.toolName()
        );
        run.route(context.skillName());
        run.startExecution();
        run.requireApproval(
                request.toolName(),
                snapshotArguments(request.arguments()),
                nonce.hash(),
                expiresAt
        );
        ExecutionRunSnapshot saved = executionRunStore.save(run);
        return new ToolExecutionResult(decision, null, saved.runId(), nonce.raw(), expiresAt);
    }

    private String snapshotArguments(Map<String, Object> arguments) {
        try {
            return SNAPSHOT_MAPPER.writeValueAsString(new TreeMap<>(arguments));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Tool arguments cannot be snapshotted", exception);
        }
    }
}
