package com.macedxs.mx.task.controller;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import com.macedxs.mx.task.dto.TaskDTO;
import com.macedxs.mx.task.entity.TaskEntity;
import com.macedxs.mx.task.entity.TaskStatus;
import com.macedxs.mx.task.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> findMyTasks() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<TaskDTO> tasks = taskService.findByOwner(user.getId())
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TaskDTO> createTask(@RequestBody CreateTaskRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TaskEntity task = taskService.createTask(user, request.title(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(entityToDto(task));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable UUID id) {
        // Verify ownership
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TaskEntity task = taskService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!task.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(entityToDto(task));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(@PathVariable UUID id, @RequestBody UpdateTaskStatusRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TaskEntity task = taskService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!task.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TaskEntity updated = taskService.updateStatus(id, request.status());
        return ResponseEntity.ok(entityToDto(updated));
    }

    private TaskDTO entityToDto(TaskEntity entity) {
        return new TaskDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDueAt()
        );
    }

    public record CreateTaskRequest(String title, String description) {}
    public record UpdateTaskStatusRequest(TaskStatus status) {}
}
