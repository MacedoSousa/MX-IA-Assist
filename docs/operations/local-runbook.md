# Runbook de operação local

## Objetivo

Este runbook descreve como iniciar, verificar, diagnosticar e parar o MX em uma máquina Windows de desenvolvimento. O ambiente é local e não deve ser exposto à internet sem uma camada adicional de autenticação, TLS, firewall e revisão de threat model.

## Pré-requisitos

| Dependência | Uso |
|---|---|
| Java 21 | Compilar e executar o backend. |
| Maven ou Maven Wrapper | Build e testes Java. |
| Docker Desktop + Compose v2 | PostgreSQL, Redis, Ollama, Open WebUI e MX Core. |
| Node.js + npm | Cliente Expo e exportação web. |
| Ollama | Modelo local conversacional. |
| Git | Versionamento e revisão. |

Confirme versões antes de investigar a aplicação:

```powershell
java -version
docker --version
docker compose version
node --version
npm --version
git --version
ollama --version
```

## Configuração

A configuração de desenvolvimento fica em `core-service/src/main/resources/application-dev.yml` e no Compose. Segredos devem ser fornecidos pelo ambiente ou por arquivo local ignorado. O repositório contém apenas `.env.example` com placeholders.

O cliente Expo usa:

```powershell
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
```

Dentro do Compose, o backend usa o hostname do serviço Ollama definido no arquivo de composição. No cliente executado no host, `localhost` deve apontar para a porta publicada pelo backend.

## Iniciar

Para iniciar as dependências e o backend containerizado:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose up -d --build
```

Verifique os serviços:

```powershell
docker compose ps
docker compose logs --tail=100 mx-core
```

Para desenvolvimento iterativo do backend, é possível iniciar PostgreSQL, Redis e Ollama pelo Compose e executar o Spring Boot diretamente em `core-service`:

```powershell
cd D:\MX\core-service
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

O cliente universal pode ser iniciado separadamente:

```powershell
cd D:\MX\clients\mx-app
npm install
npm run start
```

## Health e smoke test

O Actuator deve ser usado para verificar a aplicação e suas dependências. A rota exata exposta depende da configuração do perfil, portanto confirme em `application.properties` e nas configurações do Actuator antes de automatizar um probe.

Depois de autenticar, faça um smoke test do contrato atual:

```powershell
# Login: use o payload definido pelo AuthController
curl.exe -i -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"usuario@example.com","password":"senha-local"}'

# Conversa síncrona: substitua TOKEN pelo access token retornado
curl.exe -i -X POST http://localhost:8080/api/v1/conversations/messages `
  -H "Authorization: Bearer TOKEN" `
  -H "Content-Type: application/json" `
  -d '{"prompt":"Responda com uma frase"}'
```

Não coloque tokens em histórico de terminal compartilhado. Em diagnóstico, redija o valor antes de anexar saída.

## Diagnóstico por sintoma

| Sintoma | Verificações iniciais | Ação segura |
|---|---|---|
| MX Core não inicia | `docker compose ps`, logs do container, Java e configuração | Corrigir causa; não apagar volumes automaticamente. |
| PostgreSQL recusando conexão | Health do container, porta 5432 e credenciais do perfil | Confirmar `.env` local e migrations; preservar `data/postgres`. |
| Ollama indisponível | `ollama list`, logs do serviço, URL configurada e modelo | Iniciar modelo correto; não marcar run como concluído manualmente. |
| Cliente recebe `401` | Expiração, sessão revogada e refresh | Limpar sessão somente após falha do refresh; não copiar tokens para tickets. |
| SSE interrompido | `runId`, `correlationId`, logs redigidos e estado do run | Consultar `GET /api/v1/runs/{runId}`; não reenviar tool automaticamente. |
| Tool rejeitada | Nome, policy outcome, workspace e motivo | Corrigir configuração ou solicitar aprovação explícita; não contornar a policy. |
| Porta ocupada | `Get-NetTCPConnection -LocalPort <porta>` | Parar somente o processo/serviço identificado ou alterar a porta documentada. |

## Logs e observabilidade

Use logs do serviço específico, limitando a quantidade:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose logs --tail=200 mx-core
docker compose logs --tail=100 ollama
```

Correlacione uma ocorrência por `runId` e `correlationId`. Não inclua prompt completo, resposta completa, access token, refresh token, senha, chave de API ou conteúdo privado em logs operacionais. Falhas de telemetria não devem interromper a execução principal.

## Parar e preservar dados

Para parar os containers sem destruir volumes:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose stop
```

Para remover containers mantendo os diretórios `data/`:

```powershell
docker compose down
```

Não use `docker compose down -v`, `Remove-Item data`, comandos de prune ou scripts destrutivos como primeira tentativa de diagnóstico. A exclusão de dados exige backup validado e uma decisão explícita.

## Backup e restauração — estado planejado

O backup local deve incluir banco e configuração não secreta, com manifesto e checksum, sem copiar segredos em texto puro. A restauração deve ocorrer em diretório temporário, validar checksum e executar um smoke test antes de substituir o ambiente ativo.

Enquanto o fluxo automatizado não estiver implementado, preserve ao menos uma cópia segura dos diretórios de dados e documente a versão do Compose, migrations e modelos utilizados. O item MX-112 permanece aberto no backlog.

## Build reprodutível

O ambiente usado para as validações do backend separa build e cache Maven da árvore do projeto:

```powershell
cd D:\MX\core-service
.\mvnw.cmd -Dmx.build.directory=C:\Windows\Temp\mx-target `
  -Dmaven.repo.local=C:\Windows\Temp\mx-m2 test
```

O resultado de `target/`, a distribuição Maven local, arquivos zip, bundles Expo, logs e caches não devem ser adicionados ao Git.
