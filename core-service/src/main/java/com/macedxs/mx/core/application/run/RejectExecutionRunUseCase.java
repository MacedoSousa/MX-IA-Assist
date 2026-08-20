package com.macedxs.mx.core.application.run;

import java.util.Optional;
import java.util.UUID;

public class RejectExecutionRunUseCase {

    private final ExecutionRunStore runStore;

    public RejectExecutionRunUseCase(ExecutionRunStore runStore) {
        if (runStore == null) {
            throw new IllegalArgumentException("Execution run store is required");
        }
        this.runStore = runStore;
    }

    public Optional<ExecutionRunSnapshot> execute(UUID userId, UUID runId, String reason) {
        require(userId, "User id");
        require(runId, "Run id");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        return runStore.findById(userId, runId).map(snapshot -> {
            if (snapshot.status() != RunStatus.AWAITING_APPROVAL) {
                throw new ExecutionRunApprovalException("Execution run is not awaiting approval: " + snapshot.status());
            }
            ExecutionRun run = ExecutionRun.restore(snapshot);
            run.rejectApproval(reason);
            return runStore.save(run);
        });
    }

    private static void require(UUID value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
