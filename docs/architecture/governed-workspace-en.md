# Governed workspace and post-approval execution

## Operating principle

The model never receives a free-form terminal, arbitrary absolute path, credentials, publishing authority, or permission to start processes directly. Creation and change requests go through MX Core and registered tools; host commands remain reserved for local executors of declarative recipes.

| Step | Responsibility | Guarantee |
|---|---|---|
| Proposal | A skill emits a structured call to an allowed tool | It does not perform the write |
| Policy | `PolicyEngine` validates tool name, arguments, autonomy, and skill allowlist | Sensitive work requires approval |
| Pending state | `ToolExecutor` records tool, JSON arguments, hashed nonce, and expiry | The request cannot be altered in the panel |
| Approval | The authenticated user supplies the nonce for their own run | Ownership, one-time use, and expiry are checked |
| Resume | `ApprovedToolExecutionUseCase` deserializes only persisted arguments and calls the registered tool | No shell, command, or new arguments are accepted |
| Evidence | The run is verified and finished with serialized tool output | Audit trail remains in the runs endpoint |

## Implemented correction

Previously, approving a run only moved it from `AWAITING_APPROVAL` to `EXECUTING`; the pending action was never resumed. MX now resumes a tool only after valid approval and persists the run as `COMPLETED` or `FAILED`. Pending arguments moved from an ambiguous text representation to structured JSON before storage.

The first exercised case is `workspace.write_file`. It confines writes to the workspace root, blocks absolute paths, traversal, NUL, and symbolic links, caps size, and uses atomic replacement. The tool still requires `EXECUTE_WITH_APPROVAL`.

> Approval does not authorize a new intent. It consumes only the name and arguments registered before consent; invalid arguments, an unknown tool, or execution failure end in an audited error state.

## Coverage and next steps

`ApprovedToolExecutionUseCaseTest` demonstrates that a controlled tool does not run before approval and completes only after valid nonce and consent. `ToolExecutorTest`, `WorkspaceWriteToolPolicyTest`, `DevelopmentSkillTest`, and controller tests preserve the existing boundaries.

There is still no general-purpose terminal, project-script execution, publishing, public preview, or permission to traverse outside `D:\MX\workspaces`. The next increment should introduce named recipes for bootstrapping, static validation, and local preview at `127.0.0.1`, with host-side queues, temporary ports, logs, and explicit termination. Any recipe that executes project code must continue to require an allowed profile, staging tests, and human approval.
