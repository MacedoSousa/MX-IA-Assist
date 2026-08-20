package com.macedxs.mx.core.infrastructure.persistence;

import com.macedxs.mx.core.application.run.ExecutionRun;
import com.macedxs.mx.core.application.run.ExecutionRunSnapshot;
import com.macedxs.mx.core.application.run.ExecutionRunStore;
import org.springframework.stereotype.Component;

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
}
