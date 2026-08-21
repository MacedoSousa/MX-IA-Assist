# Availability and Backup Operations

## Incident record — 21 August 2026

The MX services became unavailable during the local backup window because the previous procedure deliberately executed `docker-compose.exe stop` before copying the persistent data directory. This was an administrative outage, not an application crash: Docker reports no out-of-memory termination, no restart loop, and the Windows event log contains no Kernel-Power (41) or unexpected-shutdown (6008) event during the observation window. After `docker-compose.exe up -d --no-build`, the Core, Web UI and Forge endpoints returned HTTP 200 and the Core healthcheck returned `UP`.

The investigation also found that recreating containers removes their prior stdout logs. Although the persisted data was preserved, this made the evidence from the preceding container lifecycle incomplete. Persistent Core log files and independent healthchecks address that blind spot.

## Operational safeguards now included

| Area | Safeguard | Expected effect |
|---|---|---|
| Recovery | `restart: unless-stopped` retained and graceful shutdown periods added | Containers recover after Docker or host restart and receive time to close work safely. |
| Startup dependency | Ollama healthcheck and healthy dependency for MX Core | The Core no longer starts its warmup flow before Ollama accepts requests. |
| Visibility | Healthchecks for DSH and the Web UI | Docker reports whether each user-facing runtime is reachable rather than merely running. |
| Evidence | Rolling Core log at `/app/logs/mx-core.log` | Lifecycle and error evidence survives container recreation, with a 500 MB retention ceiling. |
| Backup | `infrastructure/docker/scripts/backup-mx.ps1` | Produces an online logical database dump plus memory and configuration archives without stopping MX. |

## Recommended operating procedure

Run the online backup script from PowerShell whenever the MX containers are running:

```powershell
Set-Location D:\MX
.\infrastructure\docker\scripts\backup-mx.ps1
```

The script writes a timestamped folder under `D:\MX\backups`, creates a PostgreSQL custom dump, archives attachments/knowledge/evolution, archives the Docker runtime configuration, and emits SHA-256 sums. Backup folders remain excluded by `.gitignore` and must not be committed.

For a future physical snapshot of all models and Docker-compatible runtime files, copy `D:\MX\data` to external storage while the system is in a scheduled maintenance window. Caches such as `open-webui/cache` and runtime profiles under `dsh/profiles` can be recreated; PostgreSQL, attachments, knowledge, evolution and the configuration archive are the authoritative recovery set.

## Next improvements by priority

1. Add an external uptime probe for the public Tailscale URL and notify the operator when the Web UI or health endpoint fails.
2. Schedule the online backup script daily and copy a rotating encrypted backup to a second physical device or private cloud location.
3. Add structured request IDs and dashboard metrics for latency, queue length, Ollama load time and image-generation failures.
4. Add a lightweight maintenance banner in the Web UI for operations that require a planned restart.
