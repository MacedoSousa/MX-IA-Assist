package com.macedxs.mx.task.dto;

import com.macedxs.mx.task.entity.TaskStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDTO(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime dueAt
) {}
