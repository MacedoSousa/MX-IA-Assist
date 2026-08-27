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
| Document | PDF generation with Windows Arial font and attachment storage | Passed |
| Prerequisites | Verifier detects Ollama in LocalAppData, `pg_isready.exe`, and PostgreSQL `15432` | Passed |

## Recorded implementation

`initialize-mx-local-db.ps1` keeps the secret only in a Git-ignored local file and grants the runtime least-privilege access: schema usage and creation in `public`, data operations on existing tables, sequence usage, and default grants for future objects created by the database administrator. The application does not use a superuser account.

`start-ollama-local.ps1`, `start-mx-local.ps1`, `run-backend.bat`, `verify-mx-local.ps1`, `local/start-mx-core-shadow.ps1`, and `verify-mx-native-shadow.ps1` now explicitly support the native path. Native defaults use `11435` and PostgreSQL `15432`, while retaining explicit override points for rollback. The main launcher enables image generation only when `-WithImageGeneration` is supplied.

> No password, token, database dump, personal attachment, or persistent data directory was committed during this validation.

## Remaining cutover boundaries

Docker remains the production and rollback route until the following items are complete. Forge is still a transitional container component at port `7860`, so image generation is not yet Docker-free. FFmpeg and a local Whisper transcription tool were not found on the Windows PATH and must be installed and tested before promoting video and audio features.

Persistent startup for Core/Ollama/Expo, the native PostgreSQL network exposure policy, the direct-runtime Redis decision, Expo testing over LAN/Tailscale, and the documented cutover-and-return plan also remain open. DSH stays isolated at `127.0.0.1:3080` and was not changed by this work.

## Safe reproduction

1. Keep the existing containers running while validation is in progress.
2. Check prerequisites with `scripts/verify-mx-local.ps1 -OllamaUrl http://127.0.0.1:11435 -PostgresPort 15432`.
3. Validate the isolated Core with `scripts/local/start-mx-core-shadow.ps1 -WithImageGeneration` and query `http://127.0.0.1:18080/actuator/health`.
4. Run `scripts/verify-mx-native-shadow.ps1` with credentials supplied as local parameters to exercise login, conversation, PDF, attachment, and image paths. Do not store passwords in files or shared command history.

Promotion to the production port must wait until every boundary in this section has acceptance evidence.
