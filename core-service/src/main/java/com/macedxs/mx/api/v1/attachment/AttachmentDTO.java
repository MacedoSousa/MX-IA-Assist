package com.macedxs.mx.api.v1.attachment;

import com.macedxs.mx.attachment.entity.AttachmentEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentDTO(
        UUID id,
        String filename,
        String contentType,
        long size,
        String checksumSha256,
        LocalDateTime createdAt
) {
    public static AttachmentDTO from(AttachmentEntity entity) {
        return new AttachmentDTO(
                entity.getId(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getSize(),
                entity.getChecksumSha256(),
                entity.getCreatedAt()
        );
    }
}
