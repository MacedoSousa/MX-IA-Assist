package com.macedxs.mx.identity.dto;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String email,
        String name,
        String role,
        String token,
        long expiresIn,
        UUID sessionId,
        String refreshToken
) {
    public LoginResponse(UUID id, String email, String name, String role, String token, long expiresIn) {
        this(id, email, name, role, token, expiresIn, null, null);
    }

    public LoginResponse(UUID id, String email, String name, String role, String token, long expiresIn, UUID sessionId) {
        this(id, email, name, role, token, expiresIn, sessionId, null);
    }
}
