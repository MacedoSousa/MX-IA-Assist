package com.macedxs.mx.tool.repository;

import com.macedxs.mx.tool.entity.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ToolRepository extends JpaRepository<ToolEntity, UUID> {
    List<ToolEntity> findByEnabledTrue();
    Optional<ToolEntity> findByName(String name);
}
