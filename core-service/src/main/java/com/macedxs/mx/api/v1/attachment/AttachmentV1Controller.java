package com.macedxs.mx.api.v1.attachment;

import com.macedxs.mx.attachment.service.AttachmentService;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentV1Controller {

    private final AttachmentService attachmentService;
    private final UserRepository userRepository;

    public AttachmentV1Controller(AttachmentService attachmentService, UserRepository userRepository) {
        this.attachmentService = attachmentService;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDTO> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file is required");
        }
        UserEntity user = currentUser();
        var stored = attachmentService.store(
                user.getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream()
        );
        return ResponseEntity.ok(AttachmentDTO.from(stored));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID attachmentId) throws IOException {
        var stored = attachmentService.openOwned(currentUser().getId(), attachmentId);
        var resource = new InputStreamResource(java.nio.file.Files.newInputStream(stored.path()));
        String filename = stored.entity().getOriginalFilename().replaceAll("[\\r\\n\\\"]", "_");
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(stored.entity().getContentType());
        } catch (IllegalArgumentException ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(stored.entity().getSize());
        headers.set("X-Content-Type-Options", "nosniff");
        headers.setCacheControl("no-store");
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
