package com.macedxs.mx.identity.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String name,
        String token,
        String role,
        long expiresIn
) {}
