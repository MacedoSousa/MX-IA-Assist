package com.macedxs.mx.evolution.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.macedxs.mx.evolution.application.SelfExtensionJob;
import com.macedxs.mx.evolution.application.SelfExtensionJobStore;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FileSystemSelfExtensionJobStore implements SelfExtensionJobStore {

    private final Path pendingDirectory;
    private final ObjectMapper objectMapper;

    public FileSystemSelfExtensionJobStore(Path root, ObjectMapper objectMapper) {
        if (root == null || objectMapper == null) {
            throw new IllegalArgumentException("Self-extension job store dependencies are required");
        }
        this.pendingDirectory = root.resolve("jobs").resolve("pending").normalize();
        this.objectMapper = objectMapper;
    }

    @Override
    public SelfExtensionJob save(SelfExtensionJob job) {
        if (job == null) {
            throw new IllegalArgumentException("Self-extension job is required");
        }
        try {
            Files.createDirectories(pendingDirectory);
            Path target = pendingDirectory.resolve(job.jobId() + ".json").normalize();
            if (!target.getParent().equals(pendingDirectory)) {
                throw new IllegalArgumentException("Invalid self-extension job path");
            }
            Path temporary = Files.createTempFile(pendingDirectory, job.jobId().toString(), ".tmp");
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), job);
                moveAtomically(temporary, target);
            } finally {
                Files.deleteIfExists(temporary);
            }
            return job;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist self-extension job", exception);
        }
    }

    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
