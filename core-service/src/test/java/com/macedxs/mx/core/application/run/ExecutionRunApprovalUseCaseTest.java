package com.macedxs.mx.core.application.run;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionRunApprovalUseCaseTest {

    @Test
    void shouldApproveOnlyTheRunOwnedByTheAuthenticatedUser() {
        InMemoryExecutionRunStore store = new InMemoryExecutionRunStore();
        UUID userId = UUID.randomUUID();
        ExecutionRun run = pendingRun(userId);
        store.save(run);

        Optional<ExecutionRunSnapshot> approved = new ApproveExecutionRunUseCase(store)
                .execute(userId, run.runId());

        assertThat(approved).isPresent();
        assertThat(approved.orElseThrow().status()).isEqualTo(RunStatus.EXECUTING);
        assertThat(approved.orElseThrow().pendingApproval()).isNull();
    }

    @Test
    void shouldHideARunOwnedByAnotherUser() {
        InMemoryExecutionRunStore store = new InMemoryExecutionRunStore();
        UUID ownerId = UUID.randomUUID();
        ExecutionRun run = pendingRun(ownerId);
        store.save(run);

        Optional<ExecutionRunSnapshot> approved = new ApproveExecutionRunUseCase(store)
                .execute(UUID.randomUUID(), run.runId());

        assertThat(approved).isEmpty();
    }

    @Test
    void shouldRejectApprovalAndCancelThePendingRun() {
        InMemoryExecutionRunStore store = new InMemoryExecutionRunStore();
        UUID userId = UUID.randomUUID();
        ExecutionRun run = pendingRun(userId);
        store.save(run);

        Optional<ExecutionRunSnapshot> rejected = new RejectExecutionRunUseCase(store)
                .execute(userId, run.runId(), "Usuário recusou a alteração");

        assertThat(rejected).isPresent();
        assertThat(rejected.orElseThrow().status()).isEqualTo(RunStatus.CANCELLED);
        assertThat(rejected.orElseThrow().errorCode()).isEqualTo("APPROVAL_REJECTED");
    }

    @Test
    void shouldRejectApprovalActionWhenRunIsNotAwaitingApproval() {
        InMemoryExecutionRunStore store = new InMemoryExecutionRunStore();
        UUID userId = UUID.randomUUID();
        ExecutionRun run = ExecutionRun.receive(UUID.randomUUID(), userId, UUID.randomUUID(), "teste");
        store.save(run);

        assertThatThrownBy(() -> new ApproveExecutionRunUseCase(store).execute(userId, run.runId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RECEIVED");
    }

    private ExecutionRun pendingRun(UUID userId) {
        ExecutionRun run = ExecutionRun.receive(UUID.randomUUID(), userId, UUID.randomUUID(), "alterar arquivo");
        run.route("development");
        run.startExecution();
        run.requireApproval("workspace.write_file");
        return run;
    }

    private static final class InMemoryExecutionRunStore implements ExecutionRunStore {
        private final Map<UUID, ExecutionRunSnapshot> snapshots = new HashMap<>();

        @Override
        public ExecutionRunSnapshot save(ExecutionRun run) {
            ExecutionRunSnapshot snapshot = run.snapshot();
            snapshots.put(snapshot.runId(), snapshot);
            return snapshot;
        }

        @Override
        public Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId) {
            return Optional.ofNullable(snapshots.get(runId))
                    .filter(snapshot -> snapshot.userId().equals(userId));
        }
    }
}
