package com.macedxs.mx.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token cannot be blank")
        String refreshToken
) {
}
