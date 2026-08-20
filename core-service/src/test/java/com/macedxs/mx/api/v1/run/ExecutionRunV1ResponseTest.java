package com.macedxs.mx.api.v1.run;

import com.macedxs.mx.core.application.run.ExecutionRunSnapshot;
import com.macedxs.mx.core.application.run.RunStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionRunV1ResponseTest {

    @Test
    void shouldMapPersistedRunStatusWithoutLosingApprovalOrTimingData() {
        UUID runId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        Instant receivedAt = Instant.now();
        Instant finishedAt = receivedAt.plusSeconds(2);
        ExecutionRunSnapshot snapshot = new ExecutionRunSnapshot(
                runId,
                UUID.randomUUID(),
                correlationId,
                "prompt",
                RunStatus.COMPLETED,
                "development",
                null,
                "answer",
                null,
                receivedAt,
                finishedAt
        );

        ExecutionRunV1Response response = ExecutionRunV1Response.from(snapshot);

        assertThat(response.runId()).isEqualTo(runId);
        assertThat(response.correlationId()).isEqualTo(correlationId);
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.skillName()).isEqualTo("development");
        assertThat(response.output()).isEqualTo("answer");
        assertThat(response.receivedAt()).isEqualTo(receivedAt);
        assertThat(response.finishedAt()).isEqualTo(finishedAt);
    }
}
