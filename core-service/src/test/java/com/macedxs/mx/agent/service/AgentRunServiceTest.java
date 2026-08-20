package com.macedxs.mx.agent.service;

import com.macedxs.mx.agent.entity.AgentRunEntity;
import com.macedxs.mx.agent.entity.AgentRunStatus;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.service.AuthService;
import com.macedxs.mx.task.entity.TaskEntity;
import com.macedxs.mx.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AgentRunServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AgentRunService agentRunService;

    @Test
    void shouldTrackAgentExecutionForATask() {
        UserEntity user = authService.register("Agent User", "agent.user@example.com", "Strong123!");
        TaskEntity task = taskService.createTask(user, "Analyze project", "Review MX project structure");

        AgentRunEntity run = agentRunService.createRun(user, task, "coding-agent", "Review code and report");

        assertThat(run.getId()).isNotNull();
        assertThat(run.getAgentName()).isEqualTo("coding-agent");
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.RUNNING);
    }
}
