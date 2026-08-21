package com.macedxs.mx.api.v1.media;

import com.macedxs.mx.api.v1.attachment.AttachmentDTO;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import com.macedxs.mx.media.service.VideoGenerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
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
public class VideoGenerationV1Controller {

    private final VideoGenerationService videoGenerationService;
    private final UserRepository userRepository;

    public VideoGenerationV1Controller(VideoGenerationService videoGenerationService, UserRepository userRepository) {
        this.videoGenerationService = videoGenerationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/videos")
    public ResponseEntity<AttachmentDTO> generate(@Valid @RequestBody VideoGenerationRequest request) {
        return ResponseEntity.ok(AttachmentDTO.from(videoGenerationService.generate(
                currentUser().getId(),
                request.prompt(),
                request.durationSeconds(),
                request.width(),
                request.height()
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

    public record VideoGenerationRequest(
            @NotBlank(message = "Video prompt cannot be blank")
            @Size(max = 4000, message = "Video prompt cannot exceed 4000 characters")
            String prompt,
            @Min(value = 1, message = "Video duration must be at least 1 second")
            @Max(value = 30, message = "Video duration cannot exceed 30 seconds")
            int durationSeconds,
            @Min(value = 320, message = "Video width must be at least 320")
            @Max(value = 1920, message = "Video width cannot exceed 1920")
            int width,
            @Min(value = 240, message = "Video height must be at least 240")
            @Max(value = 1080, message = "Video height cannot exceed 1080")
            int height
    ) {
        public VideoGenerationRequest {
            if (durationSeconds == 0) durationSeconds = 6;
            if (width == 0) width = 1280;
            if (height == 0) height = 720;
        }
    }
}
