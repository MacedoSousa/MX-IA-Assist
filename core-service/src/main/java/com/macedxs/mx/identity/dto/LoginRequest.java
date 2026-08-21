package com.macedxs.mx.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "Email cannot be blank")
        @Pattern(
                regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
                message = "Email should contain a local part and domain"
        )
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password,

        String deviceName
) {
    public LoginRequest(String email, String password) {
        this(email, password, "unknown");
    }
}
