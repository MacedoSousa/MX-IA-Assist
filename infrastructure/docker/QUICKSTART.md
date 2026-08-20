# Quickstart local do MX

## Requisitos

Instale Docker Desktop com Compose v2, Java 21, Node.js, Git e Ollama. Reserve memória suficiente para PostgreSQL, Redis, backend e modelo local. As portas usuais são 5432, 6379, 11434, 3000 e 8080; confirme conflitos antes de iniciar.

## Subir a infraestrutura

No Windows:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 mx-core
```

Em Linux/macOS:

```bash
cd /path/to/MX/infrastructure/docker/compose
docker compose up -d --build
docker compose ps
```

O status esperado é que os serviços necessários estejam `running` ou `healthy`, de acordo com os healthchecks do Compose. Open WebUI é opcional para inspecionar o Ollama e não é a interface oficial do MX.

## Validar autenticação e API

A autenticação atual usa `/api/auth`. O payload exato deve acompanhar os DTOs do `AuthController` e o contrato publicado. Exemplo genérico:

```powershell
curl.exe -i -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"usuario@example.com","password":"senha-local"}'
```

Guarde o access token somente em ambiente local protegido:

```powershell
$env:MX_ACCESS_TOKEN = "TOKEN_RETORNADO_NO_LOGIN"
```

Envie uma mensagem ao MX Core:

```powershell
curl.exe -i -X POST http://localhost:8080/api/v1/conversations/messages `
  -H "Authorization: Bearer $env:MX_ACCESS_TOKEN" `
  -H "Content-Type: application/json" `
  -d '{"prompt":"Responda com uma frase"}'
```

Para streaming:

```powershell
curl.exe -N -X POST http://localhost:8080/api/v1/conversations/messages/stream `
  -H "Authorization: Bearer $env:MX_ACCESS_TOKEN" `
  -H "Content-Type: application/json" `
  -H "Accept: text/event-stream" `
  -d '{"prompt":"Explique o que é Clean Architecture"}'
```

O retorno deve conter eventos `started`, `token`, `completed` ou `error`. Use o `runId` para consultar o estado persistido:

```powershell
curl.exe -i http://localhost:8080/api/v1/runs/RUN_ID `
  -H "Authorization: Bearer $env:MX_ACCESS_TOKEN"
```

## Executar o cliente Expo

```powershell
cd D:\MX\clients\mx-app
npm install
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
npm run web
```

Para validação estática:

```powershell
npm run typecheck
npx expo export --platform web
```

## Logs e parada

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose logs --tail=200 mx-core
docker compose logs --tail=100 postgres
docker compose logs --tail=100 ollama
docker compose stop
```

Use `docker compose down` somente quando quiser remover containers. Não use `down -v` como primeira tentativa, pois isso remove volumes. O diagnóstico completo está em [`docs/operations/local-runbook.md`](../../docs/operations/local-runbook.md).

## Banco e segredos

Não copie senhas reais para esta documentação, fixtures ou commits. Não execute `SELECT *` em dados pessoais para criar tickets. O banco local e modelos não são artefatos versionáveis; backup e restauração controlados ainda estão em evolução.
