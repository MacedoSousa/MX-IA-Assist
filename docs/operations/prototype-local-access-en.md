# MX prototype local access

## Usage rule

The entire MX environment is a **local prototype**. No port, container, script, or service is considered production at this stage. Technical ports are not the main user interface.

| Address | Role | How to use it |
|---|---|---|
| `http://127.0.0.1:8082` | **MX visual interface** | Recommended entry point for product testing. |
| `http://127.0.0.1:18080/actuator/health` | Native shadow Core healthcheck | Technical diagnostic; should return `200`/`UP`. |
| `http://127.0.0.1:18080/` | Native shadow Core root | Not a UI; a secured `403`/Whitelabel response is expected. |
| `http://127.0.0.1:8080/` | Docker reference Core | Also a technical API, not a product page. |
| `http://127.0.0.1:3000` | Auxiliary Open WebUI | Separate experimentation interface; it does not replace the MX UI. |
| `http://127.0.0.1:3080` | DSH | Development service restricted to loopback. |

> Open MX at `http://127.0.0.1:8082`. Do not open the `18080` API root expecting a login or chat screen.

## State and boundaries

The native shadow Core at `18080` is the validation environment connected to native PostgreSQL and Ollama. The API has no public root route and remains authenticated, so a browser shows a default error page when `/` is requested. This is not an outage when `/actuator/health` returns `UP`.

The Docker environment is preserved only as a prototype reference and rollback path. It must not be described as production or removed before the remaining validations are complete. Future centralization will use a single local launcher/panel with healthchecks and reversible actions, without launching arbitrary commands or publishing content.

## Prototype central control

`scripts/mx-prototype-control.ps1` centralizes observation and explicitly permitted reversible actions. Its default mode is read-only and reports the state of the UI, shadow Core, Docker reference Core, Ollama, PostgreSQL, Forge, and DSH as JSON.

```powershell
cd D:\MX
.\scripts\mx-prototype-control.ps1
.\scripts\mx-prototype-control.ps1 -Action OpenMx
.\scripts\mx-prototype-control.ps1 -Action StartShadow
.\scripts\mx-prototype-control.ps1 -Action StopShadow
```

`OpenMx` opens only `http://127.0.0.1:8082`. `StartShadow` and `StopShadow` operate exclusively on the process listening on port `18080`; the former does not restart an already healthy Core. None of these actions starts, stops, rebuilds, or removes Docker, modifies PostgreSQL, accesses secrets, changes memory/attachments/workspaces, or publishes content.

## UI login correction

The UI at `8082` is served by Nginx. The web client now forces relative `/api` calls through the same-host proxy to Core, avoiding a build-time `localhost:8080` URL that fails when access comes from a different origin, device, or browser. The Dockerfile fallback was removed as well.

Authentication failures now return `401` with a controlled JSON body (`MX_INVALID_CREDENTIALS` and the generic `Credenciais inválidas.` message). This prevents an internal `500`, does not reveal whether a user exists or is inactive, and lets the client display useful guidance without exposing internal detail. Core and UI were rebuilt without recreating volumes or data services; a deliberately absent account confirmed `401` and the safe message through `8082`.

When reproduced in a browser, the real attempt still reached the proxy as `POST /api/auth/login`, but received `403`. The cause was `application-dev.yml`: it overrides the default CORS allowlist and did not include `http://127.0.0.1:8082`. Because Nginx forwards the `Origin` header to Core, the CORS filter rejected it. The exact loopback origin was added with no network wildcard. After the isolated Core rebuild, preflight returned `Access-Control-Allow-Origin: http://127.0.0.1:8082` and the healthcheck remained `200`.

Core and Docker PostgreSQL health, active-account presence, and Argon2 compatibility of the expected local account were also checked without exposing identifiers, hashes, or passwords. No database, schema, or permission block was detected in the Docker route; confirmation of a real session still requires an attempt in the updated UI.
