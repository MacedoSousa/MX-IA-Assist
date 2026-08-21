package com.macedxs.mx.core.application;

import com.macedxs.mx.agent.application.RouteDecision;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.application.SkillResult;
import com.macedxs.mx.agent.application.SkillRouter;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelImage;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.core.application.run.ExecutionRun;
import com.macedxs.mx.core.application.run.ExecutionRunStore;
import com.macedxs.mx.core.application.run.NoopExecutionRunStore;
import com.macedxs.mx.core.application.run.RunStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class MxCoreService {

    private final SkillRouter skillRouter;
    private final SkillTelemetry telemetry;
    private final ExecutionRunStore runStore;

    public MxCoreService(SkillRouter skillRouter) {
        this(skillRouter, SkillTelemetry.noop(), new NoopExecutionRunStore());
    }

    public MxCoreService(SkillRouter skillRouter, SkillTelemetry telemetry) {
        this(skillRouter, telemetry, new NoopExecutionRunStore());
    }

    public MxCoreService(
            SkillRouter skillRouter,
            SkillTelemetry telemetry,
            ExecutionRunStore runStore
    ) {
        if (skillRouter == null) {
            throw new IllegalArgumentException("Skill router is required");
        }
        if (telemetry == null) {
            throw new IllegalArgumentException("Skill telemetry is required");
        }
        if (runStore == null) {
            throw new IllegalArgumentException("Execution run store is required");
        }
        this.skillRouter = skillRouter;
        this.telemetry = telemetry;
        this.runStore = runStore;
    }

    public MxCoreResponse handle(UUID userId, String prompt) {
        return handle(userId, prompt, null, List.of());
    }

    public MxCoreResponse handle(UUID userId, String prompt, String idempotencyKey) {
        return handle(userId, prompt, idempotencyKey, List.of());
    }

    public MxCoreResponse handle(
            UUID userId,
            String prompt,
            String idempotencyKey,
            List<ModelImage> images
    ) {
        validateInput(userId, prompt);
        Optional<MxCoreResponse> previous = findCompletedIdempotentResponse(userId, idempotencyKey);
        if (previous.isPresent()) {
            return previous.get();
        }
        rejectInProgressDuplicate(userId, idempotencyKey);

        UUID correlationId = UUID.randomUUID();
        ExecutionRun run = ExecutionRun.receive(UUID.randomUUID(), userId, correlationId, prompt, idempotencyKey);
        persist(run);
        String skillName = null;

        try {
            SkillRequest skillRequest = new SkillRequest(prompt, images);
            RouteDecision decision = skillRouter.route(skillRequest);
            skillName = decision.skill().definition().name();
            final String selectedSkillName = skillName;
            run.route(selectedSkillName);
            persist(run);
            safely(() -> telemetry.routed(selectedSkillName, decision.confidence()));

            run.startExecution();
            persist(run);
            SkillResult result = decision.skill().execute(
                    skillRequest,
                    new SkillExecutionContext(userId, correlationId, decision.skill().definition().maximumAutonomy())
            );

            run.beginVerification();
            persist(run);
            if (result == null || result.answer() == null || result.answer().isBlank()) {
                throw new IllegalStateException("Skill returned an empty answer");
            }
            run.complete(result.answer());
            persist(run);
            safely(() -> telemetry.completed(selectedSkillName));

            return toResponse(correlationId, skillName, decision, result, run.runId());
        } catch (RuntimeException failure) {
            failIfActive(run);
            String failedSkill = skillName == null ? "unknown" : skillName;
            safely(() -> telemetry.failed(failedSkill, failure));
            throw failure;
        }
    }

    public MxCoreResponse handleStreaming(UUID userId, String prompt, Consumer<String> chunkConsumer) {
        return handleStreaming(userId, prompt, null, List.of(), chunkConsumer);
    }

    public MxCoreResponse handleStreaming(UUID userId, String prompt, String idempotencyKey, Consumer<String> chunkConsumer) {
        return handleStreaming(userId, prompt, idempotencyKey, List.of(), chunkConsumer);
    }

    public MxCoreResponse handleStreaming(
            UUID userId,
            String prompt,
            String idempotencyKey,
            List<ModelImage> images,
            Consumer<String> chunkConsumer
    ) {
        Objects.requireNonNull(chunkConsumer, "Chunk consumer is required");
        return handleStreamingWithObserver(
                userId,
                prompt,
                idempotencyKey,
                images,
                ModelStreamObserver.from(chunkConsumer)
        );
    }

    public MxCoreResponse handleStreamingWithObserver(
            UUID userId,
            String prompt,
            ModelStreamObserver observer
    ) {
        return handleStreamingWithObserver(userId, prompt, null, List.of(), observer);
    }

    public MxCoreResponse handleStreamingWithObserver(
            UUID userId,
            String prompt,
            String idempotencyKey,
            ModelStreamObserver observer
    ) {
        return handleStreamingWithObserver(userId, prompt, idempotencyKey, List.of(), observer);
    }

    public MxCoreResponse handleStreamingWithObserver(
            UUID userId,
            String prompt,
            String idempotencyKey,
            List<ModelImage> images,
            ModelStreamObserver observer
    ) {
        validateInput(userId, prompt);
        Objects.requireNonNull(observer, "Stream observer is required");
        Optional<MxCoreResponse> previous = findCompletedIdempotentResponse(userId, idempotencyKey);
        if (previous.isPresent()) {
            observer.onStarted(previous.get().runId(), previous.get().correlationId());
            observer.onChunk(previous.get().answer());
            return previous.get();
        }
        rejectInProgressDuplicate(userId, idempotencyKey);

        UUID correlationId = UUID.randomUUID();
        ExecutionRun run = ExecutionRun.receive(UUID.randomUUID(), userId, correlationId, prompt, idempotencyKey);
        persist(run);
        observer.onStarted(run.runId(), correlationId);
        String skillName = null;

        try {
            SkillRequest skillRequest = new SkillRequest(prompt, images);
            RouteDecision decision = skillRouter.route(skillRequest);
            skillName = decision.skill().definition().name();
            final String selectedSkillName = skillName;
            run.route(selectedSkillName);
            persist(run);
            safely(() -> telemetry.routed(selectedSkillName, decision.confidence()));

            run.startExecution();
            persist(run);
            SkillResult result = decision.skill().stream(
                    skillRequest,
                    new SkillExecutionContext(userId, correlationId, decision.skill().definition().maximumAutonomy()),
                    observer::onChunk
            );

            run.beginVerification();
            persist(run);
            if (result == null || result.answer() == null || result.answer().isBlank()) {
                throw new IllegalStateException("Skill returned an empty answer");
            }
            run.complete(result.answer());
            persist(run);
            safely(() -> telemetry.completed(selectedSkillName));

            return toResponse(correlationId, skillName, decision, result, run.runId());
        } catch (RuntimeException failure) {
            failIfActive(run);
            String failedSkill = skillName == null ? "unknown" : skillName;
            safely(() -> telemetry.failed(failedSkill, failure));
            throw failure;
        }
    }

    private MxCoreResponse toResponse(
            UUID correlationId,
            String skillName,
            RouteDecision decision,
            SkillResult result,
            UUID runId
    ) {
        Map<String, Object> metadata = result.metadata();
        UUID approvalRunId = parseUuid(metadata.get("approvalRunId"));
        String approvalNonce = metadata.get("approvalNonce") == null
                ? null
                : String.valueOf(metadata.get("approvalNonce"));
        Instant approvalExpiresAt = parseInstant(metadata.get("approvalExpiresAt"));
        String status = Boolean.TRUE.equals(metadata.get("approvalRequired"))
                ? "AWAITING_APPROVAL"
                : "COMPLETED";
        return new MxCoreResponse(
                correlationId,
                skillName,
                decision.confidence(),
                decision.requiresClarification(),
                result.answer(),
                runId,
                status,
                approvalRunId,
                approvalNonce,
                approvalExpiresAt
        );
    }

    private UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Instant parseInstant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void validateInput(UUID userId, String prompt) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }
    }

    private Optional<MxCoreResponse> findCompletedIdempotentResponse(UUID userId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return runStore.findByIdempotencyKey(userId, idempotencyKey)
                .filter(snapshot -> snapshot.status() == RunStatus.COMPLETED)
                .map(snapshot -> new MxCoreResponse(
                        snapshot.correlationId(),
                        snapshot.skillName() == null ? "unknown" : snapshot.skillName(),
                        1.0,
                        false,
                        snapshot.output(),
                        snapshot.runId()
                ));
    }

    private void rejectInProgressDuplicate(UUID userId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        runStore.findByIdempotencyKey(userId, idempotencyKey)
                .filter(snapshot -> switch (snapshot.status()) {
                    case RECEIVED, ROUTED, EXECUTING, AWAITING_APPROVAL, VERIFYING -> true;
                    case COMPLETED, FAILED, CANCELLED -> false;
                })
                .ifPresent(snapshot -> {
                    throw new IllegalStateException("Idempotent request is already in progress: " + snapshot.runId());
                });
    }

    private void failIfActive(ExecutionRun run) {
        if (run.status() != RunStatus.COMPLETED
                && run.status() != RunStatus.FAILED
                && run.status() != RunStatus.CANCELLED) {
            run.fail("SKILL_EXECUTION_FAILED");
            persist(run);
        }
    }

    private void persist(ExecutionRun run) {
        runStore.save(run);
    }

    private void safely(Runnable telemetryAction) {
        try {
            telemetryAction.run();
        } catch (RuntimeException ignored) {
            // Observabilidade não pode alterar o resultado da aplicação.
        }
    }
}
