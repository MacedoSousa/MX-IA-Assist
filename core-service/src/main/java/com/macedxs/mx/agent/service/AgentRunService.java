package com.macedxs.mx.agent.service;

import com.macedxs.mx.agent.entity.AgentRunEntity;
import com.macedxs.mx.agent.entity.AgentRunStatus;
import com.macedxs.mx.agent.repository.AgentRunRepository;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.task.entity.TaskEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgentRunService {

    private final AgentRunRepository agentRunRepository;

    public AgentRunService(AgentRunRepository agentRunRepository) {
        this.agentRunRepository = agentRunRepository;
    }

    @Transactional
    public AgentRunEntity createRun(UserEntity user, TaskEntity task, String agentName, String input) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (agentName == null || agentName.isBlank()) {
            throw new IllegalArgumentException("Agent name is required");
        }

        AgentRunEntity run = new AgentRunEntity();
        run.setUser(user);
        run.setTask(task);
        run.setAgentName(agentName);
        run.setInput(input);
        run.setStatus(AgentRunStatus.RUNNING);
        return agentRunRepository.save(run);
    }

    @Transactional
    public AgentRunEntity finishRun(UUID runId, String output, AgentRunStatus status) {
        AgentRunEntity run = agentRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Agent run not found"));
        run.setOutput(output);
        run.setStatus(status);
        run.setFinishedAt(LocalDateTime.now());
        return agentRunRepository.save(run);
    }

    @Transactional(readOnly = true)
    public List<AgentRunEntity> findByUser(UUID userId) {
        return agentRunRepository.findByUserId(userId);
    }
}
