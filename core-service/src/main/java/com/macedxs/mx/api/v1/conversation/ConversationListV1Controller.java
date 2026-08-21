package com.macedxs.mx.api.v1.conversation;

import com.macedxs.mx.conversation.dto.ConversationDTO;
import com.macedxs.mx.conversation.dto.ConversationPageDTO;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.service.ConversationService;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationListV1Controller {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ConversationService conversationService;
    private final UserRepository userRepository;

    public ConversationListV1Controller(
            ConversationService conversationService,
            UserRepository userRepository
    ) {
        this.conversationService = conversationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ConversationPageDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String query
    ) {
        if (page < 0) {
            return ResponseEntity.badRequest().build();
        }
        int boundedSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        UserEntity user = currentUser();
        Page<ConversationEntity> result = conversationService.findPageByUser(
                user.getId(),
                topic,
                query,
                PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "lastMessageAt"))
        );
        ConversationPageDTO response = new ConversationPageDTO(
                result.getContent().stream().map(ConversationDTO::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
        return ResponseEntity.ok(response);
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
