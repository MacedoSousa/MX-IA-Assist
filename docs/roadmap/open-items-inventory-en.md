# MX prototype open-items inventory

**Consolidation date:** August 27, 2026. This document is the prioritization source for unfinished markers in `todo.md`, roadmap, architecture, operations, security, tests, and backlog. Purely historical markers do not automatically represent unfinished work.

## P0 — Verifiable usage path

| Item | Current evidence | Next deliverable and acceptance |
|---|---|---|
| UI login | The CORS block for `127.0.0.1:8082` was corrected; a real browser session remains to be confirmed. | Valid login opens a session, restores conversations, and permits logout without exposing a token. |
| Central topology | Docker UI (`8082`), Docker Core (`8080`), and native shadow Core (`18080`) coexist for prototyping. | One explicit UI topology, with launcher/status and documented rollback. |
| Central database | Docker and shadow Core use distinct PostgreSQL databases during migration. | Documented decision and migration only after verified backup/restore; no manual data-directory copy. |
| Real client | Expo has working flows but remains monolithic and incomplete as an interface validation target. | Login, chat, conversation, attachment, and approval flows tested in the client against the selected Core. |

## P1 — Local capabilities and operational security

| Item | Current evidence | Next deliverable and acceptance |
|---|---|---|
| Direct runtime | Ollama, PostgreSQL, and shadow Core are validated; Docker remains prototype rollback. | Reversible Core/Expo startup, healthchecks, and return plan without stopping Docker. |
| PostgreSQL/Redis network | Native PostgreSQL still needs bind review; the direct Redis decision remains open. | Documented minimum exposure, local validation, and no blind data-directory changes. |
| Audio/GPU | Whisper runs on CPU; `nvidia-smi`/NVML fails. | End-to-end transcription of authorized audio; CUDA only after driver/NVML health. |
| Image and video | PDF/FFmpeg are validated; Forge is temporary Docker. | Native Forge validation or formal transitional dependency with healthcheck and operational boundary. |
| Workspace | Static bootstrap, approval, loopback preview, and stop are validated. | Separate validation and build recipes with staging, timeout, allowlist, and evidence. |
| Smoke account | The credential was removed, but the temporary account has no governed administrative deletion. | Auditable deactivation/removal without ad-hoc SQL. |

## P2 — Product and quality evolution

| Item | Current evidence | Next deliverable and acceptance |
|---|---|---|
| RAG | Traceable citations and context limits exist; ranking is lexical. | Owner filter, embeddings/vector store, reranking, and evaluation while preserving citations. |
| Authorized skills | Authorized documentary knowledge is already imported and auditable. | Skills map for games/documents/images and evaluations, without automatic training or unauthorized sources. |
| Multiplatform Expo | Web/Android/iOS base exists. | LAN/Tailscale test on an authorized device, with explicit CORS and no embedded credential. |
| Atelier of Intelligence UI | The visual direction exists; routes and components still require modularization. | Modular app with actions genuinely connected to the API, accessibility, and no mock controls. |
| Mintlify | This remains versioned local documentation; no domain or credentials are assumed. | Local Mintlify PT-BR/EN structure; publication only after explicit configuration. |

## Documentation to update, not reimplement

Some documents still describe surpassed states: `docs/approvals-and-tools.md` and `docs/mx-core-skills-architecture.md` state that approval is not yet bridged to `ExecutionRun`, but post-nonce approval was implemented and validated; `docs/product-backlog.md` contains similar historical rows for runs and refresh; and `docs/security/threat-model.md` needs T-08 updated for the delivered nonce, expiration, and one-time use. These files must be corrected in the documentation workstream, without duplicating functionality.

## Execution order

The next increment must complete the real-login check and select the unified UI route. Then the order is: audio and native infrastructure; workspace recipes and RAG; modular Expo client and Mintlify documentation. Every increment will have a test, evidence, and explicit rollback; Docker, data, attachments, and backups remain preserved until migration criteria are met.
