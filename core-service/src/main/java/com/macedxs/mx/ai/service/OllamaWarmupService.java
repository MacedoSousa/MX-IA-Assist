package com.macedxs.mx.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class OllamaWarmupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaWarmupService.class);
    private final OllamaService ollamaService;
    private final boolean enabled;

    public OllamaWarmupService(
            OllamaService ollamaService,
            @Value("${mx.ollama.warmup-enabled:false}") boolean enabled
    ) {
        this.ollamaService = ollamaService;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmupOnApplicationReady() {
        if (!enabled) {
            LOGGER.debug("Ollama warmup disabled");
            return;
        }
        CompletableFuture.runAsync(this::warmup)
                .exceptionally(error -> {
                    LOGGER.warn("Ollama warmup failed; the first request may incur model load latency", error);
                    return null;
                });
    }

    void warmup() {
        long startedAt = System.nanoTime();
        ollamaService.generateText("oi");
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        LOGGER.info("Ollama model warmed up in {} ms", elapsedMs);
    }
}
