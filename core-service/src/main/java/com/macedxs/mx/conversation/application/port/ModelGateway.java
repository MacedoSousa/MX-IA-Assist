package com.macedxs.mx.conversation.application.port;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ModelGateway {

    ModelResponse complete(ModelRequest request);

    record ModelRequest(UUID userId, String prompt, String idempotencyKey, List<ModelImage> images) {
        public ModelRequest {
            if (prompt == null || prompt.isBlank()) {
                throw new IllegalArgumentException("Prompt is required");
            }
            prompt = prompt.trim();
            idempotencyKey = normalize(idempotencyKey);
            images = images == null ? List.of() : List.copyOf(images);
        }

        public ModelRequest(UUID userId, String prompt, String idempotencyKey) {
            this(userId, prompt, idempotencyKey, List.of());
        }

        public ModelRequest(UUID userId, String prompt) {
            this(userId, prompt, null, List.of());
        }

        public ModelRequest(String prompt) {
            this(null, prompt, null, List.of());
        }

        public ModelRequest withImages(List<ModelImage> additionalImages) {
            return new ModelRequest(userId, prompt, idempotencyKey, additionalImages);
        }

        private static String normalize(String value) {
            return value == null || value.isBlank() ? null : value.trim();
        }
    }

    record ModelImage(String contentType, String base64Data) {
        public ModelImage {
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                throw new IllegalArgumentException("Model image content type is invalid");
            }
            if (base64Data == null || base64Data.isBlank()) {
                throw new IllegalArgumentException("Model image data is required");
            }
            contentType = contentType.trim().toLowerCase();
            base64Data = base64Data.trim();
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
