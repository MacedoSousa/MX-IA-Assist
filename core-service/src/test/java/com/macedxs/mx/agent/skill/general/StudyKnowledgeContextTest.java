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
    void shouldMarkStudyContentAsNonPrivilegedPromptContext() {
        StudyKnowledgeContext context = new StudyKnowledgeContext("orientação de teste");

        assertThat(context.promptContext())
                .contains("não privilegiado")
                .contains("Combine-o com evidências")
                .contains("orientação de teste");
    }
}
