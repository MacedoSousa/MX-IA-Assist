# Windows native runtime — shadow-mode validation

**Date:** August 27, 2026  
**Scope:** parallel validation of MX Core running directly on Windows, without stopping or rebuilding Docker containers.

## Outcome

The native MX Core started successfully on port `18080` after PostgreSQL restoration and after granting the application account `mx` the minimum schema, table, and sequence privileges. The health endpoint returned `UP`; the log confirms `jdbc:postgresql://127.0.0.1:15432/mx`; and the shadow launcher explicitly selects the native Ollama endpoint at `http://127.0.0.1:11435`.

| Validated flow | Evidence | Result |
|---|---|---|
| Restored database | PostgreSQL 16, parallel instance `15432`, authenticated `mx` account | Passed |
| Migrations | Flyway completed Core startup after schema/object grants | Passed |
| Core health | `GET /actuator/health` on port `18080` | `UP` |
| Conversation | Authenticated login and conversation response through native Core | Passed |
| Attachments | Upload and processing of a real validation PDF attached to a conversation | Passed |
| Image | Authenticated generation through native Core using temporary Forge at `127.0.0.1:7860` | Passed |
| Video | Short MP4 clip generated with a Forge keyframe and native FFmpeg | Passed |
| Document | PDF generation with Windows Arial font and attachment storage | Passed |
| Prerequisites | Verifier detects Ollama in LocalAppData, `pg_isready.exe`, and PostgreSQL `15432` | Passed |
| Governed workspace | Static HTML bootstrap and preview request only after approval; temporary server restricted to `127.0.0.1:48000–48099` | Passed through tests and controlled proof |
| Preview runner | Disposable proof responded on `127.0.0.1:48000` and was stopped by its request identifier | Passed |
| Windows Core regression | Maven suite completed using a clean temporary output after synchronizing video fixtures and audit expectations | Passed |

## Recorded implementation

`initialize-mx-local-db.ps1` keeps the secret only in a Git-ignored local file and grants the runtime least-privilege access: schema usage and creation in `public`, data operations on existing tables, sequence usage, and default grants for future objects created by the database administrator. The application does not use a superuser account.

`start-ollama-local.ps1`, `start-mx-local.ps1`, `run-backend.bat`, `verify-mx-local.ps1`, `local/start-mx-core-shadow.ps1`, and `verify-mx-native-shadow.ps1` now explicitly support the native path. Native defaults use `11435` and PostgreSQL `15432`, while retaining explicit override points for rollback. The main launcher enables image generation only when `-WithImageGeneration` is supplied.

> No password, token, database dump, personal attachment, or persistent data directory was committed during this validation.

### Governed workspace and preview evidence

`workspace.initialize_static_project` and `workspace.preview_static` are registered `WRITE` tools with a restricted slug, isolated workspace, no symbolic links, and a maximum autonomy of `EXECUTE_WITH_APPROVAL`. `DevelopmentSkill` can only propose the tools; `ToolExecutor` persists arguments as JSON and the approval use case re-executes only the originally registered tool. The host-side runner accepts only the declarative `static-http` recipe, revalidates the loopback host and port range, and accepts no arbitrary commands, Docker, npm, or publishing.

Python tests exercise invalid JSON, traversal, a remote host, an altered port range, and the start/stop lifecycle. The Windows Core suite was also run in a temporary directory because a legacy ACL in `D:\MX\core-service\target` prevents a non-elevated user from cleaning some generated files. The fix did not alter that ACL, source, or data: the shadow launcher already uses an independent temporary directory, and the tests used the same isolated-output approach.

The authenticated UI/API smoke completed through a temporary session: `workspace.initialize_static_project` and `workspace.preview_static` were proposed by `DevelopmentSkill`, approved with their nonces, and completed with `COMPLETED` status. The runner served the proof project only on loopback, confirmed HTTP `200`, and stopped it explicitly. Post-run cleanup confirmed the absence of the credential, proof project, associated state, and preview listener.

During the test, PostgreSQL persistence revealed that the approval run reused the conversation run's `correlationId`, violating the uniqueness constraint. `ToolExecutor` was corrected to generate an independent correlation for each approval run; the automated regression and final smoke validated the behavior. Credentials and tokens remained in memory only and were removed after completion.

## Remaining cutover boundaries

Docker remains the **prototype** reference and rollback route until the following items are complete. Forge is still a transitional container component at port `7860`, so image generation is not yet Docker-free. FFmpeg 9 was installed through the Windows package manager and the native video pipeline generated a short clip successfully. Local Whisper was installed for the launcher, but still needs validation with real speech before any broader prototype use.

Persistent startup for Core/Expo, the native PostgreSQL network exposure policy, the direct-runtime Redis decision, Expo testing over LAN/Tailscale, and the documented cutover-and-return plan also remain open. Native Ollama now has a reversible logon-start task. DSH stays isolated at `127.0.0.1:3080` and was not changed by this work.

### Audio acceleration note

Windows recognizes the NVIDIA GeForce RTX 2060 SUPER, but `nvidia-smi` returned `Failed to initialize NVML: Unknown Error`. The versioned `verify-whisper-runtime.py` check reported PyTorch `2.13.0+cpu` with CUDA unavailable. Transcription therefore remains available through the CPU fallback, and no speculative CUDA installation was attempted. NVIDIA driver recovery or update, followed by NVML verification and an official compatible CUDA PyTorch installation, is required before Whisper GPU acceleration can be enabled.

## Safe reproduction

1. Keep the existing containers running while validation is in progress.
2. Check prerequisites with `scripts/verify-mx-local.ps1 -OllamaUrl http://127.0.0.1:11435 -PostgresPort 15432`.
3. Validate the isolated Core with `scripts/local/start-mx-core-shadow.ps1 -WithImageGeneration` and query `http://127.0.0.1:18080/actuator/health`.
4. Run `scripts/verify-mx-native-shadow.ps1` with credentials supplied as local parameters to exercise login, conversation, PDF, attachment, and image paths. Do not store passwords in files or shared command history.

Promotion to the production port must wait until every boundary in this section has acceptance evidence.
