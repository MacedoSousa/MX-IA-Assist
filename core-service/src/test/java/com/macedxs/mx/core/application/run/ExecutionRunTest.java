package com.macedxs.mx.core.application.run;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionRunTest {

    @Test
    void shouldMoveFromReceivedToCompletedThroughValidLifecycle() {
        ExecutionRun run = receivedRun();

        run.route("development");
        run.startExecution();
        run.beginVerification();
        run.complete("resposta validada");

        assertThat(run.status()).isEqualTo(RunStatus.COMPLETED);
        assertThat(run.output()).isEqualTo("resposta validada");
        assertThat(run.finishedAt()).isNotNull();
    }

    @Test
    void shouldSupportApprovalPauseAndResume() {
        ExecutionRun run = receivedRun();

        run.route("development");
        run.startExecution();
        run.requireApproval("workspace.write_file");

        assertThat(run.status()).isEqualTo(RunStatus.AWAITING_APPROVAL);
        assertThat(run.pendingApproval()).isEqualTo("workspace.write_file");

        run.resumeAfterApproval();
        run.beginVerification();
        run.complete("ação aprovada");

        assertThat(run.status()).isEqualTo(RunStatus.COMPLETED);
    }

    @Test
    void shouldRejectInvalidTransitionAndPreserveCurrentState() {
        ExecutionRun run = receivedRun();

        assertThatThrownBy(() -> run.complete("não deveria concluir"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete");
        assertThat(run.status()).isEqualTo(RunStatus.RECEIVED);
    }

    @Test
    void shouldFailAnActiveRunWithSafeErrorCode() {
        ExecutionRun run = receivedRun();
        run.route("general");
        run.fail("MODEL_UNAVAILABLE");

        assertThat(run.status()).isEqualTo(RunStatus.FAILED);
        assertThat(run.errorCode()).isEqualTo("MODEL_UNAVAILABLE");
        assertThat(run.finishedAt()).isNotNull();
    }

    private ExecutionRun receivedRun() {
        return ExecutionRun.receive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pedido de teste"
        );
    }
}
