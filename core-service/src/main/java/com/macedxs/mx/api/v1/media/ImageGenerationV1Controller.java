package com.macedxs.mx.api.v1.media;

import com.macedxs.mx.api.v1.attachment.AttachmentDTO;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import com.macedxs.mx.media.service.ImageGenerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class ImageGenerationV1Controller {

    private final ImageGenerationService imageGenerationService;
    private final UserRepository userRepository;

    public ImageGenerationV1Controller(ImageGenerationService imageGenerationService, UserRepository userRepository) {
        this.imageGenerationService = imageGenerationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/images")
    public ResponseEntity<AttachmentDTO> generate(@Valid @RequestBody ImageGenerationRequest request) {
        return ResponseEntity.ok(AttachmentDTO.from(imageGenerationService.generate(
                currentUser().getId(),
                request.prompt(),
                request.width(),
                request.height(),
                new ImageGenerationService.GenerationOptions(
                        request.negativePrompt(),
                        request.seed(),
                        request.cfgScale()
                )
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

    public record ImageGenerationRequest(
            @NotBlank(message = "Image prompt cannot be blank")
            @Size(max = 4000, message = "Image prompt cannot exceed 4000 characters")
            String prompt,
            @Min(value = 64, message = "Image width must be at least 64")
            @Max(value = 2048, message = "Image width cannot exceed 2048")
            int width,
            @Min(value = 64, message = "Image height must be at least 64")
            @Max(value = 2048, message = "Image height cannot exceed 2048")
            int height,
            @Size(max = 2000, message = "Image negative prompt cannot exceed 2000 characters")
            String negativePrompt,
            @Min(value = -1, message = "Image seed must be -1 or a non-negative number")
            Long seed,
            @DecimalMin(value = "1.0", message = "Image guidance scale must be at least 1")
            @DecimalMax(value = "30.0", message = "Image guidance scale cannot exceed 30")
            Double cfgScale
    ) {
        public ImageGenerationRequest {
            if (width == 0) width = 768;
            if (height == 0) height = 768;
        }
    }
}
