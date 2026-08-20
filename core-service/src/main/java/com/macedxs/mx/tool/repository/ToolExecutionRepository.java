package com.macedxs.mx.tool.repository;

import com.macedxs.mx.tool.entity.ToolExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ToolExecutionRepository extends JpaRepository<ToolExecutionEntity, UUID> {
    List<ToolExecutionEntity> findByToolIdOrderByCreatedAt(UUID toolId);
}
