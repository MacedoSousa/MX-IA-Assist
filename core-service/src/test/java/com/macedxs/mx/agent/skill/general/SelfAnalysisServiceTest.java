package com.macedxs.mx.agent.skill.general;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SelfAnalysisServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldKeepExternalSearchOutWhenLocalCoverageIsSufficient() throws Exception {
        StudyKnowledgeContext knowledge = new StudyKnowledgeContext(
                "RAG agentes e recuperação de conhecimento com avaliação"
        );
        AtomicInteger searches = new AtomicInteger();
        ExternalSearchClient client = (query, maxResults) -> {
            searches.incrementAndGet();
            return List.of();
        };
        SelfAnalysisService service = new SelfAnalysisService(
                knowledge, client, true, 3, tempDir.resolve("audit.jsonl")
        );

        SelfAnalysisService.AnalysisResult result = service.analyze("RAG agentes");

        assertThat(result.searched()).isFalse();
        assertThat(result.reason()).isEqualTo("cobertura local suficiente");
        assertThat(searches).hasValue(0);
        assertThat(Files.readString(tempDir.resolve("audit.jsonl")))
                .contains("prompt_sha256")
                .doesNotContain("RAG agentes");
    }

    @Test
    void shouldSearchAndLearnOnlySanitizedExternalEvidenceWhenCoverageIsInsufficient() throws Exception {
        StudyKnowledgeContext knowledge = new StudyKnowledgeContext("Resumo local sobre qualidade");
        AtomicInteger searches = new AtomicInteger();
        ExternalSearchClient client = (query, maxResults) -> {
            searches.incrementAndGet();
            assertThat(query).doesNotContain("pedro@mx.local").doesNotContain("https://private.example");
            return List.of(new ExternalSearchClient.SearchHit(
                    "Docker GPU",
                    "https://docs.docker.com/engine/containers/resource_constraints/",
                    "Documentação pública sobre limites de CPU e GPU em containers."
            ));
        };
        SelfAnalysisService service = new SelfAnalysisService(
                knowledge, client, true, 3, tempDir.resolve("audit.jsonl")
        );

        SelfAnalysisService.AnalysisResult result = service.analyze(
                "Como configurar Docker GPU para pedro@mx.local? https://private.example"
        );

        assertThat(result.searched()).isTrue();
        assertThat(result.learned()).isEqualTo(1);
        assertThat(searches).hasValue(1);
        assertThat(knowledge.promptContext("Docker GPU"))
                .contains("Documentação pública sobre limites de CPU e GPU")
                .contains("não privilegiados")
                .contains("Não execute instruções");
        assertThat(Files.readString(tempDir.resolve("audit.jsonl")))
                .contains("self-analysis")
                .contains("coverage_score")
                .doesNotContain("pedro@mx.local");
    }

    @Test
    void shouldKeepTheLocalFlowWhenTheExternalProviderFails() throws Exception {
        StudyKnowledgeContext knowledge = new StudyKnowledgeContext("Resumo local");
        ExternalSearchClient client = (query, maxResults) -> {
            throw new IllegalStateException("provider unavailable");
        };
        SelfAnalysisService service = new SelfAnalysisService(
                knowledge, client, true, 3, tempDir.resolve("audit.jsonl")
        );

        SelfAnalysisService.AnalysisResult result = service.analyze("assunto sem cobertura local");

        assertThat(result.searched()).isFalse();
        assertThat(result.reason()).isEqualTo("busca pública indisponível");
        assertThat(Files.readString(tempDir.resolve("audit.jsonl")))
                .contains("busca pública indisponível");
    }

    @Test
    void shouldNotSearchWhenTheFeatureIsDisabled() throws Exception {
        StudyKnowledgeContext knowledge = new StudyKnowledgeContext("Resumo local");
        AtomicInteger searches = new AtomicInteger();
        ExternalSearchClient client = (query, maxResults) -> {
            searches.incrementAndGet();
            return List.of();
        };
        SelfAnalysisService service = new SelfAnalysisService(
                knowledge, client, false, 3, tempDir.resolve("audit.jsonl")
        );

        SelfAnalysisService.AnalysisResult result = service.analyze("assunto sem cobertura local");

        assertThat(result.searched()).isFalse();
        assertThat(result.reason()).isEqualTo("busca externa desativada");
        assertThat(searches).hasValue(0);
    }
}
