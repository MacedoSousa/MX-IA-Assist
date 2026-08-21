package com.macedxs.mx.evolution.application;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class SelfExtensionPolicy {

    public static final Set<String> ALLOWED_VALIDATIONS = Set.of(
            "maven_test",
            "web_typecheck",
            "web_export",
            "compose_config",
            "docker_build_core",
            "docker_build_web"
    );

    private static final List<String> ALLOWED_PREFIXES = List.of(
            "core-service/src/main/",
            "core-service/src/test/",
            "clients/mx-app/",
            "skills/",
            "scripts/",
            "docs/",
            "infrastructure/docker/"
    );
    private static final Pattern SLUG = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+){0,5}");
    private static final Pattern SECRET_NAME = Pattern.compile("(?i)(^|/)(\\.env(?:\\..*)?|.*(?:secret|token|password|credential|private.?key).*)$");
    private static final Pattern COMMIT_MESSAGE = Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N} ._:/()+'-]{2,119}");

    private final int maxFiles;
    private final long maxFileBytes;
    private final long maxTotalBytes;
    private final boolean allowPushRequests;

    public SelfExtensionPolicy() {
        this(20, 262_144L, 1_048_576L, false);
    }

    public SelfExtensionPolicy(int maxFiles, long maxFileBytes, long maxTotalBytes, boolean allowPushRequests) {
        if (maxFiles < 1 || maxFileBytes < 1 || maxTotalBytes < maxFileBytes) {
            throw new IllegalArgumentException("Invalid self-extension limits");
        }
        this.maxFiles = maxFiles;
        this.maxFileBytes = maxFileBytes;
        this.maxTotalBytes = maxTotalBytes;
        this.allowPushRequests = allowPushRequests;
    }

    public void validate(SelfExtensionSubmission submission) {
        if (submission == null) {
            throw new IllegalArgumentException("Self-extension submission is required");
        }
        if (!SLUG.matcher(submission.slug().trim()).matches()) {
            throw new IllegalArgumentException("Extension slug must be a lowercase kebab-case identifier");
        }
        if (submission.files().size() > maxFiles) {
            throw new IllegalArgumentException("Extension contains too many files");
        }
        long totalBytes = 0L;
        for (SelfExtensionSubmission.FileChange file : submission.files()) {
            String normalizedPath = normalizePath(file.path());
            long bytes = file.content().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            if (bytes > maxFileBytes) {
                throw new IllegalArgumentException("Extension file exceeds the maximum size: " + normalizedPath);
            }
            totalBytes = Math.addExact(totalBytes, bytes);
            if (totalBytes > maxTotalBytes) {
                throw new IllegalArgumentException("Extension payload exceeds the maximum size");
            }
        }
        for (String validation : submission.validations()) {
            if (validation == null || !ALLOWED_VALIDATIONS.contains(validation.trim().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Unsupported self-extension validation: " + validation);
            }
        }
        String commitMessage = submission.commitMessage().trim();
        if (!COMMIT_MESSAGE.matcher(commitMessage).matches() || commitMessage.contains("\n") || commitMessage.contains("\r")) {
            throw new IllegalArgumentException("Commit message is invalid");
        }
        if (submission.allowPush() && !allowPushRequests) {
            throw new IllegalArgumentException("Remote push is disabled by self-extension policy");
        }
    }

    public String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Extension file path is required");
        }
        String normalized = path.trim().replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("\u0000") || normalized.contains("../")
                || normalized.equals("..") || normalized.contains("/.git/") || normalized.equals(".git")) {
            throw new IllegalArgumentException("Extension file path is unsafe: " + path);
        }
        if (SECRET_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Extension file path targets a protected secret: " + path);
        }
        if (ALLOWED_PREFIXES.stream().noneMatch(normalized::startsWith)) {
            throw new IllegalArgumentException("Extension file path is outside the allowlist: " + path);
        }
        return normalized;
    }
}
