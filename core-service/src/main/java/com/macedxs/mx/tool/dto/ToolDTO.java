package com.macedxs.mx.tool.dto;

import java.util.UUID;

public record ToolDTO(
        UUID id,
        String name,
        String description,
        Boolean enabled
) {}
