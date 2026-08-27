package com.macedxs.mx.agent.skill.media;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.application.SkillResult;
import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.media.service.DocumentGenerationService;
import com.macedxs.mx.media.service.ImageGenerationService;
import com.macedxs.mx.media.service.VideoGenerationService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MediaSkillTest {

    @Test
    void shouldGenerateImageForExplicitConversationalRequest() {
        ImageGenerationService imageService = mock(ImageGenerationService.class);
        VideoGenerationService videoService = mock(VideoGenerationService.class);
        DocumentGenerationService documentService = mock(DocumentGenerationService.class);
        AttachmentEntity artifact = artifact("mx-generated-image.png", "image/png");
        when(imageService.generate(any(), eq("Gere uma imagem de um hamster em um carro"), eq(640), eq(640)))
                .thenReturn(artifact);

        UUID userId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        MediaSkill skill = new MediaSkill(imageService, videoService, () -> documentService);

        SkillResult result = skill.execute(
                new SkillRequest("Gere uma imagem de um hamster em um carro"),
                new SkillExecutionContext(userId, correlationId, AutonomyLevel.EXECUTE_AUTONOMOUSLY)
        );

        verify(imageService).generate(userId, "Gere uma imagem de um hamster em um carro", 640, 640);
        assertThat(result.skillName()).isEqualTo("media");
        assertThat(result.answer()).contains("mx-generated-image.png");
        assertThat(result.metadata()).containsEntry("mediaType", "IMAGE");
        assertThat(result.correlationId()).isEqualTo(correlationId);
    }

    private AttachmentEntity artifact(String filename, String contentType) {
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setId(UUID.randomUUID());
        attachment.setOriginalFilename(filename);
        attachment.setContentType(contentType);
        return attachment;
    }
}
