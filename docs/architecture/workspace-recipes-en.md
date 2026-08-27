# Governed workspace recipes

## Delivered capabilities

MX now exposes four **named, approvable** workspace operations. They do not accept free-form commands, URLs, ports, or paths from the model.

| Tool | Allowed input | Effect after approval | Enforced limits |
|---|---|---|---|
| `workspace.initialize_static_project` | `project`: lowercase slug up to 48 characters | Creates minimal HTML, CSS, and README in `D:\MX\workspaces\<project>` | Valid name; create only when absent; no symbolic links |
| `workspace.preview_static` | `project`: created project slug | Queues a `static-http` recipe for the local executor | `127.0.0.1` only; ports `48000–48099`; requires `index.html`; Core does not start processes |
| `workspace.validate_static` | `project`: existing static project slug | Queues the `static-validate` recipe | One `static-html-v1` profile; declared HTML; up to 500 files, 5 MiB, and 20 s; result plus `index.html` hash |
| `workspace.build_static` | `project`: existing static project slug | Queues the `static-build` recipe in staging | Same profile and limits; copies only to `.mx/builds/<requestId>` with SHA-256 manifest; no publishing |

The development skill can only **propose** these operations. The user must approve the matching run with a valid nonce before the tool executes and writes the preview queue.

## Local preview executor

`scripts/mx_evolution_runner.py` now has three explicit workspace operations:

```text
python scripts/mx_evolution_runner.py --workspace-preview-once
python scripts/mx_evolution_runner.py --stop-workspace-preview <request-id>
python scripts/mx_evolution_runner.py --workspace-recipe-once
```

The executor accepts only the JSON recipe persisted by MX, revalidates slug, root, and absence of symbolic links, and starts Python's standard HTTP server with the host fixed to `127.0.0.1`. It selects a free port only from the reserved range, records PID, URL, time, and log under `.mx/preview-requests/`, and can stop that process using the request identifier.

> Preview is deliberately local. It is not published to the internet, exposed on the LAN, or allowed to launch npm, Docker, or arbitrary project code.

`static-validate` and `static-build` do not call a shell, Node, npm, Docker, or a model-selected binary. They revalidate declared HTML, reject symbolic links, limit files/bytes/time, and, for build, copy only the validated project into traceable staging. The local operator must explicitly run the recipe after run approval; no recurring mode is started by the tool.

## Evidence

`WorkspaceStaticProjectToolTest` covers valid creation, escape-attempt rejection, and preview queuing. `scripts/test_mx_evolution_runner.py` covers invalid JSON, traversal, remote host, altered port range, preview start/stop, unknown recipe, and static validation/build in staging. The full Core suite ran after implementation.

## Controlled next iteration

Additional profiles must be added individually with a contract, test, allowlist, resource limit, and approval. The model will not be able to pass shell strings, select binaries, define public ports, access paths outside the workspace, publish changes, or launch recurring jobs by itself.
