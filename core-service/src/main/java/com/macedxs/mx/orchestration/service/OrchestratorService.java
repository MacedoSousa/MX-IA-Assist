package com.macedxs.mx.orchestration.service;

import com.macedxs.mx.agent.entity.AgentRunEntity;
import com.macedxs.mx.agent.entity.AgentRunStatus;
import com.macedxs.mx.agent.service.AgentRunService;
import com.macedxs.mx.ai.service.OllamaService;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.entity.ConversationMessageEntity;
import com.macedxs.mx.conversation.service.ConversationMemoryService;
import com.macedxs.mx.conversation.service.ConversationService;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.task.entity.TaskEntity;
import com.macedxs.mx.task.entity.TaskStatus;
import com.macedxs.mx.task.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrchestratorService {

    private final ConversationService conversationService;
    private final ConversationMemoryService memoryService;
    private final TaskService taskService;
    private final AgentRunService agentRunService;
    private final OllamaService ollamaService;

    public OrchestratorService(
            ConversationService conversationService,
            ConversationMemoryService memoryService,
            TaskService taskService,
            AgentRunService agentRunService,
            OllamaService ollamaService
    ) {
        this.conversationService = conversationService;
        this.memoryService = memoryService;
        this.taskService = taskService;
        this.agentRunService = agentRunService;
        this.ollamaService = ollamaService;
    }

    @Transactional
    public OrchestratorResponse handlePrompt(UserEntity user, String prompt) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }

        ConversationEntity conversation = conversationService.createConversation(user, "Nova conversa");
        TaskEntity task = taskService.createTask(user, "Process request", prompt);
        AgentRunEntity run = agentRunService.createRun(user, task, "mx-orchestrator", prompt);

        // Store user message in conversation memory
        memoryService.addMessage(conversation, ConversationMessageEntity.MessageRole.USER, prompt);

        // Generate response with context
        String answer = ollamaService.generateText(prompt);

        // Store assistant message in conversation memory
        memoryService.addMessage(conversation, ConversationMessageEntity.MessageRole.ASSISTANT, answer);

        taskService.updateStatus(task.getId(), TaskStatus.COMPLETED);
        agentRunService.finishRun(run.getId(), answer, AgentRunStatus.COMPLETED);

        return new OrchestratorResponse(
                conversation.getId(),
                task.getId(),
                run.getId(),
                answer
        );
    }

    public UUID createConversation(UserEntity user, String title) {
        return conversationService.createConversation(user, title).getId();
    }
}
