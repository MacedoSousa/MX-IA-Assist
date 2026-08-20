package com.macedxs.mx.core.application.run;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ListExecutionRunsUseCase {

    private final ExecutionRunStore runStore;

    public ListExecutionRunsUseCase(ExecutionRunStore runStore) {
        if (runStore == null) {
            throw new IllegalArgumentException("Execution run store is required");
        }
        this.runStore = runStore;
    }

    public List<ExecutionRunSnapshot> execute(UUID userId, Instant updatedSince, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (updatedSince == null) {
            throw new IllegalArgumentException("updatedSince is required");
        }
        if (limit < 1 || limit > 200) {
            throw new IllegalArgumentException("Limit must be between 1 and 200");
        }
        return runStore.findUpdatedSince(userId, updatedSince, limit);
    }
}
