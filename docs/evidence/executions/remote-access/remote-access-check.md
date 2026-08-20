# MX remote-access verification

Generated: 2026-08-20 11:49:13 -03:00

| Check | Status | Details |
|---|---|---|
| tailscale-connected | PASS | Connected state checked without storing account tokens. |
| serve-tailnet-only | PASS | Serve must report tailnet only; Funnel must remain disabled. |
| serve-mx-proxy | PASS | Serve target must be the local MX web port 8082. |
| mx-https | PASS | HTTP 200; content type text/html. |
| duckdns-resolves | PASS | The DuckDNS record resolves; exact public address is intentionally omitted. |

Security note: no tokens, passwords or exact public IP addresses are stored in this evidence.
