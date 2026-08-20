package com.macedxs.mx.conversation.controller;

import com.macedxs.mx.conversation.dto.ConversationDTO;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.service.ConversationService;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final UserRepository userRepository;

    public ConversationController(ConversationService conversationService, UserRepository userRepository) {
        this.conversationService = conversationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> findMyConversations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ConversationDTO> conversations = conversationService.findByUser(user.getId())
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ConversationEntity conversation = conversationService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(entityToDto(conversation));
    }

    private ConversationDTO entityToDto(ConversationEntity entity) {
        return new ConversationDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getCreatedAt()
        );
    }
}
