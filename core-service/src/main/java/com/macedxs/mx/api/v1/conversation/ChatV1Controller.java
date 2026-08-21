package com.macedxs.mx.api.v1.conversation;

import com.macedxs.mx.conversation.application.SendMessageCommand;
import com.macedxs.mx.conversation.application.SendMessageResult;
import com.macedxs.mx.conversation.application.SendMessageUseCase;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/conversations")
public class ChatV1Controller {

    private final SendMessageUseCase sendMessageUseCase;
    private final UserRepository userRepository;

    public ChatV1Controller(SendMessageUseCase sendMessageUseCase, UserRepository userRepository) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.userRepository = userRepository;
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatV1Response> sendMessage(@Valid @RequestBody ChatV1Request request) {
        UserEntity user = currentUser();

        SendMessageResult result = sendMessageUseCase.execute(
                new SendMessageCommand(
                        user.getId(),
                        request.conversationId(),
                        request.prompt(),
                        request.idempotencyKey(),
                        request.attachmentIds()
                )
        );

        return ResponseEntity.ok(ChatV1ResponseMapper.completed(result));
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
