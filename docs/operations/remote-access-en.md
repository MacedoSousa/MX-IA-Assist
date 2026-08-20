# Secure MX Remote Access

**Status:** configured and validated on August 20, 2026.

## Applied result

The Windows MX computer was connected to the user's tailnet with the internal hostname `mx-ai.taila61bd3.ts.net`. Tailscale Serve was enabled for the tailnet only and proxies HTTPS traffic to the local MX web client:

```text
https://mx-ai.taila61bd3.ts.net/
    -> http://127.0.0.1:8082
```

The endpoint returned `HTTP 200` on the Windows host and delivered the Expo web client served by Nginx. Tailscale reported `tailnet only`, so this endpoint is not publicly available to the entire Internet.

The requested free hostname `mx-ai.duckdns.org` currently resolves to a public IPv4 record. It remains available as a DNS reference, but it is **not used as the public MX endpoint**, because a DuckDNS record alone does not provide a private tunnel, an additional identity gate, or protection from direct port exposure. The secure remote URL is therefore the Tailscale HTTPS hostname.

## Mobile access

Install Tailscale on Android or iOS, sign in with the same account used on the Windows computer, and connect the device. Then open this address in the mobile browser:

```text
https://mx-ai.taila61bd3.ts.net/
```

The phone must be authorized in the same tailnet. If it appears as pending in the Tailscale admin panel, approve it before the first connection.

## Security model

MX does not directly publish ports 8080, 8081, 8082, 3000, 5432, 6379, or 11434. PostgreSQL, Redis, Ollama, Open WebUI, and game servers remain outside the remote endpoint. Tailscale Serve provides HTTPS inside the tailnet and forwards traffic to `127.0.0.1:8082`; no router port forwarding is required in this design [1] [2].

> Tailscale documents Serve as a way to share content from a node to its tailnet over HTTPS, while Funnel is a separate feature for exposing a service to the public Internet [1].

Funnel was intentionally left disabled. This prevents an accidental configuration change from turning MX into a public unauthenticated service. MX's own authentication remains required, while tailnet membership provides a second access-control layer.

## Verification commands

Run on Windows:

```powershell
$ts = 'C:\Program Files\Tailscale\tailscale.exe'
& $ts status
& $ts serve status
Invoke-WebRequest https://mx-ai.taila61bd3.ts.net/ -UseBasicParsing
```

Expected Serve output:

```text
https://mx-ai.taila61bd3.ts.net (tailnet only)
|-- / proxy http://127.0.0.1:8082
```

The HTTPS request should return `HTTP 200`.

## DuckDNS and limitations

`mx-ai.duckdns.org` can remain available to track the residential public address if the user's DDNS updater is configured with a private token. However, pointing the hostname directly at the computer and opening a router port would change the threat model. That option was not enabled.

To use the DuckDNS hostname as a public MX URL while retaining a proxy layer, a tunnel service that accepts an external domain or a domain registered with a compatible provider would be required. With the currently available free options, the safest immediately usable design is the private Tailscale hostname.

## Operations and restart behavior

Tailscale is installed as a Windows service, and MX continues to be started by the existing Always-On launcher. Serve remains associated with the node configuration; its status should be verified after Tailscale upgrades or a Windows restart. No remote-access token should ever be committed to Git.

## References

[1]: https://tailscale.com/docs/reference/tailscale-cli/serve "Tailscale Serve CLI"
[2]: https://tailscale.com/docs/features/magicdns "Tailscale MagicDNS"
[3]: https://www.duckdns.org/ "Duck DNS"
