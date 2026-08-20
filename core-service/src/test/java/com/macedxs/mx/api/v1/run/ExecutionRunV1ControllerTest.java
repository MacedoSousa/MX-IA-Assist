package com.macedxs.mx.api.v1.run;

import com.macedxs.mx.core.application.run.ApproveExecutionRunUseCase;
import com.macedxs.mx.core.application.run.ExecutionRunSnapshot;
import com.macedxs.mx.core.application.run.GetExecutionRunUseCase;
import com.macedxs.mx.core.application.run.RejectExecutionRunUseCase;
import com.macedxs.mx.core.application.run.RunStatus;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExecutionRunV1ControllerTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldApproveRunForTheAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UserEntity user = mock(UserEntity.class);
        UserRepository userRepository = mock(UserRepository.class);
        GetExecutionRunUseCase getUseCase = mock(GetExecutionRunUseCase.class);
        ApproveExecutionRunUseCase approveUseCase = mock(ApproveExecutionRunUseCase.class);
        RejectExecutionRunUseCase rejectUseCase = mock(RejectExecutionRunUseCase.class);
        ExecutionRunSnapshot snapshot = snapshot(userId, runId, RunStatus.EXECUTING);

        when(user.getId()).thenReturn(userId);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(approveUseCase.execute(userId, runId)).thenReturn(Optional.of(snapshot));
        authenticate();

        ExecutionRunV1Controller controller = new ExecutionRunV1Controller(
                getUseCase,
                approveUseCase,
                rejectUseCase,
                userRepository
        );

        ResponseEntity<ExecutionRunV1Response> response = controller.approveRun(runId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("EXECUTING");
        verify(approveUseCase).execute(userId, runId);
    }

    @Test
    void shouldRejectRunWithTheReasonProvidedByTheAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UserEntity user = mock(UserEntity.class);
        UserRepository userRepository = mock(UserRepository.class);
        GetExecutionRunUseCase getUseCase = mock(GetExecutionRunUseCase.class);
        ApproveExecutionRunUseCase approveUseCase = mock(ApproveExecutionRunUseCase.class);
        RejectExecutionRunUseCase rejectUseCase = mock(RejectExecutionRunUseCase.class);
        ExecutionRunSnapshot snapshot = snapshot(userId, runId, RunStatus.CANCELLED);

        when(user.getId()).thenReturn(userId);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(rejectUseCase.execute(userId, runId, "não autorizar")).thenReturn(Optional.of(snapshot));
        authenticate();

        ExecutionRunV1Controller controller = new ExecutionRunV1Controller(
                getUseCase,
                approveUseCase,
                rejectUseCase,
                userRepository
        );

        ResponseEntity<ExecutionRunV1Response> response = controller.rejectRun(
                runId,
                new ApprovalRejectionV1Request("não autorizar")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("CANCELLED");
        verify(rejectUseCase).execute(userId, runId, "não autorizar");
    }

    @Test
    void shouldReturnNotFoundWhenApprovalActionIsOutsideTheUserScope() {
        UUID runId = UUID.randomUUID();
        UserEntity user = mock(UserEntity.class);
        UserRepository userRepository = mock(UserRepository.class);
        GetExecutionRunUseCase getUseCase = mock(GetExecutionRunUseCase.class);
        ApproveExecutionRunUseCase approveUseCase = mock(ApproveExecutionRunUseCase.class);
        RejectExecutionRunUseCase rejectUseCase = mock(RejectExecutionRunUseCase.class);

        when(user.getId()).thenReturn(UUID.randomUUID());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(approveUseCase.execute(user.getId(), runId)).thenReturn(Optional.empty());
        authenticate();

        ExecutionRunV1Controller controller = new ExecutionRunV1Controller(
                getUseCase,
                approveUseCase,
                rejectUseCase,
                userRepository
        );

        ResponseEntity<ExecutionRunV1Response> response = controller.approveRun(runId);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null)
        );
    }

    private ExecutionRunSnapshot snapshot(UUID userId, UUID runId, RunStatus status) {
        Instant receivedAt = Instant.now();
        return new ExecutionRunSnapshot(
                runId,
                userId,
                UUID.randomUUID(),
                "prompt",
                status,
                "development",
                status == RunStatus.AWAITING_APPROVAL ? "workspace.write_file" : null,
                null,
                status == RunStatus.CANCELLED ? "APPROVAL_REJECTED" : null,
                receivedAt,
                status == RunStatus.CANCELLED ? receivedAt : null
        );
    }
}
