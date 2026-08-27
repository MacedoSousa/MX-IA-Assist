package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

final class WorkspaceToolSupport {

    private static final Pattern PROJECT_NAME = Pattern.compile("[a-z0-9][a-z0-9-]{0,47}");

    private WorkspaceToolSupport() {
    }

    static boolean isApprovedContext(ToolExecutionContext context) {
        return context != null && context.grantedAutonomy() == AutonomyLevel.EXECUTE_WITH_APPROVAL;
    }

    static String projectName(ToolRequest request) {
        if (request == null || request.arguments() == null) {
            return null;
        }
        Object candidate = request.arguments().get("project");
        if (!(candidate instanceof String project)) {
            return null;
        }
        String normalized = project.trim().toLowerCase(java.util.Locale.ROOT);
        return PROJECT_NAME.matcher(normalized).matches() ? normalized : null;
    }

    static boolean containsSymbolicLink(Path root, Path candidate) throws IOException {
        if (candidate == null || !candidate.startsWith(root)) {
            return true;
        }
        Path current = root;
        for (Path segment : root.relativize(candidate)) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                return true;
            }
        }
        return false;
    }
}
