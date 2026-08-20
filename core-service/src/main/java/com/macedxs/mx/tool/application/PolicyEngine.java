package com.macedxs.mx.tool.application;

import com.macedxs.mx.agent.application.AutonomyLevel;

import java.util.Map;

public class PolicyEngine {

    public PolicyDecision evaluate(
            ToolDefinition definition,
            ToolRequest request,
            AutonomyLevel grantedAutonomy,
            boolean approved
    ) {
        if (definition == null) {
            return PolicyDecision.deny("Tool definition is required");
        }
        if (request == null) {
            return PolicyDecision.deny("Tool request is required");
        }
        if (!definition.name().equalsIgnoreCase(request.toolName())) {
            return PolicyDecision.deny("Tool request does not match the registered definition");
        }
        if (grantedAutonomy == null) {
            return PolicyDecision.deny("No autonomy was granted");
        }
        if (!hasRequiredArguments(definition, request.arguments())) {
            return PolicyDecision.deny("Required tool arguments are missing");
        }

        if (definition.effect() == ToolEffect.READ_ONLY) {
            if (grantedAutonomy.ordinal() < definition.minimumAutonomy().ordinal()) {
                return PolicyDecision.deny("Read-only tool requires execution autonomy");
            }
            return PolicyDecision.allow("Allowed read-only tool");
        }

        if (grantedAutonomy.ordinal() < AutonomyLevel.PROPOSE.ordinal()) {
            return PolicyDecision.deny("Sensitive tool cannot be invoked from response-only autonomy");
        }
        if (!approved) {
            return PolicyDecision.requireApproval("Sensitive tool requires explicit user approval");
        }
        if (grantedAutonomy.ordinal() < definition.minimumAutonomy().ordinal()) {
            return PolicyDecision.deny("Granted autonomy is below the tool minimum");
        }
        return PolicyDecision.allow("Approved tool execution");
    }

    private boolean hasRequiredArguments(ToolDefinition definition, Map<String, Object> arguments) {
        return definition.requiredArguments().stream()
                .allMatch(argument -> arguments.containsKey(argument)
                        && arguments.get(argument) != null
                        && (!(arguments.get(argument) instanceof String value) || !value.isBlank()));
    }
}
