package com.macedxs.mx.conversation.service;

import com.macedxs.mx.ai.service.OllamaService;
import com.macedxs.mx.conversation.entity.ConversationEntity;
import com.macedxs.mx.conversation.repository.ConversationRepository;
import com.macedxs.mx.identity.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationServiceCrudTest {

    private final ConversationRepository repository = mock(ConversationRepository.class);
    private final OllamaService ollamaService = mock(OllamaService.class);
    private final ConversationService service = new ConversationService(repository, ollamaService);
    private final UUID userId = UUID.randomUUID();
    private final UUID conversationId = UUID.randomUUID();
    private UserEntity user;
    private ConversationEntity conversation;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", userId);
        conversation = new ConversationEntity();
        ReflectionTestUtils.setField(conversation, "id", conversationId);
        conversation.setUser(user);
        conversation.setTitle("Conversa original");
    }

    @Test
    void shouldRenameOwnedConversation() {
        when(repository.findActiveByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(conversation));
        when(repository.save(conversation)).thenReturn(conversation);

        ConversationEntity result = service.renameConversation(userId, conversationId, "Projeto MX");

        assertThat(result.getTitle()).isEqualTo("Projeto MX");
        verify(repository).save(conversation);
    }

    @Test
    void shouldArchiveOwnedConversationWithoutDeletingIt() {
        when(repository.findActiveByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(conversation));
        when(repository.save(conversation)).thenReturn(conversation);

        ConversationEntity result = service.archiveConversation(userId, conversationId);

        assertThat(result.getArchivedAt()).isNotNull();
        assertThat(result.getDeletedAt()).isNull();
        verify(repository).save(conversation);
    }

    @Test
    void shouldSoftDeleteOwnedConversationAndKeepMessagesAvailableForRestore() {
        when(repository.findActiveByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(conversation));
        when(repository.save(conversation)).thenReturn(conversation);

        ConversationEntity result = service.softDeleteConversation(userId, conversationId);

        assertThat(result.getDeletedAt()).isNotNull();
        verify(repository).save(conversation);
    }

    @Test
    void shouldRestoreDeletedConversationOwnedByUser() {
        conversation.setDeletedAt(LocalDateTime.now());
        conversation.setArchivedAt(LocalDateTime.now());
        when(repository.findOwnedById(conversationId, userId)).thenReturn(Optional.of(conversation));
        when(repository.save(conversation)).thenReturn(conversation);

        ConversationEntity result = service.restoreConversation(userId, conversationId);

        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getArchivedAt()).isNull();
        verify(repository).save(conversation);
    }

    @Test
    void shouldRejectConversationOwnedByAnotherUser() {
        when(repository.findActiveByIdAndUserId(conversationId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.renameConversation(userId, conversationId, "Não autorizado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conversation not found");
    }
}
