# Official Sources Used / Fontes Oficiais Utilizadas

This file records the external sources used to ground the portfolio documentation. / Este arquivo registra as fontes externas utilizadas para fundamentar a documentação do portfólio.

## [1] Docker Compose services

URL: https://docs.docker.com/reference/compose-file/services/

Docker defines a Compose service as an abstract computing resource backed by containers. A service can define an image, build configuration, runtime arguments, ports, environment and restart-related behavior. The `container_name` attribute allows an explicit container name, which is why MX uses stable names such as `mx-core`, `mx-web`, `mx-postgres`, `mx-redis` and `mx-ollama`.

## [2] Docker Compose volumes

URL: https://docs.docker.com/reference/compose-file/volumes/

Docker documents volumes as persistent data stores implemented by the container engine. Existing volumes are reused by Compose and are not recreated unless they are manually deleted. The MX stack uses host bind mounts under `D:\MX\data` for PostgreSQL, Redis, Ollama and Open WebUI, preserving application state independently of container recreation.

## [3] Spring Boot Actuator endpoints

URL: https://docs.spring.io/spring-boot/reference/actuator/endpoints.html

Spring Boot Actuator exposes operational endpoints such as `/actuator/health` and `/actuator/metrics`. The health endpoint provides application health information, while exposure must be intentionally configured because actuator endpoints may contain sensitive information. MX exposes health, info and metrics only in the local development profile and keeps the environment local.

## [4] Ollama API

URL: https://docs.ollama.com/api/introduction

Ollama serves its local API by default at `http://localhost:11434/api`. The MX Core calls the local Ollama service and uses the locally available `qwen3:8b` model. The integration keeps prompts and model execution on the user's machine.

## [5] Expo Web deployment

URL: https://docs.expo.dev/deploy/web/

Expo documents the web deployment flow for exporting an Expo application to a browser-compatible bundle. MX uses an Expo Web export in a multi-stage Docker build and serves the resulting static application through Nginx on port 8082.

## Citation policy / Política de citação

Claims about the actual MX environment are supported by repository evidence under `docs/evidence/` and by reproducible scripts under `scripts/`. External platform behavior is cited to the official sources above.
