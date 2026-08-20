package com.macedxs.mx.core.application.run;

import java.util.Optional;
import java.util.UUID;

public class GetExecutionRunUseCase {

    private final ExecutionRunStore runStore;

    public GetExecutionRunUseCase(ExecutionRunStore runStore) {
        if (runStore == null) {
            throw new IllegalArgumentException("Execution run store is required");
        }
        this.runStore = runStore;
    }

    public Optional<ExecutionRunSnapshot> execute(UUID userId, UUID runId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (runId == null) {
            throw new IllegalArgumentException("Run id is required");
        }
        return runStore.findById(userId, runId);
    }
}
