package com.macedxs.mx.api.v1.identity;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.entity.UserPreferenceEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import com.macedxs.mx.identity.service.UserPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/preferences")
public class UserPreferenceV1Controller {

    private final UserPreferenceService preferenceService;
    private final UserRepository userRepository;

    public UserPreferenceV1Controller(
            UserPreferenceService preferenceService,
            UserRepository userRepository
    ) {
        this.preferenceService = preferenceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<UserPreferenceDTO> getPreferences() {
        UserEntity user = currentUser();
        UserPreferenceEntity preference = preferenceService.getOrCreate(user);
        return ResponseEntity.ok(toDto(user.getId(), preference));
    }

    @PutMapping
    public ResponseEntity<UserPreferenceDTO> updatePreferences(
            @RequestBody UpdatePreferenceRequest request
    ) {
        UserEntity user = currentUser();
        UserPreferenceEntity preference = preferenceService.update(
                user.getId(),
                new UserPreferenceService.UpdateRequest(
                        request.learningStyle(),
                        request.knowledgeLevel(),
                        request.topicsOfInterest()
                )
        );
        return ResponseEntity.ok(toDto(user.getId(), preference));
    }

    private UserPreferenceDTO toDto(UUID userId, UserPreferenceEntity preference) {
        return new UserPreferenceDTO(
                userId,
                preference.getLearningStyle(),
                preference.getKnowledgeLevel(),
                preference.getTopicsOfInterest() == null
                        ? Set.of()
                        : Set.copyOf(preference.getTopicsOfInterest()),
                preference.getLastActiveAt()
        );
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public record UpdatePreferenceRequest(
            String learningStyle,
            String knowledgeLevel,
            java.util.Set<String> topicsOfInterest
    ) {
    }

}
