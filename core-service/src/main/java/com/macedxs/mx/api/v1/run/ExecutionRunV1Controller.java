package com.macedxs.mx.api.v1.run;

import com.macedxs.mx.core.application.run.ApproveExecutionRunUseCase;
import com.macedxs.mx.core.application.run.ApprovedToolExecutionUseCase;
import com.macedxs.mx.core.application.run.CancelExecutionRunUseCase;
import com.macedxs.mx.core.application.run.ExecutionRunApprovalException;
import com.macedxs.mx.core.application.run.GetExecutionRunUseCase;
import com.macedxs.mx.core.application.run.ListExecutionRunsUseCase;
import com.macedxs.mx.core.application.run.RejectExecutionRunUseCase;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/runs")
public class ExecutionRunV1Controller {

    private final GetExecutionRunUseCase getExecutionRunUseCase;
    private final ListExecutionRunsUseCase listExecutionRunsUseCase;
    private final ApproveExecutionRunUseCase approveExecutionRunUseCase;
    private final ApprovedToolExecutionUseCase approvedToolExecutionUseCase;
    private final CancelExecutionRunUseCase cancelExecutionRunUseCase;
    private final RejectExecutionRunUseCase rejectExecutionRunUseCase;
    private final UserRepository userRepository;

    public ExecutionRunV1Controller(
            GetExecutionRunUseCase getExecutionRunUseCase,
            ApproveExecutionRunUseCase approveExecutionRunUseCase,
            RejectExecutionRunUseCase rejectExecutionRunUseCase,
            UserRepository userRepository
    ) {
        this(getExecutionRunUseCase, null, approveExecutionRunUseCase, null, rejectExecutionRunUseCase, null, userRepository);
    }

    @Autowired
    public ExecutionRunV1Controller(
            GetExecutionRunUseCase getExecutionRunUseCase,
            ListExecutionRunsUseCase listExecutionRunsUseCase,
            ApproveExecutionRunUseCase approveExecutionRunUseCase,
            CancelExecutionRunUseCase cancelExecutionRunUseCase,
            RejectExecutionRunUseCase rejectExecutionRunUseCase,
            ApprovedToolExecutionUseCase approvedToolExecutionUseCase,
            UserRepository userRepository
    ) {
        this.getExecutionRunUseCase = getExecutionRunUseCase;
        this.listExecutionRunsUseCase = listExecutionRunsUseCase;
        this.approveExecutionRunUseCase = approveExecutionRunUseCase;
        this.cancelExecutionRunUseCase = cancelExecutionRunUseCase;
        this.rejectExecutionRunUseCase = rejectExecutionRunUseCase;
        this.approvedToolExecutionUseCase = approvedToolExecutionUseCase;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<ExecutionRunV1Response>> listRuns(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedSince,
            @RequestParam(defaultValue = "100") int limit
    ) {
        UserEntity user = currentUser();
        Instant cursor = updatedSince == null ? Instant.EPOCH : updatedSince;
        return ResponseEntity.ok(listExecutionRunsUseCase.execute(user.getId(), cursor, limit)
                .stream()
                .map(ExecutionRunV1Response::from)
                .toList());
    }

    @GetMapping("/{runId}")
    public ResponseEntity<ExecutionRunV1Response> getRun(@PathVariable UUID runId) {
        UserEntity user = currentUser();
        return getExecutionRunUseCase.execute(user.getId(), runId)
                .map(snapshot -> ResponseEntity.ok(ExecutionRunV1Response.from(snapshot)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<ExecutionRunV1Response> approveRun(UUID runId) {
        UserEntity user = currentUser();
        return approveExecutionRunUseCase.execute(user.getId(), runId)
                .map(snapshot -> ResponseEntity.ok(ExecutionRunV1Response.from(snapshot)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{runId}/approve")
    public ResponseEntity<ExecutionRunV1Response> approveRun(
            @PathVariable UUID runId,
            @RequestBody(required = false) ApprovalV1Request request
    ) {
        UserEntity user = currentUser();
        try {
            String nonce = request == null ? null : request.approvalNonce();
            if (approvedToolExecutionUseCase == null) {
                return approveExecutionRunUseCase.execute(user.getId(), runId, nonce)
                        .map(snapshot -> ResponseEntity.ok(ExecutionRunV1Response.from(snapshot)))
                        .orElseGet(() -> ResponseEntity.notFound().build());
            }
            return approvedToolExecutionUseCase.execute(user.getId(), runId, nonce)
                    .map(snapshot -> ResponseEntity.ok(ExecutionRunV1Response.from(snapshot)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (ExecutionRunApprovalException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{runId}/cancel")
    public ResponseEntity<ExecutionRunV1Response> cancelRun(@PathVariable UUID runId) {
        UserEntity user = currentUser();
        try {
            return cancelExecutionRunUseCase.execute(user.getId(), runId)
                    .map(snapshot -> ResponseEntity.ok(ExecutionRunV1Response.from(snapshot)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException exception) {
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
