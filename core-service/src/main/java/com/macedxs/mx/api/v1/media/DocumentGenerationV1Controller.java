package com.macedxs.mx.api.v1.media;

import com.macedxs.mx.api.v1.attachment.AttachmentDTO;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import com.macedxs.mx.media.service.DocumentGenerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media")
public class DocumentGenerationV1Controller {

    private final DocumentGenerationService documentGenerationService;
    private final UserRepository userRepository;

    public DocumentGenerationV1Controller(DocumentGenerationService documentGenerationService, UserRepository userRepository) {
        this.documentGenerationService = documentGenerationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/documents")
    public ResponseEntity<AttachmentDTO> generate(@Valid @RequestBody DocumentGenerationRequest request) {
        return ResponseEntity.ok(AttachmentDTO.from(documentGenerationService.generate(
                currentUser().getId(), request.prompt(), request.title(), request.format()
        )));
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public record DocumentGenerationRequest(
            @NotBlank(message = "Document prompt cannot be blank")
            @Size(max = 4000, message = "Document prompt cannot exceed 4000 characters")
            String prompt,
            @Size(max = 120, message = "Document title cannot exceed 120 characters")
            String title,
            DocumentGenerationService.DocumentFormat format
    ) {
        public DocumentGenerationRequest {
            if (format == null) format = DocumentGenerationService.DocumentFormat.MARKDOWN;
        }
    }
}
