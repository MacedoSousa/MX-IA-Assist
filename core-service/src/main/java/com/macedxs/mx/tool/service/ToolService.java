package com.macedxs.mx.tool.service;

import com.macedxs.mx.tool.entity.ToolEntity;
import com.macedxs.mx.tool.entity.ToolExecutionEntity;
import com.macedxs.mx.tool.repository.ToolExecutionRepository;
import com.macedxs.mx.tool.repository.ToolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ToolService {

    private final ToolRepository toolRepository;
    private final ToolExecutionRepository executionRepository;

    public ToolService(ToolRepository toolRepository, ToolExecutionRepository executionRepository) {
        this.toolRepository = toolRepository;
        this.executionRepository = executionRepository;
    }

    @Transactional(readOnly = true)
    public List<ToolEntity> getAvailableTools() {
        return toolRepository.findByEnabledTrue();
    }

    @Transactional(readOnly = true)
    public ToolEntity findToolByName(String name) {
        return toolRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + name));
    }

    @Transactional
    public ToolExecutionEntity recordExecution(ToolEntity tool, String input) {
        if (tool == null || tool.getId() == null) {
            throw new IllegalArgumentException("Tool is required");
        }

        ToolExecutionEntity execution = new ToolExecutionEntity();
        execution.setTool(tool);
        execution.setInput(input);
        execution.setStatus(ToolExecutionEntity.ExecutionStatus.PENDING);
        return executionRepository.save(execution);
    }

    @Transactional
    public ToolExecutionEntity updateExecution(UUID executionId,
                                               String output,
                                               ToolExecutionEntity.ExecutionStatus status,
                                               Long executionTimeMs) {
        ToolExecutionEntity execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found"));

        execution.setOutput(output);
        execution.setStatus(status);
        execution.setExecutionTimeMs(executionTimeMs);
        return executionRepository.save(execution);
    }

    @Transactional
    public ToolExecutionEntity recordError(UUID executionId, String error) {
        ToolExecutionEntity execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found"));

        execution.setError(error);
        execution.setStatus(ToolExecutionEntity.ExecutionStatus.FAILED);
        return executionRepository.save(execution);
    }

    @Transactional(readOnly = true)
    public List<ToolExecutionEntity> getExecutionHistory(UUID toolId) {
        return executionRepository.findByToolIdOrderByCreatedAt(toolId);
    }
}
