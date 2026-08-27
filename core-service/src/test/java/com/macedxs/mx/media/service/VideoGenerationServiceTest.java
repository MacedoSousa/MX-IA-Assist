package com.macedxs.mx.media.service;

import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.attachment.service.AttachmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VideoGenerationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void rejectsBlankPromptBeforeStartingFfmpeg() {
        AttachmentService attachments = mock(AttachmentService.class);
        VideoGenerationService service = service(attachments, mock(ImageGenerationService.class), true, "false");

        assertThatThrownBy(() -> service.generate(USER_ID, "  ", 6, 1280, 720))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prompt");
    }

    @Test
    void rejectsDurationAboveConfiguredLimit() {
        VideoGenerationService service = service(mock(AttachmentService.class), mock(ImageGenerationService.class), true, "false");

        assertThatThrownBy(() -> service.generate(USER_ID, "A short MX video", 31, 1280, 720))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duration");
    }

    @Test
    void rejectsDimensionsAboveConfiguredPixelLimit() {
        VideoGenerationService service = new VideoGenerationService(
                mock(AttachmentService.class), mock(ImageGenerationService.class), true, "false", 30, 640 * 480, 24, 10
        );

        assertThatThrownBy(() -> service.generate(USER_ID, "A short MX video", 6, 1280, 720))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dimensions");
    }

    @Test
    void refusesGenerationWhenFeatureIsDisabled() {
        VideoGenerationService service = service(mock(AttachmentService.class), mock(ImageGenerationService.class), false, "false");

        assertThatThrownBy(() -> service.generate(USER_ID, "A short MX video", 6, 1280, 720))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void surfacesFfmpegFailureWithoutPersistingAttachment() {
        AttachmentService attachments = mock(AttachmentService.class);
        ImageGenerationService images = mock(ImageGenerationService.class);
        when(images.render(eq(USER_ID), anyString(), eq(320), eq(240))).thenReturn(new byte[]{1, 2, 3});
        VideoGenerationService service = service(attachments, images, true, "false");

        assertThatThrownBy(() -> service.generate(USER_ID, "A short MX video", 1, 320, 240))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FFmpeg could not generate the video");

        org.mockito.Mockito.verifyNoInteractions(attachments);
    }

    @Test
    void storesGeneratedMp4AsOwnedAttachment(@TempDir Path tempDir) throws Exception {
        AttachmentService attachments = mock(AttachmentService.class);
        ImageGenerationService images = mock(ImageGenerationService.class);
        AttachmentEntity expected = new AttachmentEntity();
        Path executable = fakeFfmpeg(tempDir);
        when(images.render(eq(USER_ID), anyString(), eq(320), eq(240))).thenReturn(new byte[]{1, 2, 3});
        when(attachments.store(
                eq(USER_ID), anyString(), eq("video/mp4"), anyLong(), any(InputStream.class)
        )).thenReturn(expected);

        VideoGenerationService service = service(attachments, images, true, executable.toString());
        AttachmentEntity actual = service.generate(USER_ID, "A short MX video", 1, 320, 240);

        assertThat(actual).isSameAs(expected);
        verify(attachments).store(
                eq(USER_ID), anyString(), eq("video/mp4"), anyLong(), any(InputStream.class)
        );
    }

    private VideoGenerationService service(AttachmentService attachments, ImageGenerationService images, boolean enabled, String ffmpeg) {
        return new VideoGenerationService(attachments, images, enabled, ffmpeg, 30, 2_073_600, 24, 10);
    }

    private Path fakeFfmpeg(Path tempDir) throws Exception {
        if (System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win")) {
            Path executable = tempDir.resolve("fake-ffmpeg.cmd");
            Files.writeString(executable, "@echo off\r\nset output=\r\n:args\r\nif \"%~1\"==\"\" goto write\r\nset output=%~1\r\nshift\r\ngoto args\r\n:write\r\necho fake-mp4>\"%output%\"\r\n");
            return executable;
        }
        Path executable = tempDir.resolve("fake-ffmpeg.sh");
        Files.writeString(executable, "#!/bin/sh\noutput=\"\"\nfor arg in \"$@\"; do output=\"$arg\"; done\nprintf 'fake-mp4' > \"$output\"\n");
        assertThat(executable.toFile().setExecutable(true)).isTrue();
        return executable;
    }
}
