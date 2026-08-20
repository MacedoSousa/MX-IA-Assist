package com.macedxs.mx.core.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRunJpaRepository extends JpaRepository<ExecutionRunEntity, UUID> {

    Optional<ExecutionRunEntity> findByIdAndUserId(UUID id, UUID userId);

    Optional<ExecutionRunEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    List<ExecutionRunEntity> findByUserIdAndUpdatedAtGreaterThanOrderByUpdatedAtAscIdAsc(
            UUID userId,
            Instant updatedAt,
            Pageable pageable
    );
}
