package com.macedxs.mx.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password,

        String deviceName
) {
    public LoginRequest(String email, String password) {
        this(email, password, "unknown");
    }
}
