# Governed workspace recipes

## Delivered capabilities

MX now exposes two **named, approvable** workspace operations. They do not accept free-form commands, URLs, ports, or paths from the model.

| Tool | Allowed input | Effect after approval | Enforced limits |
|---|---|---|---|
| `workspace.initialize_static_project` | `project`: lowercase slug up to 48 characters | Creates minimal HTML, CSS, and README in `D:\MX\workspaces\<project>` | Valid name; create only when absent; no symbolic links |
| `workspace.preview_static` | `project`: created project slug | Queues a `static-http` recipe for the local executor | `127.0.0.1` only; ports `48000–48099`; requires `index.html`; Core does not start processes |

The development skill can only **propose** these operations. The user must approve the matching run with a valid nonce before the tool executes and writes the preview queue.

## Local preview executor

`scripts/mx_evolution_runner.py` now has two explicit modes:

```text
python scripts/mx_evolution_runner.py --workspace-preview-once
python scripts/mx_evolution_runner.py --stop-workspace-preview <request-id>
```

The executor accepts only the JSON recipe persisted by MX, revalidates slug, root, and absence of symbolic links, and starts Python's standard HTTP server with the host fixed to `127.0.0.1`. It selects a free port only from the reserved range, records PID, URL, time, and log under `.mx/preview-requests/`, and can stop that process using the request identifier.

> Preview is deliberately local. It is not published to the internet, exposed on the LAN, or allowed to launch npm, Docker, or arbitrary project code.

## Evidence

`WorkspaceStaticProjectToolTest` covers valid creation, escape-attempt rejection, and preview queuing. The runner proof started a disposable static project on `127.0.0.1:48000`, confirmed its HTTP response, and stopped the process by `requestId`. The full Core suite ran after implementation.

## Controlled next iteration

New recipes must be added one at a time with a contract, test, allowlist, and approval: static validation, project build, and preview of explicitly supported profiles. The model will not be able to pass shell strings, select binaries, define public ports, access paths outside the workspace, publish changes, or launch recurring jobs by itself.
