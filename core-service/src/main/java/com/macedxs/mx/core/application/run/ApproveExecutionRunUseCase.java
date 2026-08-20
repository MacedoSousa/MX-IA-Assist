package com.macedxs.mx.core.application.run;

import java.util.Optional;
import java.util.UUID;

public class ApproveExecutionRunUseCase {

    private final ExecutionRunStore runStore;

    public ApproveExecutionRunUseCase(ExecutionRunStore runStore) {
        if (runStore == null) {
            throw new IllegalArgumentException("Execution run store is required");
        }
        this.runStore = runStore;
    }

    public Optional<ExecutionRunSnapshot> execute(UUID userId, UUID runId) {
        require(userId, "User id");
        require(runId, "Run id");

        return runStore.findById(userId, runId).map(snapshot -> {
            if (snapshot.status() != RunStatus.AWAITING_APPROVAL) {
                throw new ExecutionRunApprovalException("Execution run is not awaiting approval: " + snapshot.status());
            }
            ExecutionRun run = ExecutionRun.restore(snapshot);
            run.resumeAfterApproval();
            return runStore.save(run);
        });
    }

    private static void require(UUID value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
