package com.macedxs.mx.orchestration.service;

import java.util.UUID;

public class OrchestratorResponse {
    private final UUID conversationId;
    private final UUID taskId;
    private final UUID agentRunId;
    private final String answer;

    public OrchestratorResponse(UUID conversationId, UUID taskId, UUID agentRunId, String answer) {
        this.conversationId = conversationId;
        this.taskId = taskId;
        this.agentRunId = agentRunId;
        this.answer = answer;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getAgentRunId() {
        return agentRunId;
    }

    public String getAnswer() {
        return answer;
    }
}
