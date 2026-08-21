package com.macedxs.mx.bootstrap;

import com.macedxs.mx.agent.application.SkillRegistry;
import com.macedxs.mx.agent.application.SkillRouter;
import com.macedxs.mx.agent.skill.development.DevelopmentSkill;
import com.macedxs.mx.agent.skill.general.GeneralSkill;
import com.macedxs.mx.agent.skill.quality.QualitySkill;
import com.macedxs.mx.agent.skill.infrastructure.InfrastructureSkill;
import com.macedxs.mx.agent.skill.data.DataSkill;
import com.macedxs.mx.agent.skill.teaching.TeachingSkill;
import com.macedxs.mx.conversation.application.SendMessageUseCase;
import com.macedxs.mx.conversation.application.port.ConversationStore;
import com.macedxs.mx.conversation.application.port.ModelGateway;
import com.macedxs.mx.core.application.MxCoreService;
import com.macedxs.mx.core.application.run.ApproveExecutionRunUseCase;
import com.macedxs.mx.core.application.run.CancelExecutionRunUseCase;
import com.macedxs.mx.core.application.run.ExecutionRunStore;
import com.macedxs.mx.core.application.run.GetExecutionRunUseCase;
import com.macedxs.mx.core.application.run.ListExecutionRunsUseCase;
import com.macedxs.mx.core.application.run.RejectExecutionRunUseCase;
import com.macedxs.mx.core.infrastructure.MxCoreModelGateway;
import com.macedxs.mx.tool.application.PolicyEngine;
import com.macedxs.mx.tool.application.ToolExecutor;
import com.macedxs.mx.tool.application.ToolRegistry;
import com.macedxs.mx.tool.workspace.WorkspaceListTool;
import com.macedxs.mx.tool.workspace.WorkspaceReadFileTool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.macedxs.mx.identity.service.UserPreferenceService;

@Configuration
public class ConversationConfiguration {

    @Bean
    GeneralSkill generalSkill(
            @Qualifier("ollamaModelGateway") ModelGateway modelGateway,
            UserPreferenceService userPreferenceService
    ) {
        return new GeneralSkill(
                modelGateway,
                com.macedxs.mx.agent.skill.general.StudyKnowledgeContext.fromClasspath(),
                userPreferenceService
        );
    }

    @Bean
    DevelopmentSkill developmentSkill(
            @Qualifier("ollamaModelGateway") ModelGateway modelGateway,
            ToolExecutor toolExecutor
    ) {
        return new DevelopmentSkill(modelGateway, toolExecutor);
    }

    @Bean
    QualitySkill qualitySkill(@Qualifier("ollamaModelGateway") ModelGateway modelGateway) {
        return new QualitySkill(modelGateway);
    }

    @Bean
    InfrastructureSkill infrastructureSkill(@Qualifier("ollamaModelGateway") ModelGateway modelGateway) {
        return new InfrastructureSkill(modelGateway);
    }

    @Bean
    DataSkill dataSkill(@Qualifier("ollamaModelGateway") ModelGateway modelGateway) {
        return new DataSkill(modelGateway);
    }

    @Bean
    TeachingSkill teachingSkill(@Qualifier("ollamaModelGateway") ModelGateway modelGateway) {
        return new TeachingSkill(modelGateway);
    }

    @Bean
    SkillRegistry skillRegistry(
            GeneralSkill generalSkill,
            DevelopmentSkill developmentSkill,
            QualitySkill qualitySkill,
            InfrastructureSkill infrastructureSkill,
            DataSkill dataSkill,
            TeachingSkill teachingSkill
    ) {
        SkillRegistry registry = new SkillRegistry();
        registry.register(generalSkill);
        registry.register(developmentSkill);
        registry.register(qualitySkill);
        registry.register(infrastructureSkill);
        registry.register(dataSkill);
        registry.register(teachingSkill);
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
    ListExecutionRunsUseCase listExecutionRunsUseCase(ExecutionRunStore executionRunStore) {
        return new ListExecutionRunsUseCase(executionRunStore);
    }

    @Bean
    ApproveExecutionRunUseCase approveExecutionRunUseCase(ExecutionRunStore executionRunStore) {
        return new ApproveExecutionRunUseCase(executionRunStore);
    }

    @Bean
    CancelExecutionRunUseCase cancelExecutionRunUseCase(ExecutionRunStore executionRunStore) {
        return new CancelExecutionRunUseCase(executionRunStore);
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
    WorkspaceReadFileTool workspaceReadFileTool(
            @Value("${mx.workspace.root:.}") String workspaceRoot,
            @Value("${mx.workspace.max-read-bytes:1048576}") long maxReadBytes
    ) {
        return new WorkspaceReadFileTool(Path.of(workspaceRoot), maxReadBytes);
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
            WorkspaceReadFileTool workspaceReadFileTool,
            com.macedxs.mx.tool.workspace.WorkspaceWriteTool workspaceWriteTool
    ) {
        ToolRegistry registry = new ToolRegistry();
        registry.register(workspaceListTool);
        registry.register(workspaceReadFileTool);
        registry.register(workspaceWriteTool);
        return registry;
    }

    @Bean
    PolicyEngine policyEngine() {
        return new PolicyEngine();
    }

    @Bean
    ToolExecutor toolExecutor(
            ToolRegistry toolRegistry,
            PolicyEngine policyEngine,
            ExecutionRunStore executionRunStore,
            @Value("${mx.approval.expiration-ms:900000}") long approvalExpirationMs
    ) {
        return new ToolExecutor(
                toolRegistry,
                policyEngine,
                executionRunStore,
                Duration.ofMillis(approvalExpirationMs)
        );
    }

    @Bean
    SendMessageUseCase sendMessageUseCase(
            ConversationStore conversationStore,
            MxCoreModelGateway mxCoreModelGateway,
            com.macedxs.mx.attachment.service.AttachmentService attachmentService
    ) {
        return new SendMessageUseCase(conversationStore, mxCoreModelGateway, attachmentService);
    }
}
