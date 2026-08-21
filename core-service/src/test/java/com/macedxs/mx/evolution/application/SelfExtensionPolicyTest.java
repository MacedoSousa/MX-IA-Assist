package com.macedxs.mx.evolution.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelfExtensionPolicyTest {

    private final SelfExtensionPolicy policy = new SelfExtensionPolicy();

    @Test
    void acceptsSmallAllowlistedExtension() {
        SelfExtensionSubmission submission = validSubmission();

        policy.validate(submission);

        assertThat(policy.normalizePath("skills/observability/Skill.java"))
                .isEqualTo("skills/observability/Skill.java");
    }

    @Test
    void rejectsTraversalAndProtectedSecretPaths() {
        SelfExtensionSubmission traversal = submissionWithPath("skills/../.env");
        SelfExtensionSubmission secret = submissionWithPath("docs/private-key.txt");

        assertThatThrownBy(() -> policy.validate(traversal))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy.validate(secret))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUnknownValidationAndPushRequestByDefault() {
        SelfExtensionSubmission unknownValidation = new SelfExtensionSubmission(
                SelfExtensionSubmission.ExtensionType.SKILL,
                "safe-skill",
                "Descrição",
                List.of(new SelfExtensionSubmission.FileChange("skills/safe/Skill.java", "package skills.safe;")),
                List.of("run_shell"),
                "feat(mx): add safe skill",
                false
        );
        SelfExtensionSubmission push = new SelfExtensionSubmission(
                SelfExtensionSubmission.ExtensionType.SKILL,
                "safe-skill",
                "Descrição",
                List.of(new SelfExtensionSubmission.FileChange("skills/safe/Skill.java", "package skills.safe;")),
                List.of("maven_test"),
                "feat(mx): add safe skill",
                true
        );

        assertThatThrownBy(() -> policy.validate(unknownValidation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported self-extension validation");
        assertThatThrownBy(() -> policy.validate(push))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Remote push is disabled");
    }

    private SelfExtensionSubmission validSubmission() {
        return new SelfExtensionSubmission(
                SelfExtensionSubmission.ExtensionType.SKILL,
                "safe-skill",
                "Descrição",
                List.of(new SelfExtensionSubmission.FileChange("skills/safe/Skill.java", "package skills.safe;")),
                List.of("maven_test"),
                "feat(mx): add safe skill",
                false
        );
    }

    private SelfExtensionSubmission submissionWithPath(String path) {
        return new SelfExtensionSubmission(
                SelfExtensionSubmission.ExtensionType.SKILL,
                "safe-skill",
                "Descrição",
                List.of(new SelfExtensionSubmission.FileChange(path, "content")),
                List.of("maven_test"),
                "feat(mx): add safe skill",
                false
        );
    }
}
