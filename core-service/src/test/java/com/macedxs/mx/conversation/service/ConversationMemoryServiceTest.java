package com.macedxs.mx.conversation.service;

import com.macedxs.mx.conversation.entity.ConversationMessageEntity;
import com.macedxs.mx.conversation.repository.ConversationMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationMemoryServiceTest {

    private final ConversationMessageRepository repository = mock(ConversationMessageRepository.class);
    private final ConversationMemoryService service = new ConversationMemoryService(repository);

    @Test
    void shouldReturnPagedConversationHistory() {
        UUID conversationId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(1, 20);
        Page<ConversationMessageEntity> expected = new PageImpl<>(List.of(), pageable, 20);
        when(repository.findByConversationIdOrderByCreatedAtAscIdAsc(conversationId, pageable))
                .thenReturn(expected);

        Page<ConversationMessageEntity> result = service.getHistory(conversationId, pageable);

        assertThat(result).isSameAs(expected);
        verify(repository).findByConversationIdOrderByCreatedAtAscIdAsc(conversationId, pageable);
    }

    @Test
    void shouldRejectMissingConversationOrPageable() {
        PageRequest pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> service.getHistory(null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conversation is required");
        assertThatThrownBy(() -> service.getHistory(UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pageable is required");
    }
}
