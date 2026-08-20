package com.macedxs.mx.conversation.controller;

import com.macedxs.mx.conversation.dto.MessageDTO;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.entity.ConversationMessageEntity;
import com.macedxs.mx.conversation.service.ConversationMemoryService;
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
public class ConversationHistoryController {

    private final ConversationService conversationService;
    private final ConversationMemoryService memoryService;
    private final UserRepository userRepository;

    public ConversationHistoryController(
            ConversationService conversationService,
            ConversationMemoryService memoryService,
            UserRepository userRepository
    ) {
        this.conversationService = conversationService;
        this.memoryService = memoryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getConversationHistory(@PathVariable UUID conversationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ConversationEntity conversation = conversationService.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<MessageDTO> messages = memoryService.getHistory(conversationId)
                .stream()
                .map(msg -> new MessageDTO(
                        msg.getId(),
                        msg.getRole().name(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageDTO> addMessage(
            @PathVariable UUID conversationId,
            @RequestBody AddMessageRequest request
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ConversationEntity conversation = conversationService.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        ConversationMessageEntity.MessageRole role = ConversationMessageEntity.MessageRole.valueOf(request.role().toUpperCase());
        ConversationMessageEntity message = memoryService.addMessage(conversation, role, request.content());

        MessageDTO dto = new MessageDTO(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt()
        );

        return ResponseEntity.ok(dto);
    }

    public record AddMessageRequest(String role, String content) {}
}
