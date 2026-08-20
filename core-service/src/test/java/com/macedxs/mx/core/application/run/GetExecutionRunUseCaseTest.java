package com.macedxs.mx.core.application.run;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GetExecutionRunUseCaseTest {

    @Test
    void shouldReturnRunScopedToTheAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        ExecutionRunSnapshot expected = snapshot(runId, userId, RunStatus.COMPLETED);
        ExecutionRunStore store = new StubStore(expected);

        Optional<ExecutionRunSnapshot> result = new GetExecutionRunUseCase(store).execute(userId, runId);

        assertThat(result).contains(expected);
    }

    @Test
    void shouldReturnEmptyWhenRunDoesNotBelongToTheAuthenticatedUser() {
        UUID runId = UUID.randomUUID();
        ExecutionRunStore store = new StubStore(snapshot(runId, UUID.randomUUID(), RunStatus.COMPLETED));

        assertThat(new GetExecutionRunUseCase(store).execute(UUID.randomUUID(), runId)).isEmpty();
    }

    private static ExecutionRunSnapshot snapshot(UUID runId, UUID userId, RunStatus status) {
        return new ExecutionRunSnapshot(
                runId,
                userId,
                UUID.randomUUID(),
                "prompt",
                status,
                "general",
                null,
                status == RunStatus.COMPLETED ? "answer" : null,
                null,
                Instant.now(),
                status == RunStatus.COMPLETED ? Instant.now() : null
        );
    }

    private record StubStore(ExecutionRunSnapshot snapshot) implements ExecutionRunStore {
        @Override
        public ExecutionRunSnapshot save(ExecutionRun run) {
            return run.snapshot();
        }

        @Override
        public Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId) {
            if (snapshot.userId().equals(userId) && snapshot.runId().equals(runId)) {
                return Optional.of(snapshot);
            }
            return Optional.empty();
        }
    }
}
