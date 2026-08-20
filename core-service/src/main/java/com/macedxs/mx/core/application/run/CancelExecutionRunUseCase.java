package com.macedxs.mx.core.application.run;

import java.util.Optional;
import java.util.UUID;

public final class CancelExecutionRunUseCase {

    private final ExecutionRunStore runStore;

    public CancelExecutionRunUseCase(ExecutionRunStore runStore) {
        if (runStore == null) {
            throw new IllegalArgumentException("Execution run store is required");
        }
        this.runStore = runStore;
    }

    public Optional<ExecutionRunSnapshot> execute(UUID userId, UUID runId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (runId == null) {
            throw new IllegalArgumentException("Run id is required");
        }
        return runStore.findById(userId, runId).map(snapshot -> {
            ExecutionRun run = ExecutionRun.restore(snapshot);
            run.cancel();
            return runStore.save(run);
        });
    }
}
