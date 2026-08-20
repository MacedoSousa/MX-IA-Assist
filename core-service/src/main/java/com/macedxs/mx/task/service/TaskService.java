package com.macedxs.mx.task.service;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.task.entity.TaskEntity;
import com.macedxs.mx.task.entity.TaskStatus;
import com.macedxs.mx.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskEntity createTask(UserEntity owner, String title, String description) {
        if (owner == null || owner.getId() == null) {
            throw new IllegalArgumentException("Owner is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        TaskEntity task = new TaskEntity();
        task.setOwner(owner);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(TaskStatus.PENDING);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Optional<TaskEntity> findById(UUID taskId) {
        return taskRepository.findById(taskId);
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> findByOwner(UUID ownerId) {
        return taskRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public TaskEntity updateStatus(UUID taskId, TaskStatus status) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setStatus(status);
        return taskRepository.save(task);
    }
}
