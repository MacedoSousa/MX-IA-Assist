package com.macedxs.mx.agent.skill.general;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudyKnowledgeContextTest {

    @Test
    void shouldLoadTheVersionedStudySummaryFromClasspath() {
        StudyKnowledgeContext context = StudyKnowledgeContext.fromClasspath();

        assertThat(context.summary())
                .contains("Síntese do conhecimento de estudos para o MX")
                .contains("RAG documental")
                .contains("não determina uma resposta única");
    }

    @Test
    void shouldLoadIndexedChunksFromClasspath() {
        StudyKnowledgeContext context = StudyKnowledgeContext.fromClasspath();

        assertThat(context.chunkCount()).isGreaterThan(0);
    }

    @Test
    void shouldRetrieveLexicallyRelevantChunksForTheUserPrompt() {
        StudyKnowledgeContext context = StudyKnowledgeContext.fromClasspath();

        String promptContext = context.promptContext("Como aplicar RAG e agentes com avaliação de qualidade?");

        assertThat(promptContext)
                .contains("Trechos relevantes do conhecimento documental de estudos")
                .contains("não privilegiados")
                .contains("Fonte:");
    }

    @Test
    void shouldFallbackToTheStudySummaryWhenThePromptIsEmpty() {
        StudyKnowledgeContext context = new StudyKnowledgeContext("orientação de teste");

        assertThat(context.promptContext(""))
                .contains("não privilegiado")
                .contains("Combine-o com evidências")
                .contains("orientação de teste");
    }

    @Test
    void shouldMarkStudyContentAsNonPrivilegedPromptContext() {
        StudyKnowledgeContext context = new StudyKnowledgeContext("orientação de teste");

        assertThat(context.promptContext("teste"))
                .contains("não privilegiado")
                .contains("Combine-o com evidências")
                .contains("orientação de teste");
    }

    @Test
    void shouldReportInsufficientCoverageForUnknownTopics() {
        StudyKnowledgeContext context = new StudyKnowledgeContext("orientação local sobre testes");

        assertThat(context.assessCoverage("astronomia observacional"))
                .extracting(StudyKnowledgeContext.Coverage::sufficient)
                .isEqualTo(false);
    }

    @Test
    void shouldDeduplicateRuntimeEvidence() {
        StudyKnowledgeContext context = new StudyKnowledgeContext("orientação local");

        assertThat(context.learnExternal(
                "https://example.org/a",
                "Fonte A",
                "Evidência externa sobre assunto novo",
                java.util.Set.of("busca-externa")
        )).isTrue();
        assertThat(context.learnExternal(
                "https://example.org/a",
                "Fonte A",
                "Evidência externa sobre assunto novo",
                java.util.Set.of("busca-externa")
        )).isFalse();
        assertThat(context.chunkCount()).isEqualTo(1);
    }
}
