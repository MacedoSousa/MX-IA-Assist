package com.macedxs.mx.conversation.dto;

import java.util.List;

public record ConversationPageDTO(
        List<ConversationDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
