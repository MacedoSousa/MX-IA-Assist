package com.macedxs.mx.api.v1.media;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import com.macedxs.mx.media.service.AudioTranscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class AudioTranscriptionV1Controller {

    private final AudioTranscriptionService transcriptionService;
    private final UserRepository userRepository;

    public AudioTranscriptionV1Controller(AudioTranscriptionService transcriptionService, UserRepository userRepository) {
        this.transcriptionService = transcriptionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/audio/{attachmentId}/transcription")
    public ResponseEntity<TranscriptionDTO> transcribe(@PathVariable UUID attachmentId) {
        AudioTranscriptionService.Transcription transcription = transcriptionService.transcribe(
                currentUser().getId(),
                attachmentId
        );
        return ResponseEntity.ok(new TranscriptionDTO(
                transcription.attachmentId(),
                transcription.text(),
                transcription.engine()
        ));
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public record TranscriptionDTO(UUID attachmentId, String text, String engine) {
    }
}
