package com.macedxs.mx.tool.workspace;

import com.macedxs.mx.agent.application.AutonomyLevel;
import com.macedxs.mx.tool.application.Tool;
import com.macedxs.mx.tool.application.ToolDefinition;
import com.macedxs.mx.tool.application.ToolEffect;
import com.macedxs.mx.tool.application.ToolExecutionContext;
import com.macedxs.mx.tool.application.ToolRequest;
import com.macedxs.mx.tool.application.ToolResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Creates a deliberately small, dependency-free static project inside the configured workspace. */
public final class WorkspaceInitializeStaticProjectTool implements Tool {

    public static final String NAME = "workspace.initialize_static_project";
    private final Path workspaceRoot;
    private final ToolDefinition definition = new ToolDefinition(
            NAME,
            "1.0.0",
            "Cria um projeto HTML estático mínimo dentro do workspace após aprovação humana.",
            ToolEffect.WRITE,
            AutonomyLevel.EXECUTE_WITH_APPROVAL,
            Duration.ofSeconds(10),
            Set.of("project")
    );

    public WorkspaceInitializeStaticProjectTool(Path workspaceRoot) {
        if (workspaceRoot == null) {
            throw new IllegalArgumentException("Workspace root is required");
        }
        this.workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
    }

    @Override
    public ToolDefinition definition() {
        return definition;
    }

    @Override
    public ToolResult execute(ToolRequest request, ToolExecutionContext context) {
        if (!WorkspaceToolSupport.isApprovedContext(context)) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_WRITE_APPROVAL_REQUIRED");
        }
        String project = WorkspaceToolSupport.projectName(request);
        if (project == null) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PROJECT_INVALID");
        }
        try {
            Files.createDirectories(workspaceRoot);
            if (Files.isSymbolicLink(workspaceRoot)) {
                return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PATH_FORBIDDEN");
            }
            Path projectRoot = workspaceRoot.resolve(project).normalize();
            if (!projectRoot.startsWith(workspaceRoot) || Files.exists(projectRoot, LinkOption.NOFOLLOW_LINKS)) {
                return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PROJECT_EXISTS");
            }
            Files.createDirectories(projectRoot);
            writeNew(projectRoot.resolve("index.html"), html(project));
            writeNew(projectRoot.resolve("styles.css"), css());
            writeNew(projectRoot.resolve("README.md"), readme(project));
            return ToolResult.success(NAME, context.correlationId(), Map.of(
                    "project", project,
                    "path", project,
                    "files", List.of("index.html", "styles.css", "README.md"),
                    "previewRecipe", "workspace.preview_static"
            ));
        } catch (IOException exception) {
            return ToolResult.failure(NAME, context.correlationId(), "WORKSPACE_PROJECT_CREATE_FAILED");
        }
    }

    private void writeNew(Path target, String content) throws IOException {
        Files.writeString(target, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    private String html(String project) {
        return "<!doctype html>\n<html lang=\"pt-BR\">\n<head>\n" +
                "  <meta charset=\"utf-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "  <title>" + project + " — MX Workspace</title>\n  <link rel=\"stylesheet\" href=\"styles.css\">\n" +
                "</head>\n<body>\n  <main>\n    <p class=\"eyebrow\">MX Workspace</p>\n" +
                "    <h1>" + project + "</h1>\n    <p>Projeto estático criado em workspace isolado. Solicite um preview local pelo MX antes de publicar.</p>\n" +
                "  </main>\n</body>\n</html>\n";
    }

    private String css() {
        return "body { margin: 0; font-family: system-ui, sans-serif; background: #0d1b2a; color: #f8f4e8; }\n" +
                "main { max-width: 44rem; padding: 12vh 8vw; }\n.eyebrow { color: #58d6b0; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; }\n" +
                "h1 { font-family: Georgia, serif; font-size: clamp(2.5rem, 8vw, 5rem); margin: .3rem 0 1rem; }\n";
    }

    private String readme(String project) {
        return "# " + project + "\n\n" +
                "Projeto estático criado pelo MX em workspace isolado.\n\n" +
                "## Preview local\n\n" +
                "Solicite a receita `workspace.preview_static` no MX e aprove a operação. O preview será restrito ao loopback e deverá ser encerrado explicitamente após uso.\n";
    }
}
