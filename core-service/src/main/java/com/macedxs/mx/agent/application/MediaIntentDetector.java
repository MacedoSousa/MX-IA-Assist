package com.macedxs.mx.agent.application;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Mantém a classificação de mídia determinística e independente de infraestrutura.
 * Somente pedidos que combinam verbo de criação com uma modalidade são delegados.
 */
public final class MediaIntentDetector {

    public enum MediaType { IMAGE, VIDEO, DOCUMENT }

    private static final Pattern CREATION_VERB = Pattern.compile(
            "\\b(gere|gerar|gera|crie|criar|cria|produza|produzir|produz|faca|desenhe|desenhar)\\b");
    private static final Pattern VIDEO = Pattern.compile("\\b(video|animacao|filme|clipe)\\b");
    private static final Pattern DOCUMENT = Pattern.compile("\\b(documento|pdf|docx|relatorio|proposta|curriculo)\\b");
    private static final Pattern IMAGE = Pattern.compile("\\b(imagem|ilustracao|foto|arte|desenho|poster)\\b");

    private MediaIntentDetector() {
    }

    public static Optional<MediaType> detect(String prompt) {
        String normalized = normalize(prompt);
        if (normalized.isBlank() || normalized.contains("[mx_document_output]")) {
            return Optional.empty();
        }
        if (!CREATION_VERB.matcher(normalized).find()) {
            return Optional.empty();
        }
        if (VIDEO.matcher(normalized).find()) {
            return Optional.of(MediaType.VIDEO);
        }
        if (DOCUMENT.matcher(normalized).find()) {
            return Optional.of(MediaType.DOCUMENT);
        }
        if (IMAGE.matcher(normalized).find()) {
            return Optional.of(MediaType.IMAGE);
        }
        return Optional.empty();
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
