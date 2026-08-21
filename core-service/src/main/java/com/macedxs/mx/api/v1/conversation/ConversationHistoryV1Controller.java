package com.macedxs.mx.api.v1.conversation;

import com.macedxs.mx.conversation.dto.MessageDTO;
import com.macedxs.mx.conversation.dto.MessagePageDTO;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.entity.ConversationMessageEntity;
import com.macedxs.mx.conversation.service.ConversationMemoryService;
import com.macedxs.mx.conversation.service.ConversationService;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationHistoryV1Controller {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ConversationService conversationService;
    private final ConversationMemoryService memoryService;
    private final UserRepository userRepository;

    public ConversationHistoryV1Controller(
            ConversationService conversationService,
            ConversationMemoryService memoryService,
            UserRepository userRepository
    ) {
        this.conversationService = conversationService;
        this.memoryService = memoryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<MessagePageDTO> getConversationHistory(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0) {
            return ResponseEntity.badRequest().build();
        }
        int boundedSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        UserEntity user = currentUser();
        ConversationEntity conversation = conversationService.findActiveById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (conversation.getUser() == null
                || conversation.getUser().getId() == null
                || !conversation.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        Page<ConversationMessageEntity> result = memoryService.getHistory(
                conversationId,
                PageRequest.of(page, boundedSize)
        );
        MessagePageDTO response = new MessagePageDTO(
                result.getContent().stream().map(this::toDto).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
        return ResponseEntity.ok(response);
    }

    private MessageDTO toDto(ConversationMessageEntity message) {
        return new MessageDTO(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt()
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
}
