package com.macedxs.mx.identity.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String name,
        String email,
        String role
) {}
