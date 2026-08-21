package com.macedxs.mx.attachment.repository;

import com.macedxs.mx.attachment.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<AttachmentEntity, UUID> {

    Optional<AttachmentEntity> findByIdAndUserId(UUID id, UUID userId);

    List<AttachmentEntity> findAllByIdInAndUserId(List<UUID> ids, UUID userId);
}
