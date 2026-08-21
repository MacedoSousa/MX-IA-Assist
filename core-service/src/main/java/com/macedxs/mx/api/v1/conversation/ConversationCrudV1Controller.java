package com.macedxs.mx.api.v1.conversation;

import com.macedxs.mx.conversation.dto.ConversationCreateRequest;
import com.macedxs.mx.conversation.dto.ConversationDTO;
import com.macedxs.mx.conversation.dto.ConversationRenameRequest;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.service.ConversationService;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationCrudV1Controller {

    private final ConversationService conversationService;
    private final UserRepository userRepository;

    public ConversationCrudV1Controller(ConversationService conversationService, UserRepository userRepository) {
        this.conversationService = conversationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ConversationDTO> create(@RequestBody(required = false) ConversationCreateRequest request) {
        UserEntity user = currentUser();
        ConversationEntity created = conversationService.createConversation(user, request == null ? null : request.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversationDTO.from(created));
    }

    @GetMapping("/trash")
    public ResponseEntity<List<ConversationDTO>> trash() {
        UserEntity user = currentUser();
        return ResponseEntity.ok(conversationService.findDeletedByUser(user.getId()).stream().map(ConversationDTO::from).toList());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ConversationDTO> rename(
            @PathVariable UUID id,
            @RequestBody ConversationRenameRequest request
    ) {
        UserEntity user = currentUser();
        return ResponseEntity.ok(ConversationDTO.from(
                conversationService.renameConversation(user.getId(), id, request == null ? null : request.title())
        ));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ConversationDTO> archive(@PathVariable UUID id) {
        UserEntity user = currentUser();
        return ResponseEntity.ok(ConversationDTO.from(conversationService.archiveConversation(user.getId(), id)));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ConversationDTO> restore(@PathVariable UUID id) {
        UserEntity user = currentUser();
        return ResponseEntity.ok(ConversationDTO.from(conversationService.restoreConversation(user.getId(), id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UserEntity user = currentUser();
        conversationService.softDeleteConversation(user.getId(), id);
        return ResponseEntity.noContent().build();
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
