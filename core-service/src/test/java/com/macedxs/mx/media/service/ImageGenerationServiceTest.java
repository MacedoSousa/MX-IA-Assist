package com.macedxs.mx.media.service;

import com.macedxs.mx.attachment.service.AttachmentService;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ImageGenerationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void rejectsNegativePromptOutsideTheConfiguredBoundBeforeCallingForge() {
        ImageGenerationService service = service();

        assertThatThrownBy(() -> service.render(
                USER_ID,
                "Um tabuleiro de jogo estratégico",
                768,
                768,
                new ImageGenerationService.GenerationOptions("x".repeat(2_001), null, null)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative prompt");
    }

    @Test
    void rejectsAnInvalidSeedBeforeCallingForge() {
        ImageGenerationService service = service();

        assertThatThrownBy(() -> service.render(
                USER_ID,
                "Uma interface editorial",
                768,
                768,
                new ImageGenerationService.GenerationOptions(null, -2L, 7.0)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("seed");
    }

    private ImageGenerationService service() {
        return new ImageGenerationService(
                mock(AttachmentService.class),
                new com.fasterxml.jackson.databind.ObjectMapper(),
                mock(HttpClient.class),
                true,
                "http://127.0.0.1:7860",
                1_048_576,
                24
        );
    }
}
