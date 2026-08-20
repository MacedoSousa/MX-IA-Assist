package com.macedxs.mx.core.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExecutionRunJpaRepository extends JpaRepository<ExecutionRunEntity, UUID> {

    Optional<ExecutionRunEntity> findByIdAndUserId(UUID id, UUID userId);
}
