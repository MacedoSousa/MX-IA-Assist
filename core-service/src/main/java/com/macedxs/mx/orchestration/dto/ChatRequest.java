package com.macedxs.mx.orchestration.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "Prompt cannot be blank")
        String prompt
) {}
