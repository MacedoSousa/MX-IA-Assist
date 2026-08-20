package com.macedxs.mx.core.application;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.RouteDecision;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.application.SkillResult;
import com.macedxs.mx.agent.application.SkillRouter;
import com.macedxs.mx.conversation.application.port.ModelStreamObserver;
import com.macedxs.mx.core.application.run.ExecutionRun;
import com.macedxs.mx.core.application.run.ExecutionRunStore;
import com.macedxs.mx.core.application.run.NoopExecutionRunStore;

import java.util.UUID;
import java.util.Objects;
import java.util.Optional;
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
        return handle(userId, prompt, null);
    }

    public MxCoreResponse handle(UUID userId, String prompt, String idempotencyKey) {
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
            RouteDecision decision = skillRouter.route(prompt);
            skillName = decision.skill().definition().name();
            final String selectedSkillName = skillName;
            run.route(selectedSkillName);
            persist(run);
            safely(() -> telemetry.routed(selectedSkillName, decision.confidence()));

            run.startExecution();
            persist(run);
            SkillResult result = decision.skill().execute(
                    new SkillRequest(prompt),
                    new SkillExecutionContext(userId, correlationId, AutonomyLevel.RESPOND)
            );

            run.beginVerification();
            persist(run);
            if (result == null || result.answer() == null || result.answer().isBlank()) {
                throw new IllegalStateException("Skill returned an empty answer");
            }
            run.complete(result.answer());
            persist(run);
            safely(() -> telemetry.completed(selectedSkillName));

            return new MxCoreResponse(
                    correlationId,
                    skillName,
                    decision.confidence(),
                    decision.requiresClarification(),
                    result.answer(),
                    run.runId()
            );
        } catch (RuntimeException failure) {
            if (run.status() != com.macedxs.mx.core.application.run.RunStatus.COMPLETED
                    && run.status() != com.macedxs.mx.core.application.run.RunStatus.FAILED
                    && run.status() != com.macedxs.mx.core.application.run.RunStatus.CANCELLED) {
                run.fail("SKILL_EXECUTION_FAILED");
                persist(run);
            }
            String failedSkill = skillName == null ? "unknown" : skillName;
            safely(() -> telemetry.failed(failedSkill, failure));
            throw failure;
        }
    }

    public MxCoreResponse handleStreaming(UUID userId, String prompt, Consumer<String> chunkConsumer) {
        return handleStreaming(userId, prompt, null, chunkConsumer);
    }

    public MxCoreResponse handleStreaming(UUID userId, String prompt, String idempotencyKey, Consumer<String> chunkConsumer) {
        Objects.requireNonNull(chunkConsumer, "Chunk consumer is required");
        return handleStreamingWithObserver(userId, prompt, idempotencyKey, ModelStreamObserver.from(chunkConsumer));
    }

    public MxCoreResponse handleStreamingWithObserver(
            UUID userId,
            String prompt,
            ModelStreamObserver observer
    ) {
        return handleStreamingWithObserver(userId, prompt, null, observer);
    }

    public MxCoreResponse handleStreamingWithObserver(
            UUID userId,
            String prompt,
            String idempotencyKey,
            ModelStreamObserver observer
    ) {
        validateInput(userId, prompt);
        Optional<MxCoreResponse> previous = findCompletedIdempotentResponse(userId, idempotencyKey);
        if (previous.isPresent()) {
            observer.onStarted(previous.get().runId(), previous.get().correlationId());
            observer.onChunk(previous.get().answer());
            return previous.get();
        }
        rejectInProgressDuplicate(userId, idempotencyKey);
        Objects.requireNonNull(observer, "Stream observer is required");

        UUID correlationId = UUID.randomUUID();
        ExecutionRun run = ExecutionRun.receive(UUID.randomUUID(), userId, correlationId, prompt, idempotencyKey);
        persist(run);
        observer.onStarted(run.runId(), correlationId);
        String skillName = null;

        try {
            RouteDecision decision = skillRouter.route(prompt);
            skillName = decision.skill().definition().name();
            final String selectedSkillName = skillName;
            run.route(selectedSkillName);
            persist(run);
            safely(() -> telemetry.routed(selectedSkillName, decision.confidence()));

            run.startExecution();
            persist(run);
            SkillResult result = decision.skill().stream(
                    new SkillRequest(prompt),
                    new SkillExecutionContext(userId, correlationId, AutonomyLevel.RESPOND),
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

            return new MxCoreResponse(
                    correlationId,
                    skillName,
                    decision.confidence(),
                    decision.requiresClarification(),
                    result.answer(),
                    run.runId()
            );
        } catch (RuntimeException failure) {
            if (run.status() != com.macedxs.mx.core.application.run.RunStatus.COMPLETED
                    && run.status() != com.macedxs.mx.core.application.run.RunStatus.FAILED
                    && run.status() != com.macedxs.mx.core.application.run.RunStatus.CANCELLED) {
                run.fail("SKILL_EXECUTION_FAILED");
                persist(run);
            }
            String failedSkill = skillName == null ? "unknown" : skillName;
            safely(() -> telemetry.failed(failedSkill, failure));
            throw failure;
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
                .filter(snapshot -> snapshot.status() == com.macedxs.mx.core.application.run.RunStatus.COMPLETED)
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
                .filter(snapshot -> snapshot.status() != com.macedxs.mx.core.application.run.RunStatus.COMPLETED)
                .ifPresent(snapshot -> {
                    throw new IllegalStateException("Idempotent request is already in progress: " + snapshot.runId());
                });
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
