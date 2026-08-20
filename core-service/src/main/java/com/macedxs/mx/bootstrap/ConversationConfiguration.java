package com.macedxs.mx.bootstrap;

import com.macedxs.mx.agent.application.Skill;
import com.macedxs.mx.agent.application.SkillRegistry;
import com.macedxs.mx.agent.application.SkillRouter;
import com.macedxs.mx.tool.application.PolicyEngine;
import com.macedxs.mx.tool.application.ToolExecutor;
import com.macedxs.mx.tool.application.ToolRegistry;
import com.macedxs.mx.tool.workspace.WorkspaceListTool;
import org.springframework.beans.factory.annotation.Value;
import com.macedxs.mx.agent.skill.development.DevelopmentSkill;
import com.macedxs.mx.agent.skill.general.GeneralSkill;
import com.macedxs.mx.conversation.application.SendMessageUseCase;
import com.macedxs.mx.conversation.application.port.ConversationStore;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.core.application.MxCoreService;
import com.macedxs.mx.core.application.run.ApproveExecutionRunUseCase;
import com.macedxs.mx.core.application.run.ExecutionRunStore;
import com.macedxs.mx.core.application.run.RejectExecutionRunUseCase;
import com.macedxs.mx.core.application.run.GetExecutionRunUseCase;
import com.macedxs.mx.core.infrastructure.MxCoreModelGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ConversationConfiguration {

    @Bean
    GeneralSkill generalSkill(@Qualifier("ollamaModelGateway") ModelGateway modelGateway) {
        return new GeneralSkill(modelGateway);
    }

    @Bean
    DevelopmentSkill developmentSkill(@Qualifier("ollamaModelGateway") ModelGateway modelGateway) {
        return new DevelopmentSkill(modelGateway);
    }

    @Bean
    SkillRegistry skillRegistry(GeneralSkill generalSkill, DevelopmentSkill developmentSkill) {
        SkillRegistry registry = new SkillRegistry();
        registry.register(generalSkill);
        registry.register(developmentSkill);
        return registry;
    }

    @Bean
    SkillRouter skillRouter(SkillRegistry skillRegistry) {
        return new SkillRouter(skillRegistry);
    }

    @Bean
    MxCoreService mxCoreService(
            SkillRouter skillRouter,
            com.macedxs.mx.core.application.SkillTelemetry telemetry,
            ExecutionRunStore executionRunStore
    ) {
        return new MxCoreService(skillRouter, telemetry, executionRunStore);
    }

    @Bean
    GetExecutionRunUseCase getExecutionRunUseCase(ExecutionRunStore executionRunStore) {
        return new GetExecutionRunUseCase(executionRunStore);
    }

    @Bean
    ApproveExecutionRunUseCase approveExecutionRunUseCase(ExecutionRunStore executionRunStore) {
        return new ApproveExecutionRunUseCase(executionRunStore);
    }

    @Bean
    RejectExecutionRunUseCase rejectExecutionRunUseCase(ExecutionRunStore executionRunStore) {
        return new RejectExecutionRunUseCase(executionRunStore);
    }

    @Bean(destroyMethod = "close")
    ExecutorService mxStreamingExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    WorkspaceListTool workspaceListTool(
            @Value("${mx.workspace.root:.}") String workspaceRoot,
            @Value("${mx.workspace.max-entries:500}") int maxEntries,
            @Value("${mx.workspace.max-depth:4}") int maxDepth
    ) {
        return new WorkspaceListTool(Path.of(workspaceRoot), maxEntries, maxDepth);
    }

    @Bean
    com.macedxs.mx.tool.workspace.WorkspaceWriteTool workspaceWriteTool(
            @Value("${mx.workspace.root:.}") String workspaceRoot,
            @Value("${mx.workspace.max-write-bytes:1048576}") long maxWriteBytes
    ) {
        return new com.macedxs.mx.tool.workspace.WorkspaceWriteTool(Path.of(workspaceRoot), maxWriteBytes);
    }

    @Bean
    ToolRegistry toolRegistry(
            WorkspaceListTool workspaceListTool,
            com.macedxs.mx.tool.workspace.WorkspaceWriteTool workspaceWriteTool
    ) {
        ToolRegistry registry = new ToolRegistry();
        registry.register(workspaceListTool);
        registry.register(workspaceWriteTool);
        return registry;
    }

    @Bean
    PolicyEngine policyEngine() {
        return new PolicyEngine();
    }

    @Bean
    ToolExecutor toolExecutor(ToolRegistry toolRegistry, PolicyEngine policyEngine) {
        return new ToolExecutor(toolRegistry, policyEngine);
    }

    @Bean
    SendMessageUseCase sendMessageUseCase(
            ConversationStore conversationStore,
            MxCoreModelGateway mxCoreModelGateway
    ) {
        return new SendMessageUseCase(conversationStore, mxCoreModelGateway);
    }
}
