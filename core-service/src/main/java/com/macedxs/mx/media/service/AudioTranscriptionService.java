package com.macedxs.mx.media.service;

import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.attachment.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AudioTranscriptionService {

    private final AttachmentService attachmentService;
    private final boolean enabled;
    private final String command;
    private final String model;
    private final int timeoutSeconds;
    private final int maxChars;

    @Autowired
    public AudioTranscriptionService(
            AttachmentService attachmentService,
            @Value("${mx.media.audio-transcription.enabled:false}") boolean enabled,
            @Value("${mx.media.audio-transcription.command:whisper}") String command,
            @Value("${mx.media.audio-transcription.model:base}") String model,
            @Value("${mx.media.audio-transcription.timeout-seconds:300}") int timeoutSeconds,
            @Value("${mx.media.audio-transcription.max-chars:50000}") int maxChars
    ) {
        this(attachmentService, enabled, command, model, timeoutSeconds, maxChars, 1);
    }

    AudioTranscriptionService(
            AttachmentService attachmentService,
            boolean enabled,
            String command,
            String model,
            int timeoutSeconds,
            int maxChars,
            int ignored
    ) {
        if (attachmentService == null || command == null || command.isBlank() || model == null || model.isBlank()) {
            throw new IllegalArgumentException("Audio transcription configuration is invalid");
        }
        if (timeoutSeconds <= 0 || maxChars <= 0) {
            throw new IllegalArgumentException("Audio transcription limits must be positive");
        }
        this.attachmentService = attachmentService;
        this.enabled = enabled;
        this.command = command.trim();
        this.model = model.trim();
        this.timeoutSeconds = timeoutSeconds;
        this.maxChars = maxChars;
    }

    public Transcription transcribe(UUID userId, UUID attachmentId) {
        if (!enabled) {
            throw new IllegalStateException("Local audio transcription is disabled; install/configure Whisper first");
        }
        AttachmentService.StoredAttachment stored = attachmentService.openOwned(userId, attachmentId);
        AttachmentEntity entity = stored.entity();
        if (entity.getContentType() == null || !entity.getContentType().startsWith("audio/")) {
            throw new IllegalArgumentException("Attachment is not an audio file");
        }

        Path outputDirectory = null;
        try {
            outputDirectory = Files.createTempDirectory("mx-whisper-");
            Process process = new ProcessBuilder(List.of(
                    command,
                    stored.path().toString(),
                    "--model", model,
                    "--device", "cpu",
                    "--fp16", "False",
                    "--output_format", "txt",
                    "--output_dir", outputDirectory.toString()
            )).redirectErrorStream(true).start();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Audio transcription timed out");
            }
            if (process.exitValue() != 0) {
                String diagnostics = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new IllegalStateException("Whisper failed: " + truncate(diagnostics));
            }
            Path transcript = Files.walk(outputDirectory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".txt"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Whisper returned no transcript"));
            String text = Files.readString(transcript, StandardCharsets.UTF_8).trim();
            if (text.isBlank()) {
                throw new IllegalStateException("Whisper returned an empty transcript");
            }
            return new Transcription(attachmentId, truncate(text), "whisper/" + model);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Audio transcription was interrupted", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not execute local Whisper", exception);
        } finally {
            deleteTree(outputDirectory);
        }
    }

    private String truncate(String value) {
        if (value == null) return "";
        return value.length() <= maxChars ? value : value.substring(0, maxChars);
    }

    private void deleteTree(Path directory) {
        if (directory == null) return;
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best effort cleanup
                }
            });
        } catch (IOException ignored) {
            // best effort cleanup
        }
    }

    public record Transcription(UUID attachmentId, String text, String engine) {
    }
}
