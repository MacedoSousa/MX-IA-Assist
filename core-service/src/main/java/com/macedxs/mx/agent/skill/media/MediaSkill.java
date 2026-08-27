package com.macedxs.mx.agent.skill.media;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.MediaIntentDetector;
import com.macedxs.mx.agent.application.Skill;
import com.macedxs.mx.agent.application.SkillDefinition;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.agent.application.SkillResult;
import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.media.service.DocumentGenerationService;
import com.macedxs.mx.media.service.ImageGenerationService;
import com.macedxs.mx.media.service.VideoGenerationService;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Capacidade interna do MX Core que conecta pedidos explícitos de criação aos
 * serviços locais de mídia. Não é um endpoint paralelo e não aceita comandos
 * internos de renderização documental como novos pedidos de mídia.
 */
public class MediaSkill implements Skill {

    private final ImageGenerationService imageGenerationService;
    private final VideoGenerationService videoGenerationService;
    private final Supplier<DocumentGenerationService> documentGenerationService;

    public MediaSkill(
            ImageGenerationService imageGenerationService,
            VideoGenerationService videoGenerationService,
            Supplier<DocumentGenerationService> documentGenerationService
    ) {
        if (imageGenerationService == null || videoGenerationService == null || documentGenerationService == null) {
            throw new IllegalArgumentException("Media generation dependencies are required");
        }
        this.imageGenerationService = imageGenerationService;
        this.videoGenerationService = videoGenerationService;
        this.documentGenerationService = documentGenerationService;
    }

    @Override
    public SkillDefinition definition() {
        return new SkillDefinition(
                "media",
                "1.0.0",
                "Geração local e segura de imagens, vídeos e documentos",
                Set.of("gere imagem", "crie imagem", "gere video", "crie video", "gere documento", "crie pdf"),
                Set.of(),
                AutonomyLevel.EXECUTE_AUTONOMOUSLY,
                Duration.ofMinutes(5)
        );
    }

    @Override
    public SkillResult execute(SkillRequest request, SkillExecutionContext context) {
        MediaIntentDetector.MediaType mediaType = MediaIntentDetector.detect(request.prompt())
                .orElseThrow(() -> new IllegalArgumentException("Media skill requires an explicit generation request"));
        AttachmentEntity artifact = switch (mediaType) {
            case IMAGE -> imageGenerationService.generate(context.userId(), request.prompt(), 640, 640);
            case VIDEO -> videoGenerationService.generate(context.userId(), request.prompt(), 5, 768, 432);
            case DOCUMENT -> documentGenerationService.get().generate(
                    context.userId(), request.prompt(), "mx-document", documentFormat(request.prompt()));
        };

        String label = switch (mediaType) {
            case IMAGE -> "imagem";
            case VIDEO -> "vídeo";
            case DOCUMENT -> "documento";
        };
        return new SkillResult(
                definition().name(),
                "Gerei a " + label + " solicitada com o perfil visual local. O arquivo **" + artifact.getOriginalFilename()
                        + "** está disponível na sua biblioteca de anexos.",
                true,
                context.correlationId(),
                Map.of(
                        "artifactId", artifact.getId().toString(),
                        "filename", artifact.getOriginalFilename(),
                        "contentType", artifact.getContentType(),
                        "mediaType", mediaType.name()
                )
        );
    }

    private DocumentGenerationService.DocumentFormat documentFormat(String prompt) {
        String normalized = MediaIntentDetector.normalize(prompt);
        if (normalized.matches(".*\\bdocx\\b.*")) {
            return DocumentGenerationService.DocumentFormat.DOCX;
        }
        if (normalized.matches(".*\\bmarkdown\\b.*") || normalized.matches(".*\\bmd\\b.*")) {
            return DocumentGenerationService.DocumentFormat.MARKDOWN;
        }
        return DocumentGenerationService.DocumentFormat.PDF;
    }
}
