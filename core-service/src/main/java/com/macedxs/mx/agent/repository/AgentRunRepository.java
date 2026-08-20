package com.macedxs.mx.agent.repository;

import com.macedxs.mx.agent.entity.AgentRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentRunRepository extends JpaRepository<AgentRunEntity, UUID> {
    List<AgentRunEntity> findByUserId(UUID userId);
}
