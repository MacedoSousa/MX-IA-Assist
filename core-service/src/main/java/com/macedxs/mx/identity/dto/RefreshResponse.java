package com.macedxs.mx.identity.dto;

import java.util.UUID;

public record RefreshResponse(
        UUID id,
        String email,
        String name,
        String role,
        String token,
        long expiresIn,
        UUID sessionId,
        String refreshToken,
        long refreshExpiresIn
) {
}
