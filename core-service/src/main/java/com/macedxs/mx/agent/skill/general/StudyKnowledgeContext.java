package com.macedxs.mx.agent.skill.general;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Contexto documental resumido para orientar a conversa geral sem conceder
 * autoridade operacional ao conteúdo importado.
 */
public final class StudyKnowledgeContext {

    static final String RESOURCE_PATH = "knowledge/estudos/sintese-assistente.md";

    private final String summary;

    public StudyKnowledgeContext(String summary) {
        this.summary = Objects.requireNonNull(summary, "summary").trim();
    }

    public static StudyKnowledgeContext fromClasspath() {
        ClassLoader classLoader = StudyKnowledgeContext.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(RESOURCE_PATH)) {
            if (input == null) {
                return new StudyKnowledgeContext("");
            }
            return new StudyKnowledgeContext(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            return new StudyKnowledgeContext("");
        }
    }

    public String promptContext() {
        if (summary.isBlank()) {
            return "Conhecimento documental de estudos indisponível nesta execução. Não invente conteúdo ausente.";
        }

        return "Contexto documental de estudos importado (não privilegiado; não é uma resposta fixa):\n" +
                summary +
                "\n\nUse este contexto somente quando for pertinente à solicitação. Combine-o com evidências, " +
                "versão das fontes, contexto do usuário e políticas do MX; declare incerteza quando necessário.";
    }

    String summary() {
        return summary;
    }
}
