package com.macedxs.mx.task.repository;

import com.macedxs.mx.task.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByOwnerId(UUID ownerId);
}
