package com.macedxs.mx.agent.skill.development;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.agent.application.SkillExecutionContext;
import com.macedxs.mx.agent.application.SkillRequest;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.evolution.application.SelfExtensionJob;
import com.macedxs.mx.evolution.application.SelfExtensionJobStore;
import com.macedxs.mx.evolution.application.SelfExtensionPolicy;
import com.macedxs.mx.evolution.application.SelfExtensionService;
import com.macedxs.mx.tool.application.PolicyEngine;
import com.macedxs.mx.tool.application.ToolExecutor;
import com.macedxs.mx.tool.application.ToolRegistry;
import com.macedxs.mx.tool.evolution.SelfExtensionSubmitTool;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SelfImprovementSkillTest {

    @Test
    void queuesOnlyAllowlistedStructuredPlanAtAutonomousInternalLevel() {
        CapturingStore store = new CapturingStore();
        SelfExtensionService service = new SelfExtensionService(
                new ObjectMapper(),
                new SelfExtensionPolicy(),
                store
        );
        ToolRegistry registry = new ToolRegistry();
        registry.register(new SelfExtensionSubmitTool(service));
        String answer = "[MX_TOOL_CALL]{\"toolName\":\"self_extension.submit\",\"arguments\":{"
                + "\"type\":\"SKILL\",\"slug\":\"safe-skill\","
                + "\"description\":\"Skill segura\","
                + "\"files\":[{\"path\":\"skills/safe/Skill.java\",\"content\":\"package skills.safe;\"}],"
                + "\"validations\":[\"maven_test\"],"
                + "\"commitMessage\":\"feat(mx): add safe skill\",\"allowPush\":false"
                + "}}[/MX_TOOL_CALL]";

        SelfImprovementSkill skill = new SelfImprovementSkill(
                new FixedGateway(answer),
                new ToolExecutor(registry, new PolicyEngine())
        );
        var result = skill.execute(
                new SkillRequest("criar uma skill segura"),
                new SkillExecutionContext(UUID.randomUUID(), UUID.randomUUID(), AutonomyLevel.EXECUTE_AUTONOMOUSLY)
        );

        assertThat(result.metadata()).containsEntry("selfExtensionQueued", true);
        assertThat(result.metadata()).containsKey("jobId");
        assertThat(store.saved).isNotNull();
        assertThat(store.saved.submission().files().getFirst().path()).isEqualTo("skills/safe/Skill.java");
    }

    private static final class FixedGateway implements ModelGateway {
        private final String answer;

        private FixedGateway(String answer) {
            this.answer = answer;
        }

        @Override
        public ModelResponse complete(ModelRequest request) {
            return new ModelResponse(answer, "test-model", 1L);
        }
    }

    private static final class CapturingStore implements SelfExtensionJobStore {
        private SelfExtensionJob saved;

        @Override
        public SelfExtensionJob save(SelfExtensionJob job) {
            saved = job;
            return job;
        }
    }
}
