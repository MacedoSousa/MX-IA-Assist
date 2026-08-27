package com.macedxs.mx.core.application.run;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolExecutionResult;
import com.macedxs.mx.tool.application.ToolExecutor;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Consome uma aprovação válida e executa somente a tool e os argumentos
 * previamente registrados pelo ToolExecutor. Nenhum comando de shell é aceito.
 */
public final class ApprovedToolExecutionUseCase {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final ExecutionRunStore runStore;
    private final ToolExecutor toolExecutor;
    private final ApproveExecutionRunUseCase approveExecutionRunUseCase;
    private final ObjectMapper objectMapper;

    public ApprovedToolExecutionUseCase(ExecutionRunStore runStore, ToolExecutor toolExecutor) {
        this(runStore, toolExecutor, new ObjectMapper());
    }

    ApprovedToolExecutionUseCase(ExecutionRunStore runStore, ToolExecutor toolExecutor, ObjectMapper objectMapper) {
        if (runStore == null || toolExecutor == null || objectMapper == null) {
            throw new IllegalArgumentException("Run store, tool executor and object mapper are required");
        }
        this.runStore = runStore;
        this.toolExecutor = toolExecutor;
        this.approveExecutionRunUseCase = new ApproveExecutionRunUseCase(runStore);
        this.objectMapper = objectMapper;
    }

    public Optional<ExecutionRunSnapshot> execute(UUID userId, UUID runId, String approvalNonce) {
        Optional<ExecutionRunSnapshot> pending = runStore.findById(userId, runId);
        if (pending.isEmpty()) {
            return Optional.empty();
        }
        ExecutionRunSnapshot original = pending.orElseThrow();
        if (original.status() != RunStatus.AWAITING_APPROVAL
                || original.pendingApproval() == null || original.pendingApproval().isBlank()) {
            throw new ExecutionRunApprovalException("Execution run has no pending tool approval");
        }

        Map<String, Object> arguments;
        try {
            arguments = objectMapper.readValue(original.pendingApprovalArguments(), MAP_TYPE);
        } catch (Exception exception) {
            return Optional.of(fail(original, "APPROVED_TOOL_ARGUMENTS_INVALID"));
        }

        ExecutionRunSnapshot resumed = approveExecutionRunUseCase.execute(userId, runId, approvalNonce)
                .orElseThrow();
        try {
            ToolExecutionResult execution = toolExecutor.execute(
                    new ToolRequest(original.pendingApproval(), arguments),
                    new ToolExecutionContext(
                            original.userId(),
                            original.correlationId(),
                            original.skillName(),
                            AutonomyLevel.EXECUTE_WITH_APPROVAL,
                            Set.of(original.pendingApproval())
                    ),
                    true
            );
            ToolResult toolResult = execution.toolResult();
            if (toolResult == null || !toolResult.success()) {
                return Optional.of(fail(resumed, toolResult == null
                        ? "APPROVED_TOOL_EXECUTION_DENIED"
                        : "APPROVED_TOOL_" + toolResult.errorCode()));
            }
            ExecutionRun completed = ExecutionRun.restore(resumed);
            completed.beginVerification();
            completed.complete(objectMapper.writeValueAsString(Map.of(
                    "toolName", toolResult.toolName(),
                    "output", toolResult.output()
            )));
            return Optional.of(runStore.save(completed));
        } catch (Exception exception) {
            return Optional.of(fail(resumed, "APPROVED_TOOL_EXECUTION_FAILED"));
        }
    }

    private ExecutionRunSnapshot fail(ExecutionRunSnapshot snapshot, String errorCode) {
        ExecutionRun run = ExecutionRun.restore(snapshot);
        run.fail(errorCode);
        return runStore.save(run);
    }
}
