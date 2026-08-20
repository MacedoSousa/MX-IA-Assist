package com.macedxs.mx.core.application.run;

import java.util.Optional;
import java.util.UUID;

public interface ExecutionRunStore {

    ExecutionRunSnapshot save(ExecutionRun run);

    Optional<ExecutionRunSnapshot> findById(UUID userId, UUID runId);
}
