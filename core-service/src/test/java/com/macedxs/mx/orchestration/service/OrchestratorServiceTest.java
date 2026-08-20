package com.macedxs.mx.orchestration.service;

import com.macedxs.mx.agent.entity.AgentRunEntity;
import com.macedxs.mx.agent.entity.AgentRunStatus;
import com.macedxs.mx.agent.service.AgentRunService;
import com.macedxs.mx.ai.service.OllamaService;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.service.ConversationMemoryService;
import com.macedxs.mx.conversation.service.ConversationService;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.task.entity.TaskEntity;
import com.macedxs.mx.task.entity.TaskStatus;
import com.macedxs.mx.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrchestratorServiceTest {

    @Test
    void shouldCreateConversationTaskAndAgentRunForAUserPrompt() {
        ConversationService conversationService = mock(ConversationService.class);
        ConversationMemoryService memoryService = mock(ConversationMemoryService.class);
        TaskService taskService = mock(TaskService.class);
        AgentRunService agentRunService = mock(AgentRunService.class);
        OllamaService ollamaService = mock(OllamaService.class);

        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("user@example.com");

        ConversationEntity conversation = new ConversationEntity();
        conversation.setTitle("Nova conversa");

        TaskEntity task = new TaskEntity();
        task.setTitle("Process request");
        task.setStatus(TaskStatus.PENDING);

        AgentRunEntity run = new AgentRunEntity();
        run.setAgentName("mx-orchestrator");
        run.setStatus(AgentRunStatus.RUNNING);

        when(conversationService.createConversation(user, "Nova conversa")).thenReturn(conversation);
        when(taskService.createTask(user, "Process request", "Test prompt")).thenReturn(task);
        when(agentRunService.createRun(user, task, "mx-orchestrator", "Test prompt")).thenReturn(run);
        when(ollamaService.generateText("Test prompt")).thenReturn("Resposta do orquestrador");

        OrchestratorService service = new OrchestratorService(conversationService, memoryService, taskService, agentRunService, ollamaService);

        OrchestratorResponse response = service.handlePrompt(user, "Test prompt");

        assertThat(response.getAnswer()).isEqualTo("Resposta do orquestrador");
        assertThat(response.getTaskId()).isEqualTo(task.getId());
    }
}
