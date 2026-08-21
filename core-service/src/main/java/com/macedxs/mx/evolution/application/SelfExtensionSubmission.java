package com.macedxs.mx.evolution.application;

import java.util.List;

public record SelfExtensionSubmission(
        ExtensionType type,
        String slug,
        String description,
        List<FileChange> files,
        List<String> validations,
        String commitMessage,
        boolean allowPush
) {

    public SelfExtensionSubmission {
        if (type == null) {
            throw new IllegalArgumentException("Extension type is required");
        }
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Extension slug is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Extension description is required");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one extension file is required");
        }
        if (validations == null || validations.isEmpty()) {
            throw new IllegalArgumentException("At least one validation is required");
        }
        if (commitMessage == null || commitMessage.isBlank()) {
            throw new IllegalArgumentException("Commit message is required");
        }
        files = List.copyOf(files);
        validations = List.copyOf(validations);
    }

    public enum ExtensionType {
        AGENT,
        SKILL,
        TOOL
    }

    public record FileChange(String path, String content) {
        public FileChange {
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("Extension file path is required");
            }
            if (content == null) {
                throw new IllegalArgumentException("Extension file content is required");
            }
        }
    }
}
