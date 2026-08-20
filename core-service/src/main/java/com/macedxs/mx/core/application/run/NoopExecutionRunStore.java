package com.macedxs.mx.core.application.run;

import java.util.Optional;
import java.util.UUID;

public class NoopExecutionRunStore implements ExecutionRunStore {

    @Override
    public ExecutionRunSnapshot save(ExecutionRun run) {
        return run.snapshot();
    }

    @Override
    public Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId) {
        return Optional.empty();
    }
}
