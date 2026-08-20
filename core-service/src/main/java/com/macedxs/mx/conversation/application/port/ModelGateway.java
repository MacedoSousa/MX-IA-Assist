package com.macedxs.mx.conversation.application.port;

import java.time.Instant;
import java.util.UUID;

public interface ModelGateway {

    ModelResponse complete(ModelRequest request);

    record ModelRequest(UUID userId, String prompt, String idempotencyKey) {
        public ModelRequest {
            if (prompt == null || prompt.isBlank()) {
                throw new IllegalArgumentException("Prompt is required");
            }
            prompt = prompt.trim();
            idempotencyKey = normalize(idempotencyKey);
        }

        public ModelRequest(UUID userId, String prompt) {
            this(userId, prompt, null);
        }

        public ModelRequest(String prompt) {
            this(null, prompt, null);
        }

        private static String normalize(String value) {
            return value == null || value.isBlank() ? null : value.trim();
        }
    }

    record ModelResponse(
            String answer,
            String model,
            long durationMs,
            UUID correlationId,
            String skillName,
            UUID runId,
            String status,
            UUID approvalRunId,
            String approvalNonce,
            Instant approvalExpiresAt
    ) {
        public ModelResponse(String answer, String model, long durationMs) {
            this(answer, model, durationMs, null, null, null,
                    "COMPLETED", null, null, null);
        }

        public ModelResponse(
                String answer,
                String model,
                long durationMs,
                UUID correlationId,
                String skillName
        ) {
            this(answer, model, durationMs, correlationId, skillName, null,
                    "COMPLETED", null, null, null);
        }

        public ModelResponse(
                String answer,
                String model,
                long durationMs,
                UUID correlationId,
                String skillName,
                UUID runId
        ) {
            this(answer, model, durationMs, correlationId, skillName, runId,
                    "COMPLETED", null, null, null);
        }

        public ModelResponse {
            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException("Model status is required");
            }
            if (approvalRunId == null && (approvalNonce != null || approvalExpiresAt != null)) {
                throw new IllegalArgumentException("Approval metadata requires an approval run");
            }
        }
    }
}
