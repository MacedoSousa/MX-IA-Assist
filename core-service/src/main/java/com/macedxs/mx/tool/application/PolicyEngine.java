package com.macedxs.mx.tool.application;

import com.macedxs.mx.agent.application.AutonomyLevel;

import java.util.Map;
import java.util.Set;

public class PolicyEngine {

    public PolicyDecision evaluate(
            ToolDefinition definition,
            ToolRequest request,
            AutonomyLevel grantedAutonomy,
            boolean approved
    ) {
        return evaluate(definition, request, grantedAutonomy, approved, null);
    }

    public PolicyDecision evaluate(
            ToolDefinition definition,
            ToolRequest request,
            AutonomyLevel grantedAutonomy,
            boolean approved,
            Set<String> allowedTools
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
        if (allowedTools != null && allowedTools.stream().noneMatch(tool -> tool.equalsIgnoreCase(request.toolName()))) {
            return PolicyDecision.deny("Tool is not allowed for the selected skill");
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

        if (definition.effect() == ToolEffect.SELF_MODIFICATION) {
            if (grantedAutonomy != AutonomyLevel.EXECUTE_AUTONOMOUSLY) {
                return PolicyDecision.deny("Self-modification requires the internal autonomous execution level");
            }
            if (grantedAutonomy.ordinal() < definition.minimumAutonomy().ordinal()) {
                return PolicyDecision.deny("Granted autonomy is below the tool minimum");
            }
            return PolicyDecision.allow("Allowed bounded self-modification job");
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
