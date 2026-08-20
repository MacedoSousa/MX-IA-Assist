package com.macedxs.mx.orchestration.dto;

import java.util.UUID;

public record ChatResponse(
        UUID conversationId,
        UUID taskId,
        UUID agentRunId,
        String answer
) {}
