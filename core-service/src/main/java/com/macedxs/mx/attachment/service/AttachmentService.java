package com.macedxs.mx.attachment.service;

import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.attachment.repository.AttachmentRepository;
import com.macedxs.mx.conversation.application.port.ModelGateway.ModelImage;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AttachmentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "text/plain",
            "text/markdown",
            "text/csv",
            "application/json",
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/webp",
            "audio/mpeg",
            "audio/wav",
            "audio/ogg",
            "audio/mp4"
    );

    private final AttachmentRepository repository;
    private final UserRepository userRepository;
    private final Path root;
    private final long maxBytes;
    private final long maxTextBytes;
    private final long maxVisionBytes;

    @Autowired
    public AttachmentService(
            AttachmentRepository repository,
            UserRepository userRepository,
            @Value("${mx.attachments.root:./workspace/attachments}") String root,
            @Value("${mx.attachments.max-bytes:26214400}") long maxBytes,
            @Value("${mx.attachments.max-text-bytes:262144}") long maxTextBytes,
            @Value("${mx.attachments.max-vision-bytes:5242880}") long maxVisionBytes
    ) {
        this(repository, userRepository, Path.of(root), maxBytes, maxTextBytes, maxVisionBytes);
    }

    AttachmentService(
            AttachmentRepository repository,
            UserRepository userRepository,
            Path root,
            long maxBytes,
            long maxTextBytes,
            long maxVisionBytes
    ) {
        if (repository == null || userRepository == null || root == null) {
            throw new IllegalArgumentException("Attachment dependencies are required");
        }
        if (maxBytes <= 0 || maxTextBytes <= 0 || maxVisionBytes <= 0) {
            throw new IllegalArgumentException("Attachment limits must be positive");
        }
        this.repository = repository;
        this.userRepository = userRepository;
        this.root = root.toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        this.maxTextBytes = maxTextBytes;
        this.maxVisionBytes = maxVisionBytes;
    }

    @Transactional
    public AttachmentEntity store(
            UUID userId,
            String originalFilename,
            String contentType,
            long declaredSize,
            InputStream input
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (input == null) {
            throw new IllegalArgumentException("Attachment content is required");
        }
        String normalizedType = normalizeContentType(contentType);
        if (!ALLOWED_CONTENT_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("Unsupported attachment content type: " + normalizedType);
        }
        if (declaredSize > maxBytes) {
            throw new IllegalArgumentException("Attachment exceeds the maximum allowed size");
        }

        UUID attachmentId = UUID.randomUUID();
        String safeOriginalName = safeFilename(originalFilename);
        String storedFilename = attachmentId + extensionOf(safeOriginalName);
        Path target = safePath(storedFilename);
        Path temporary = safePath(storedFilename + ".tmp");

        try {
            Files.createDirectories(root);
            long size = copyWithLimit(input, temporary);
            if (size == 0) {
                throw new IllegalArgumentException("Attachment cannot be empty");
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);

            AttachmentEntity entity = new AttachmentEntity();
            entity.setId(attachmentId);
            entity.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found")));
            entity.setOriginalFilename(safeOriginalName);
            entity.setStoredFilename(storedFilename);
            entity.setContentType(normalizedType);
            entity.setSize(size);
            entity.setChecksumSha256(checksum(target));
            try {
                return repository.save(entity);
            } catch (RuntimeException databaseFailure) {
                Files.deleteIfExists(target);
                throw databaseFailure;
            }
        } catch (IOException exception) {
            deleteQuietly(temporary);
            deleteQuietly(target);
            throw new IllegalStateException("Could not store attachment", exception);
        } finally {
            deleteQuietly(temporary);
        }
    }

    @Transactional(readOnly = true)
    public AttachmentEntity getOwned(UUID userId, UUID attachmentId) {
        if (userId == null || attachmentId == null) {
            throw new IllegalArgumentException("User and attachment are required");
        }
        return repository.findByIdAndUserId(attachmentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
    }

    @Transactional(readOnly = true)
    public StoredAttachment openOwned(UUID userId, UUID attachmentId) {
        AttachmentEntity entity = getOwned(userId, attachmentId);
        Path path = safePath(entity.getStoredFilename());
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Attachment content is missing");
        }
        return new StoredAttachment(entity, path);
    }

    @Transactional(readOnly = true)
    public ResolvedAttachments resolveForModel(UUID userId, List<UUID> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return ResolvedAttachments.empty();
        }
        if (attachmentIds.size() > 8 || attachmentIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("At most 8 valid attachments can be sent");
        }

        List<AttachmentEntity> entities = repository.findAllByIdInAndUserId(attachmentIds, userId);
        if (entities.size() != attachmentIds.size()) {
            throw new IllegalArgumentException("One or more attachments were not found");
        }

        List<String> textParts = new ArrayList<>();
        List<ModelImage> images = new ArrayList<>();
        for (UUID id : attachmentIds) {
            AttachmentEntity entity = entities.stream()
                    .filter(candidate -> candidate.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
            Path path = safePath(entity.getStoredFilename());
            if (!Files.isRegularFile(path)) {
                throw new IllegalStateException("Attachment content is missing");
            }
            if (entity.getContentType().startsWith("text/")) {
                textParts.add("Arquivo " + entity.getOriginalFilename() + ":\n" + readText(path));
            } else if (isJson(entity.getContentType())) {
                textParts.add("Arquivo " + entity.getOriginalFilename() + ":\n" + readText(path));
            } else if (entity.getContentType().startsWith("image/")) {
                if (entity.getSize() > maxVisionBytes) {
                    throw new IllegalArgumentException("Image exceeds the vision model limit");
                }
                try {
                    images.add(new ModelImage(
                            entity.getContentType(),
                            Base64.getEncoder().encodeToString(Files.readAllBytes(path))
                    ));
                } catch (IOException exception) {
                    throw new IllegalStateException("Could not read image attachment", exception);
                }
                textParts.add("Imagem anexada: " + entity.getOriginalFilename() +
                        ". Analise-a somente se o modelo tiver capacidade visual.");
            } else if (entity.getContentType().startsWith("audio/")) {
                textParts.add("Áudio anexado: " + entity.getOriginalFilename() +
                        ". A transcrição deve ser solicitada pelo endpoint de áudio local.");
            } else {
                textParts.add("Anexo binário disponível: " + entity.getOriginalFilename() +
                        " (" + entity.getContentType() + ", " + entity.getSize() + " bytes).");
            }
        }
        return new ResolvedAttachments(String.join("\n\n", textParts), images);
    }

    public Path safePath(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()
                || storedFilename.contains("/") || storedFilename.contains("\\")) {
            throw new IllegalArgumentException("Invalid attachment path");
        }
        Path resolved = root.resolve(storedFilename).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Attachment path escapes the storage root");
        }
        return resolved;
    }

    private long copyWithLimit(InputStream input, Path temporary) throws IOException {
        long total = 0;
        byte[] buffer = new byte[8192];
        try (InputStream source = input;
             java.io.OutputStream output = Files.newOutputStream(temporary)) {
            int read;
            while ((read = source.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes) {
                    throw new IllegalArgumentException("Attachment exceeds the maximum allowed size");
                }
                output.write(buffer, 0, read);
            }
        }
        return total;
    }

    private String readText(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            int length = (int) Math.min(bytes.length, maxTextBytes);
            return new String(bytes, 0, length, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read text attachment", exception);
        }
    }

    private String checksum(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private String safeFilename(String originalFilename) {
        String value = originalFilename == null ? "attachment" : originalFilename.replace('\\', '/');
        String filename = Path.of(value).getFileName().toString().trim();
        if (filename.isBlank() || filename.equals(".") || filename.equals("..")) {
            return "attachment";
        }
        return filename.length() > 200 ? filename.substring(filename.length() - 200) : filename;
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 && dot < filename.length() - 1 ? filename.substring(dot).toLowerCase(Locale.ROOT) : "";
    }

    private boolean isJson(String contentType) {
        return "application/json".equals(contentType);
    }

    private void deleteQuietly(Path path) {
        try {
            if (path != null) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // best effort cleanup
        }
    }

    public record StoredAttachment(AttachmentEntity entity, Path path) {
    }

    public record ResolvedAttachments(String textContext, List<ModelImage> images) {
        public ResolvedAttachments {
            textContext = textContext == null ? "" : textContext;
            images = images == null ? List.of() : List.copyOf(images);
        }

        public static ResolvedAttachments empty() {
            return new ResolvedAttachments("", List.of());
        }
    }

}
