package com.macedxs.mx.agent.skill.general;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeQueryExpanderTest {

    private final KnowledgeQueryExpander expander = new KnowledgeQueryExpander();

    @Test
    void shouldExpandPortugueseLearningTermsWithEnglishAliases() {
        KnowledgeQueryExpander.ExpandedQuery query = expander.expand("Como melhorar meu aprendizado com machine learning?");

        assertThat(query.originalTokens()).contains("aprendizado", "machine", "learning");
        assertThat(query.expandedTokens()).contains("modelo", "training", "treino");
        assertThat(query.expandedTokens()).doesNotContain("ml");
    }

    @Test
    void shouldExpandRagAndAgentTermsWithoutCallingAnExternalService() {
        KnowledgeQueryExpander.ExpandedQuery query = expander.expand("Quero uma busca com agentes");

        assertThat(query.expandedTokens()).contains("retrieval", "rag", "workflow", "tool");
        assertThat(query.expandedOnly()).doesNotContain("quero");
    }
}
