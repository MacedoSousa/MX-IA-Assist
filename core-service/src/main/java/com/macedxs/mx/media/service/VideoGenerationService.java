package com.macedxs.mx.media.service;

import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.attachment.service.AttachmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class VideoGenerationService {

    private final AttachmentService attachmentService;
    private final ImageGenerationService imageGenerationService;
    private final boolean enabled;
    private final String ffmpegCommand;
    private final int maxDurationSeconds;
    private final int maxPixels;
    private final int framesPerSecond;
    private final int timeoutSeconds;

    public VideoGenerationService(
            AttachmentService attachmentService,
            ImageGenerationService imageGenerationService,
            @Value("${mx.media.video-generation.enabled:false}") boolean enabled,
            @Value("${mx.media.video-generation.ffmpeg-command:ffmpeg}") String ffmpegCommand,
            @Value("${mx.media.video-generation.max-duration-seconds:30}") int maxDurationSeconds,
            @Value("${mx.media.video-generation.max-pixels:2073600}") int maxPixels,
            @Value("${mx.media.video-generation.frames-per-second:24}") int framesPerSecond,
            @Value("${mx.media.video-generation.timeout-seconds:180}") int timeoutSeconds
    ) {
        if (attachmentService == null || imageGenerationService == null) {
            throw new IllegalArgumentException("Video generation dependencies are required");
        }
        if (maxDurationSeconds <= 0 || maxPixels <= 0 || framesPerSecond <= 0 || timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Video generation limits must be positive");
        }
        this.attachmentService = attachmentService;
        this.imageGenerationService = imageGenerationService;
        this.enabled = enabled;
        this.ffmpegCommand = ffmpegCommand == null || ffmpegCommand.isBlank() ? "ffmpeg" : ffmpegCommand.trim();
        this.maxDurationSeconds = maxDurationSeconds;
        this.maxPixels = maxPixels;
        this.framesPerSecond = framesPerSecond;
        this.timeoutSeconds = timeoutSeconds;
    }

    public AttachmentEntity generate(UUID userId, String prompt, int durationSeconds, int width, int height) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (prompt == null || prompt.isBlank() || prompt.length() > 4000) {
            throw new IllegalArgumentException("Video prompt must contain 1 to 4000 characters");
        }
        if (durationSeconds < 1 || durationSeconds > maxDurationSeconds) {
            throw new IllegalArgumentException("Video duration exceeds the configured limit");
        }
        if (width < 320 || height < 240 || ((long) width * height) > maxPixels) {
            throw new IllegalArgumentException("Video dimensions exceed the configured limit");
        }
        if (!enabled) {
            throw new IllegalStateException("Local video generation is disabled; enable FFmpeg generation in the environment");
        }

        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("mx-video-");
            Path keyframeFile = workDir.resolve("keyframe.png");
            Path outputFile = workDir.resolve("generated.mp4");
            Files.write(keyframeFile, imageGenerationService.render(userId, prompt.trim(), width, height));

            String filter = "scale=" + width + ":" + height + ":force_original_aspect_ratio=increase,"
                    + "crop=" + width + ":" + height + ","
                    + "zoompan=z='min(zoom+0.0008,1.10)':d=1:s=" + width + "x" + height + ":fps=" + framesPerSecond;

            Process process = new ProcessBuilder(
                    ffmpegCommand,
                    "-y",
                    "-loop", "1",
                    "-i", keyframeFile.toString(),
                    "-t", Integer.toString(durationSeconds),
                    "-vf", filter,
                    "-an",
                    "-c:v", "libx264",
                    "-preset", "veryfast",
                    "-pix_fmt", "yuv420p",
                    "-movflags", "+faststart",
                    outputFile.toString()
            ).redirectErrorStream(true).start();

            ByteArrayOutputStream processOutput = new ByteArrayOutputStream();
            Thread outputReader = Thread.startVirtualThread(() -> {
                try {
                    process.getInputStream().transferTo(processOutput);
                } catch (Exception ignored) {
                    // The exit code remains the source of truth for the bounded process.
                }
            });
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Video generation timed out");
            }
            outputReader.join(Duration.ofSeconds(2).toMillis());
            if (process.exitValue() != 0 || !Files.isRegularFile(outputFile)) {
                String details = processOutput.toString(StandardCharsets.UTF_8);
                if (details.length() > 500) details = details.substring(details.length() - 500);
                throw new IllegalStateException("FFmpeg could not generate the video: " + details);
            }

            byte[] mp4 = Files.readAllBytes(outputFile);
            return attachmentService.store(
                    userId,
                    "mx-generated-" + UUID.randomUUID() + ".mp4",
                    "video/mp4",
                    mp4.length,
                    new ByteArrayInputStream(mp4)
            );
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate local video", exception);
        } finally {
            deleteWorkDir(workDir);
        }
    }

    private void deleteWorkDir(Path workDir) {
        if (workDir == null) return;
        try (var paths = Files.walk(workDir)) {
            paths.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                    // Temporary media files are best-effort cleanup only.
                }
            });
        } catch (Exception ignored) {
            // Best-effort cleanup.
        }
    }
}
