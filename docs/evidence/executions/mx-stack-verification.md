# MX Stack Verification

Generated: 2026-08-20T10:54:33.0538429-03:00

| Probe | URL | HTTP | Result |
|---|---|---:|---|
| MX Core health | ``http://localhost:8080/actuator/health`` | 200 | PASS |
| MX Web health | ``http://localhost:8082/health`` | 200 | PASS |
| Ollama models | ``http://localhost:11434/api/tags`` | 200 | PASS |
| Open WebUI health | ``http://localhost:3000/health`` | 200 | PASS |

## MX containers

```text
mx-core|mx-core:latest|Up About a minute (healthy)|0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp
mx-redis|redis:7|Up 8 minutes (healthy)|0.0.0.0:6379->6379/tcp, [::]:6379->6379/tcp
mx-web|mx-web:latest|Up About an hour|0.0.0.0:8082->80/tcp, [::]:8082->80/tcp
mx-postgres|postgres:16|Up 2 hours (healthy)|0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp
mx-ollama|ollama/ollama:latest|Up 2 hours|0.0.0.0:11434->11434/tcp, [::]:11434->11434/tcp
mx-open-webui|ghcr.io/open-webui/open-webui:main|Up 2 hours (healthy)|0.0.0.0:3000->8080/tcp, [::]:3000->8080/tcp
```

## Preserved containers

```text
medsync-web-1|medsync-web|Up About an hour|0.0.0.0:8081->80/tcp, [::]:8081->80/tcp
medsync-api-1|medsync-api|Up 2 hours|0.0.0.0:3001->3000/tcp, [::]:3001->3000/tcp
medsync-mysql-1|mysql:8.4|Up 2 hours (healthy)|3306/tcp
deceasedcraft-server|deceasedcraft_docker_ready-minecraft|Up 14 hours|0.0.0.0:25565->25565/tcp, [::]:25565->25565/tcp
core-service-spring-init-1|curlimages/curl|Exited (0) 2 weeks ago|
minecraft|itzg/minecraft-server:latest|Exited (137) 12 days ago|
ts3-server|teamspeak:latest|Exited (0) 2 weeks ago|
carpg-server|eclipse-temurin:17-jdk|Exited (137) 7 weeks ago|
```

## Always-on task

```text

TaskName                 LastRunTime         LastTaskResult NumberOfMissedRuns
--------                 -----------         -------------- ------------------
MX-AI-Assistant-AlwaysOn 20/08/2026 10:45:54              0                  0
```

Overall result: **PASS**

