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
    void shouldIncludeReproducibleCitationMetadataForVersionedStudyChunks() {
        StudyKnowledgeContext context = StudyKnowledgeContext.fromClasspath();

        String promptContext = context.promptContext("Como aplicar RAG documental com avaliação de qualidade?");

        assertThat(promptContext)
                .contains("Citação:")
                .contains("origem=")
                .contains("trecho=")
                .contains("versão=sha256:");
    }

    @Test
    void shouldAvoidInventingPageNumbersWhenTheImportedChunkHasNoPageMetadata() {
        StudyKnowledgeContext context = StudyKnowledgeContext.fromClasspath();

        String promptContext = context.promptContext("Como aplicar RAG documental com avaliação de qualidade?");

        assertThat(promptContext).contains("página=não informada");
    }

    @Test
    void rag001ShouldReturnAValidDocumentCitationForCoveredStudyContent() {
        StudyKnowledgeContext context = StudyKnowledgeContext.fromClasspath();

        String promptContext = context.promptContext("Explique RAG documental e avaliação de qualidade.");

        assertThat(promptContext)
                .contains("Fonte:")
                .contains("Citação:")
                .containsPattern("versão=sha256:[a-f0-9]{64}");
    }

    @Test
    void rag002ShouldReportInsufficientEvidenceForAnUnknownTopic() {
        StudyKnowledgeContext context = new StudyKnowledgeContext("orientação local sobre testes");

        StudyKnowledgeContext.Coverage coverage = context.assessCoverage("astronomia observacional planetária");

        assertThat(coverage.sufficient()).isFalse();
        assertThat(coverage.bestSource()).isBlank();
    }

    @Test
    void rag003ShouldPreserveTheCurrentChunkVersionHashInTheCitation() {
        StudyKnowledgeContext context = StudyKnowledgeContext.fromClasspath();

        String promptContext = context.promptContext("Como usar RAG documental para buscar evidências?");

        assertThat(promptContext).containsPattern("versão=sha256:[a-f0-9]{64}");
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
