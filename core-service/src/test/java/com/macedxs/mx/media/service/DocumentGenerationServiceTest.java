package com.macedxs.mx.media.service;

import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.attachment.service.AttachmentService;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentGenerationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void rejectsBlankPromptBeforeInvokingTheCore() {
        DocumentGenerationService service = service(mock(AttachmentService.class), mock(ModelGateway.class), true);

        assertThatThrownBy(() -> service.generate(USER_ID, " ", null, DocumentGenerationService.DocumentFormat.MARKDOWN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prompt");
    }

    @Test
    void refusesGenerationWhenFeatureIsDisabled() {
        DocumentGenerationService service = service(mock(AttachmentService.class), mock(ModelGateway.class), false);

        assertThatThrownBy(() -> service.generate(USER_ID, "Criar uma ata", null, DocumentGenerationService.DocumentFormat.MARKDOWN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void generatesMarkdownThroughTheMxCoreAndStoresItAsAnOwnedAttachment() {
        AttachmentService attachments = mock(AttachmentService.class);
        ModelGateway core = mock(ModelGateway.class);
        AttachmentEntity expected = new AttachmentEntity();
        when(core.complete(any(ModelGateway.ModelRequest.class))).thenReturn(
                new ModelGateway.ModelResponse("# Relatório\n\nConteúdo validado.", "deepseek-r1:14b", 12)
        );
        when(attachments.store(eq(USER_ID), anyString(), eq("text/markdown"), anyLong(), any(InputStream.class)))
                .thenReturn(expected);

        AttachmentEntity result = service(attachments, core, true).generate(
                USER_ID, "Criar um relatório", "Relatório de teste", DocumentGenerationService.DocumentFormat.MARKDOWN
        );

        assertThat(result).isSameAs(expected);
        verify(core).complete(any(ModelGateway.ModelRequest.class));
        verify(attachments).store(eq(USER_ID), eq("relatorio-de-teste.md"), eq("text/markdown"), anyLong(), any(InputStream.class));
    }

    @Test
    void rendersDocxAndPdfWithPortableFileSignatures() throws Exception {
        AttachmentService attachments = mock(AttachmentService.class);
        ModelGateway core = mock(ModelGateway.class);
        when(core.complete(any(ModelGateway.ModelRequest.class))).thenReturn(
                new ModelGateway.ModelResponse("# Relatório\n\nConteúdo em português.", "deepseek-r1:14b", 12)
        );
        when(attachments.store(eq(USER_ID), anyString(), anyString(), anyLong(), any(InputStream.class)))
                .thenReturn(new AttachmentEntity());
        DocumentGenerationService service = service(attachments, core, true);

        service.generate(USER_ID, "Criar relatório", "Teste", DocumentGenerationService.DocumentFormat.DOCX);
        var docxContent = forClass(InputStream.class);
        verify(attachments).store(eq(USER_ID), eq("teste.docx"), eq("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), anyLong(), docxContent.capture());
        assertThat(docxContent.getValue().readAllBytes()).startsWith("PK".getBytes(StandardCharsets.US_ASCII));

        service.generate(USER_ID, "Criar relatório", "Teste", DocumentGenerationService.DocumentFormat.PDF);
        var pdfContent = forClass(InputStream.class);
        verify(attachments).store(eq(USER_ID), eq("teste.pdf"), eq("application/pdf"), anyLong(), pdfContent.capture());
        assertThat(pdfContent.getValue().readAllBytes()).startsWith("%PDF".getBytes(StandardCharsets.US_ASCII));
    }

    private DocumentGenerationService service(AttachmentService attachments, ModelGateway core, boolean enabled) {
        return new DocumentGenerationService(attachments, core, enabled);
    }
}
