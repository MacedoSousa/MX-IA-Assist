package com.macedxs.mx.api.v1.run;

import com.macedxs.mx.core.application.run.ApproveExecutionRunUseCase;
import com.macedxs.mx.core.application.run.ExecutionRunApprovalException;
import com.macedxs.mx.core.application.run.GetExecutionRunUseCase;
import com.macedxs.mx.core.application.run.RejectExecutionRunUseCase;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/runs")
public class ExecutionRunV1Controller {

    private final GetExecutionRunUseCase getExecutionRunUseCase;
    private final ApproveExecutionRunUseCase approveExecutionRunUseCase;
    private final RejectExecutionRunUseCase rejectExecutionRunUseCase;
    private final UserRepository userRepository;

    public ExecutionRunV1Controller(
            GetExecutionRunUseCase getExecutionRunUseCase,
            ApproveExecutionRunUseCase approveExecutionRunUseCase,
            RejectExecutionRunUseCase rejectExecutionRunUseCase,
            UserRepository userRepository
    ) {
        this.getExecutionRunUseCase = getExecutionRunUseCase;
        this.approveExecutionRunUseCase = approveExecutionRunUseCase;
        this.rejectExecutionRunUseCase = rejectExecutionRunUseCase;
        this.userRepository = userRepository;
    }

    @GetMapping("/{runId}")
    public ResponseEntity<ExecutionRunV1Response> getRun(@PathVariable UUID runId) {
        UserEntity user = currentUser();
        return getExecutionRunUseCase.execute(user.getId(), runId)
                .map(snapshot -> ResponseEntity.ok(ExecutionRunV1Response.from(snapshot)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{runId}/approve")
    public ResponseEntity<ExecutionRunV1Response> approveRun(@PathVariable UUID runId) {
        UserEntity user = currentUser();
        try {
            return approveExecutionRunUseCase.execute(user.getId(), runId)
                    .map(snapshot -> ResponseEntity.ok(ExecutionRunV1Response.from(snapshot)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (ExecutionRunApprovalException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{runId}/reject")
    public ResponseEntity<ExecutionRunV1Response> rejectRun(
            @PathVariable UUID runId,
            @RequestBody ApprovalRejectionV1Request request
    ) {
        UserEntity user = currentUser();
        try {
            return rejectExecutionRunUseCase.execute(user.getId(), runId, request.reason())
                    .map(snapshot -> ResponseEntity.ok(ExecutionRunV1Response.from(snapshot)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (ExecutionRunApprovalException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
