package com.macedxs.mx.evolution.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SelfExtensionService {

    private final ObjectMapper objectMapper;
    private final SelfExtensionPolicy policy;
    private final SelfExtensionJobStore jobStore;

    public SelfExtensionService(
            ObjectMapper objectMapper,
            SelfExtensionPolicy policy,
            SelfExtensionJobStore jobStore
    ) {
        if (objectMapper == null || policy == null || jobStore == null) {
            throw new IllegalArgumentException("Self-extension service dependencies are required");
        }
        this.objectMapper = objectMapper;
        this.policy = policy;
        this.jobStore = jobStore;
    }

    public SubmissionResult submit(UUID userId, UUID correlationId, Map<String, Object> arguments) {
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        SelfExtensionSubmission submission = parse(arguments);
        policy.validate(submission);
        List<SelfExtensionSubmission.FileChange> normalizedFiles = submission.files().stream()
                .map(file -> new SelfExtensionSubmission.FileChange(policy.normalizePath(file.path()), file.content()))
                .toList();
        SelfExtensionSubmission normalized = new SelfExtensionSubmission(
                submission.type(),
                submission.slug(),
                submission.description(),
                normalizedFiles,
                submission.validations(),
                submission.commitMessage(),
                submission.allowPush()
        );
        SelfExtensionJob saved = jobStore.save(SelfExtensionJob.pending(userId, correlationId, normalized));
        return new SubmissionResult(
                saved.jobId(),
                saved.status().name(),
                normalized.files().stream().map(SelfExtensionSubmission.FileChange::path).toList(),
                normalized.validations()
        );
    }

    private SelfExtensionSubmission parse(Map<String, Object> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("Self-extension arguments are required");
        }
        JsonNode root = objectMapper.valueToTree(arguments);
        String typeValue = requiredText(root, "type").toUpperCase(java.util.Locale.ROOT);
        SelfExtensionSubmission.ExtensionType type;
        try {
            type = SelfExtensionSubmission.ExtensionType.valueOf(typeValue);
        } catch (IllegalArgumentException invalidType) {
            throw new IllegalArgumentException("Unsupported extension type: " + typeValue);
        }

        JsonNode filesNode = root.get("files");
        if (filesNode == null || !filesNode.isArray()) {
            throw new IllegalArgumentException("Extension files must be an array");
        }
        List<SelfExtensionSubmission.FileChange> files = new ArrayList<>();
        filesNode.forEach(fileNode -> {
            if (fileNode == null || !fileNode.isObject()) {
                throw new IllegalArgumentException("Each extension file must be an object");
            }
            files.add(new SelfExtensionSubmission.FileChange(
                    requiredText(fileNode, "path"),
                    requiredText(fileNode, "content")
            ));
        });

        JsonNode validationsNode = root.get("validations");
        if (validationsNode == null || !validationsNode.isArray()) {
            throw new IllegalArgumentException("Extension validations must be an array");
        }
        List<String> validations = new ArrayList<>();
        validationsNode.forEach(validation -> {
            if (validation == null || !validation.isTextual() || validation.asText().isBlank()) {
                throw new IllegalArgumentException("Extension validation must be a non-empty string");
            }
            validations.add(validation.asText().trim().toLowerCase(java.util.Locale.ROOT));
        });

        return new SelfExtensionSubmission(
                type,
                requiredText(root, "slug"),
                requiredText(root, "description"),
                files,
                validations,
                requiredText(root, "commitMessage"),
                root.path("allowPush").asBoolean(false)
        );
    }

    private String requiredText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Required self-extension field is missing: " + field);
        }
        return value.asText().trim();
    }

    public record SubmissionResult(
            UUID jobId,
            String status,
            List<String> files,
            List<String> validations
    ) {
        public SubmissionResult {
            if (jobId == null || status == null || status.isBlank()) {
                throw new IllegalArgumentException("Submission result identity is required");
            }
            files = files == null ? List.of() : List.copyOf(files);
            validations = validations == null ? List.of() : List.copyOf(validations);
        }

        public Map<String, Object> asMap() {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("jobId", jobId.toString());
            output.put("status", status);
            output.put("files", files);
            output.put("validations", validations);
            output.put("runner", "scripts/mx_evolution_runner.py");
            return output;
        }
    }
}
