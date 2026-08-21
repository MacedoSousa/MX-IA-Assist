package com.macedxs.mx.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OllamaWarmupServiceTest {

    @Mock
    private OllamaService ollamaService;

    @Test
    void shouldWarmupWhenEnabled() {
        OllamaWarmupService service = new OllamaWarmupService(ollamaService, true);

        service.warmupOnApplicationReady();

        verify(ollamaService, timeout(1_000)).generateText("oi");
    }

    @Test
    void shouldNotWarmupWhenDisabled() {
        OllamaWarmupService service = new OllamaWarmupService(ollamaService, false);

        service.warmupOnApplicationReady();

        verifyNoInteractions(ollamaService);
    }
}
