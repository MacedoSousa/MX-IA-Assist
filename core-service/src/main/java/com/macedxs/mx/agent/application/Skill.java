package com.macedxs.mx.agent.application;

import java.util.Objects;
import java.util.function.Consumer;

public interface Skill {

    SkillDefinition definition();

    SkillResult execute(SkillRequest request, SkillExecutionContext context);

    default SkillResult stream(
            SkillRequest request,
            SkillExecutionContext context,
            Consumer<String> chunkConsumer
    ) {
        Objects.requireNonNull(chunkConsumer, "Chunk consumer is required");
        SkillResult result = execute(request, context);
        if (result != null && result.answer() != null && !result.answer().isEmpty()) {
            chunkConsumer.accept(result.answer());
        }
        return result;
    }
}
