package com.macedxs.mx.core.application.run;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRunStore {

    ExecutionRunSnapshot save(ExecutionRun run);

    Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId);

    default Optional<ExecutionRunSnapshot> findByIdempotencyKey(UUID userId, String idempotencyKey) {
        return Optional.empty();
    }

    default List<ExecutionRunSnapshot> findUpdatedSince(UUID userId, Instant updatedSince, int limit) {
        return List.of();
    }
}
