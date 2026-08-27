# Registration-free ephemeral local console

`scripts/mx-local-chat.ps1` provides a local CMD or PowerShell conversation **without creating an account or session**. It uses only native Ollama at `127.0.0.1:11435` and discards the interaction when the terminal closes.

```powershell
cd D:\MX

# Interactive conversation with MX's default model
.\scripts\mx-local-chat.ps1

# One-off question without history
.\scripts\mx-local-chat.ps1 -Prompt "Explain Clean Architecture in two sentences."

# Smaller local profile for faster terminal responses
.\scripts\mx-local-chat.ps1 -Model qwen3:8b -Prompt "Reply only: ready."
```

| Capability | Registration-free console | Authenticated MX interface |
|---|---|---|
| Local conversation | Yes, ephemeral | Yes, linked to the account |
| History and memory | No | Yes, through the existing flow |
| Documentary knowledge/RAG | No | Yes, with traceable citations |
| Attachments, media, and documents | No | Yes, when features are enabled |
| Files, workspace, and approvals | No | Yes, with human approval and allowlists |
| Internet and self-analysis | No | Only according to Core policy and configuration |

> The console is not an authentication shortcut and does not bypass MX protections. It does not call Core, read secrets, or access databases, files, memory, tools, Docker, or publishing.

Local validation confirmed a reply using `qwen3:8b`. `deepseek-r1:14b` remains MX's strong default model, but can show higher terminal latency depending on current hardware load and model state.
