package com.macedxs.mx.media.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macedxs.mx.attachment.entity.AttachmentEntity;
import com.macedxs.mx.attachment.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageGenerationService {

    private static final String DEFAULT_NEGATIVE_PROMPT = "blurry, low quality, lowres, out of focus, soft focus, distorted, deformed, duplicate, watermark, signature, text artifacts";
    private static final double DEFAULT_CFG_SCALE = 7.0;
    private static final String DEFAULT_SAMPLER = "DPM++ 2M";

    public record GenerationOptions(String negativePrompt, Long seed, Double cfgScale) {
        public static final GenerationOptions DEFAULT = new GenerationOptions(
                DEFAULT_NEGATIVE_PROMPT,
                null,
                DEFAULT_CFG_SCALE
        );
    }

    private final AttachmentService attachmentService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String baseUrl;
    private final int maxPixels;
    private final int steps;

    @Autowired
    public ImageGenerationService(
            AttachmentService attachmentService,
            @Value("${mx.media.image-generation.enabled:false}") boolean enabled,
            @Value("${mx.media.image-generation.url:http://host.docker.internal:7860}") String baseUrl,
            @Value("${mx.media.image-generation.max-pixels:1048576}") int maxPixels,
            @Value("${mx.media.image-generation.steps:32}") int steps
    ) {
        this(
                attachmentService,
                new ObjectMapper(),
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                enabled,
                baseUrl,
                maxPixels,
                steps
        );
    }

    ImageGenerationService(
            AttachmentService attachmentService,
            ObjectMapper objectMapper,
            HttpClient httpClient,
            boolean enabled,
            String baseUrl,
            int maxPixels,
            int steps
    ) {
        if (attachmentService == null || objectMapper == null || httpClient == null) {
            throw new IllegalArgumentException("Image generation dependencies are required");
        }
        if (maxPixels <= 0 || steps <= 0) {
            throw new IllegalArgumentException("Image generation limits must be positive");
        }
        this.attachmentService = attachmentService;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.enabled = enabled;
        this.baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "http://host.docker.internal:7860"
                : baseUrl.replaceAll("/+$", "");
        this.maxPixels = maxPixels;
        this.steps = steps;
    }

    public AttachmentEntity generate(UUID userId, String prompt, int width, int height) {
        return generate(userId, prompt, width, height, GenerationOptions.DEFAULT);
    }

    public AttachmentEntity generate(UUID userId, String prompt, int width, int height, GenerationOptions options) {
        byte[] png = render(userId, prompt, width, height, options);
        return attachmentService.store(
                userId,
                "mx-generated-" + UUID.randomUUID() + ".png",
                "image/png",
                png.length,
                new ByteArrayInputStream(png)
        );
    }

    /**
     * Produz somente os bytes do quadro visual. Serviços compostos, como vídeo,
     * podem usar o resultado sem criar um anexo intermediário exposto ao usuário.
     */
    public byte[] render(UUID userId, String prompt, int width, int height) {
        return render(userId, prompt, width, height, GenerationOptions.DEFAULT);
    }

    public byte[] render(UUID userId, String prompt, int width, int height, GenerationOptions options) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (prompt == null || prompt.isBlank() || prompt.length() > 4000) {
            throw new IllegalArgumentException("Image prompt must contain 1 to 4000 characters");
        }
        if (width < 64 || height < 64 || ((long) width * height) > maxPixels) {
            throw new IllegalArgumentException("Image dimensions exceed the configured limit");
        }
        if (!enabled) {
            throw new IllegalStateException("Local image generation is disabled; configure a local Stable Diffusion endpoint first");
        }

        GenerationOptions normalizedOptions = normalizeOptions(options);
        validateOptions(normalizedOptions);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("prompt", prompt.trim());
        if (normalizedOptions.negativePrompt() != null && !normalizedOptions.negativePrompt().isBlank()) {
            payload.put("negative_prompt", normalizedOptions.negativePrompt().trim());
        }
        if (normalizedOptions.seed() != null) {
            payload.put("seed", normalizedOptions.seed());
        }
        if (normalizedOptions.cfgScale() != null) {
            payload.put("cfg_scale", normalizedOptions.cfgScale());
        }
        payload.put("width", width);
        payload.put("height", height);
        payload.put("steps", steps);
        payload.put("sampler_name", DEFAULT_SAMPLER);
        payload.put("batch_size", 1);
        payload.put("send_images", true);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/sdapi/v1/txt2img"))
                    .timeout(Duration.ofSeconds(180))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Image generation endpoint failed with status " + response.statusCode());
            }
            JsonNode images = objectMapper.readTree(response.body()).path("images");
            if (!images.isArray() || images.isEmpty() || images.get(0).asText().isBlank()) {
                throw new IllegalStateException("Image generation endpoint returned no image");
            }
            return Base64.getDecoder().decode(stripDataUri(images.get(0).asText()));
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate local image", exception);
        }
    }

    private String stripDataUri(String value) {
        int comma = value.indexOf(',');
        return comma >= 0 ? value.substring(comma + 1) : value;
    }

    private GenerationOptions normalizeOptions(GenerationOptions options) {
        GenerationOptions requested = options == null ? GenerationOptions.DEFAULT : options;
        String negativePrompt = requested.negativePrompt() == null || requested.negativePrompt().isBlank()
                ? DEFAULT_NEGATIVE_PROMPT
                : requested.negativePrompt().trim();
        double cfgScale = requested.cfgScale() == null ? DEFAULT_CFG_SCALE : requested.cfgScale();
        return new GenerationOptions(negativePrompt, requested.seed(), cfgScale);
    }

    private void validateOptions(GenerationOptions options) {
        if (options.negativePrompt() != null && options.negativePrompt().length() > 2_000) {
            throw new IllegalArgumentException("Image negative prompt cannot exceed 2000 characters");
        }
        if (options.seed() != null && options.seed() < -1) {
            throw new IllegalArgumentException("Image seed must be -1 or a non-negative number");
        }
        if (options.cfgScale() != null && (options.cfgScale() < 1.0 || options.cfgScale() > 30.0)) {
            throw new IllegalArgumentException("Image guidance scale must be between 1 and 30");
        }
    }
}
