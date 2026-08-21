package com.macedxs.mx.attachment.service;

import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.attachment.repository.AttachmentRepository;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttachmentServiceTest {

    private final AttachmentRepository repository = mock(AttachmentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    @Test
    void shouldStoreTextWithChecksumAndResolveItForModel(@TempDir Path tempDir) throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = mock(UserEntity.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any(AttachmentEntity.class))).thenAnswer(invocation -> persistAsNew(invocation.getArgument(0)));
        AttachmentService service = new AttachmentService(repository, userRepository, tempDir, 1024, 1024, 1024);
        byte[] content = "conteúdo seguro".getBytes(StandardCharsets.UTF_8);

        AttachmentEntity stored = service.store(
                userId,
                "../anotacoes.txt",
                "text/plain",
                content.length,
                new ByteArrayInputStream(content)
        );

        assertThat(stored.getUser()).isSameAs(user);
        assertThat(stored.getOriginalFilename()).isEqualTo("anotacoes.txt");
        assertThat(stored.getChecksumSha256()).hasSize(64);
        assertThat(Files.readAllBytes(tempDir.resolve(stored.getStoredFilename())))
                .isEqualTo(content);
        when(repository.findAllByIdInAndUserId(List.of(stored.getId()), userId))
                .thenReturn(List.of(stored));

        AttachmentService.ResolvedAttachments resolved = service.resolveForModel(userId, List.of(stored.getId()));

        assertThat(resolved.textContext()).contains("anotacoes.txt").contains("conteúdo seguro");
        assertThat(resolved.images()).isEmpty();
    }

    @Test
    void shouldStorePdfAttachmentWithPdfContentType(@TempDir Path tempDir) throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = mock(UserEntity.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any(AttachmentEntity.class))).thenAnswer(invocation -> persistAsNew(invocation.getArgument(0)));
        AttachmentService service = new AttachmentService(repository, userRepository, tempDir, 4096, 4096, 4096);
        byte[] pdfHeader = "%PDF-1.7\\n".getBytes(StandardCharsets.US_ASCII);

        AttachmentEntity stored = service.store(
                userId,
                "documento.pdf",
                "application/pdf",
                pdfHeader.length,
                new ByteArrayInputStream(pdfHeader)
        );

        assertThat(stored.getOriginalFilename()).isEqualTo("documento.pdf");
        assertThat(stored.getContentType()).isEqualTo("application/pdf");
        assertThat(Files.readAllBytes(tempDir.resolve(stored.getStoredFilename())))
                .isEqualTo(pdfHeader);
    }

    @Test
    void shouldStorePdfWhenBrowserSendsGenericMime(@TempDir Path tempDir) throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = mock(UserEntity.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any(AttachmentEntity.class))).thenAnswer(invocation -> persistAsNew(invocation.getArgument(0)));
        AttachmentService service = new AttachmentService(repository, userRepository, tempDir, 4096, 4096, 4096);
        byte[] pdfHeader = "%PDF-1.7\\n".getBytes(StandardCharsets.US_ASCII);

        AttachmentEntity stored = service.store(
                userId,
                "documento.pdf",
                "application/octet-stream",
                pdfHeader.length,
                new ByteArrayInputStream(pdfHeader)
        );

        assertThat(stored.getContentType()).isEqualTo("application/pdf");
    }

    @Test
    void shouldRejectUnsupportedMimeAndOversizedFiles(@TempDir Path tempDir) {
        AttachmentService service = new AttachmentService(repository, userRepository, tempDir, 4, 4, 4);

        assertThatThrownBy(() -> service.store(
                UUID.randomUUID(), "payload.exe", "application/x-msdownload", 1,
                new ByteArrayInputStream(new byte[]{1})
        )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unsupported attachment");

        assertThatThrownBy(() -> service.store(
                UUID.randomUUID(), "large.txt", "text/plain", 5,
                new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5})
        )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("exceeds");
    }

    @Test
    void shouldRejectPathTraversalAndForeignAttachmentReferences(@TempDir Path tempDir) {
        UUID userId = UUID.randomUUID();
        AttachmentService service = new AttachmentService(repository, userRepository, tempDir, 1024, 1024, 1024);

        assertThatThrownBy(() -> service.safePath("../outside.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid attachment path");
        assertThatThrownBy(() -> service.safePath("nested\\outside.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid attachment path");

        when(repository.findAllByIdInAndUserId(any(), any())).thenReturn(List.of());
        assertThatThrownBy(() -> service.resolveForModel(userId, List.of(UUID.randomUUID())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
        assertThatThrownBy(() -> service.resolveForModel(userId, List.of(UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldCreateImagePayloadOnlyForOwnedImage(@TempDir Path tempDir) throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = mock(UserEntity.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any(AttachmentEntity.class))).thenAnswer(invocation -> persistAsNew(invocation.getArgument(0)));
        AttachmentService service = new AttachmentService(repository, userRepository, tempDir, 1024, 1024, 1024);
        AttachmentEntity image = service.store(
                userId,
                "diagram.png",
                "image/png",
                3,
                new ByteArrayInputStream(new byte[]{1, 2, 3})
        );
        when(repository.findAllByIdInAndUserId(List.of(image.getId()), userId))
                .thenReturn(List.of(image));

        AttachmentService.ResolvedAttachments resolved = service.resolveForModel(userId, List.of(image.getId()));

        assertThat(resolved.textContext()).contains("Imagem anexada: diagram.png");
        assertThat(resolved.images()).hasSize(1);
        assertThat(resolved.images().getFirst().contentType()).isEqualTo("image/png");
        assertThat(resolved.images().getFirst().base64Data()).isNotBlank();
    }

    private AttachmentEntity persistAsNew(AttachmentEntity entity) {
        assertThat(entity.getId()).isNull();
        entity.setId(UUID.randomUUID());
        return entity;
    }
}
