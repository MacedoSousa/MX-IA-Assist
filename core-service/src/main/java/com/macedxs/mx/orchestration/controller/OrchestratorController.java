package com.macedxs.mx.orchestration.controller;

import com.macedxs.mx.conversation.application.SendMessageCommand;
import com.macedxs.mx.conversation.application.SendMessageResult;
import com.macedxs.mx.conversation.application.SendMessageUseCase;
import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.repository.UserRepository;
import com.macedxs.mx.orchestration.dto.ChatRequest;
import com.macedxs.mx.orchestration.dto.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrchestratorController {

    private final SendMessageUseCase sendMessageUseCase;
    private final UserRepository userRepository;

    public OrchestratorController(SendMessageUseCase sendMessageUseCase, UserRepository userRepository) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.userRepository = userRepository;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SendMessageResult response = sendMessageUseCase.execute(
                new SendMessageCommand(user.getId(), null, request.prompt())
        );

        return ResponseEntity.ok(new ChatResponse(
                response.conversationId(),
                null,
                null,
                response.answer()
        ));
    }
}
