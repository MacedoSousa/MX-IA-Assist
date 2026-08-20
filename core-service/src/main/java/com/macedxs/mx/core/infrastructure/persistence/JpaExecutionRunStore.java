package com.macedxs.mx.core.infrastructure.persistence;

import com.macedxs.mx.core.application.run.ExecutionRun;
import com.macedxs.mx.core.application.run.ExecutionRunSnapshot;
import com.macedxs.mx.core.application.run.ExecutionRunStore;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaExecutionRunStore implements ExecutionRunStore {

    private final ExecutionRunJpaRepository repository;

    public JpaExecutionRunStore(ExecutionRunJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ExecutionRunSnapshot save(ExecutionRun run) {
        if (run == null) {
            throw new IllegalArgumentException("Execution run is required");
        }
        return repository.save(ExecutionRunEntity.fromSnapshot(run.snapshot())).toSnapshot();
    }

    @Override
    public Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId) {
        if (userId == null || runId == null) {
            return Optional.empty();
        }
        return repository.findByIdAndUserId(runId, userId).map(ExecutionRunEntity::toSnapshot);
    }

    @Override
    public Optional<ExecutionRunSnapshot> findByIdempotencyKey(UUID userId, String idempotencyKey) {
        if (userId == null || idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey.trim())
                .map(ExecutionRunEntity::toSnapshot);
    }

    @Override
    public List<ExecutionRunSnapshot> findUpdatedSince(UUID userId, Instant updatedSince, int limit) {
        if (userId == null || updatedSince == null || limit <= 0) {
            return List.of();
        }
        int boundedLimit = Math.min(limit, 200);
        return repository.findByUserIdAndUpdatedAtGreaterThanOrderByUpdatedAtAscIdAsc(
                        userId,
                        updatedSince,
                        PageRequest.of(0, boundedLimit)
                )
                .stream()
                .map(ExecutionRunEntity::toSnapshot)
                .toList();
    }
}
