package com.macedxs.mx.conversation.application.port;

import java.util.UUID;

public interface ModelGateway {

    ModelResponse complete(ModelRequest request);

    record ModelRequest(UUID userId, String prompt) {
        public ModelRequest {
            if (prompt == null || prompt.isBlank()) {
                throw new IllegalArgumentException("Prompt is required");
            }
            prompt = prompt.trim();
        }

        public ModelRequest(String prompt) {
            this(null, prompt);
        }
    }

    record ModelResponse(
            String answer,
            String model,
            long durationMs,
            UUID correlationId,
            String skillName,
            UUID runId
    ) {
        public ModelResponse(String answer, String model, long durationMs) {
            this(answer, model, durationMs, null, null, null);
        }

        public ModelResponse(
                String answer,
                String model,
                long durationMs,
                UUID correlationId,
                String skillName
        ) {
            this(answer, model, durationMs, correlationId, skillName, null);
        }
    }
}
