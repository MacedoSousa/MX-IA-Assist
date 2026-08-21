# MX — Assistente pessoal local

> **Status:** fundação avançada executável localmente. O stack Docker, o cliente web, a integração de tools estruturadas, a aprovação humana, a operação Always-On e as evidências técnicas estão implementados e validados. O projeto continua evoluindo e não deve ser tratado como serviço de alta disponibilidade em produção.

O **MX** é um assistente pessoal local inspirado em um “Jarvis”, projetado para apoiar programação, organização pessoal e consultas gerais usando modelos executados localmente pelo Ollama. O usuário conversa somente com o **MX Core**. As skills especialistas, as tools e as políticas de autonomia são capacidades internas coordenadas pelo núcleo e não canais independentes de comunicação.

O projeto é estruturado com **Clean Architecture, TDD e SOLID**, priorizando isolamento por usuário, segurança por padrão, observabilidade e evolução incremental. O backend é um monólito modular em Java 21/Spring Boot; o cliente é universal e compartilhado entre web, Android e iOS por meio de Expo/React Native.

## Visão arquitetural

```text
┌─────────────────────────────────────────────────────────────┐
│ Cliente universal Expo                                      │
│ Web · Android · iOS                                         │
│ Login · chat · streaming · status do run · aprovação        │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS/HTTP local + JWT
┌──────────────────────────▼──────────────────────────────────┐
│ API v1 / MX Core                                             │
│ Autenticação · conversação · ExecutionRun · SSE             │
├─────────────────────────────────────────────────────────────┤
│ Orquestração interna                                         │
│ Roteamento de skill · autonomia · verificação · correlação   │
├──────────────────────────────┬──────────────────────────────┤
│ Skills especialistas          │ Policy Engine + ToolExecutor │
│ General · Development         │ allowlist · efeito · sandbox │
└──────────────┬───────────────┴──────────────┬───────────────┘
               │                              │
       PostgreSQL + Flyway              Ollama local
       usuários, sessões,               modelo conversacional
       mensagens e runs
```

A regra central é que dependências apontam para dentro: o domínio e os casos de uso não dependem de Spring, HTTP, JPA ou Ollama. Adapters de entrada e saída implementam portas explícitas, enquanto a composição Spring conecta as implementações no bootstrap.

## O que já está implementado

| Área | Estado atual | Evidência funcional |
|---|---|---|
| MX Core | Implementado | Ponto único de entrada, correlação, roteamento e coordenação de skills. |
| Autenticação | Implementado em nível de fundação | JWT com `sessionId`, sessão persistida, refresh rotativo com hash SHA-256 e logout revogável. |
| ExecutionRun | Implementado em nível de fundação | Lifecycle `RECEIVED → ROUTED → EXECUTING → VERIFYING → COMPLETED/FAILED`, com `AWAITING_APPROVAL` e `CANCELLED`. |
| Streaming | Implementado | SSE com eventos `started`, `token`, `completed` e `error`; parser NDJSON do Ollama. |
| Skills | Implementado | Skills `general`, `development` e `quality` atrás do MX Core, com contratos explícitos e roteamento interno. |
| Tools | Integradas com segurança | Parser estruturado `[MX_TOOL_CALL]`, allowlist, `ToolExecutor`, sandbox, bloqueio de traversal/symlink, escrita atômica e limite de bytes. |
| Aprovação | Integrada no caminho de produção | `REQUIRE_APPROVAL` cria `ExecutionRun` em `AWAITING_APPROVAL` com nonce hash, expiração, idempotência e propagação REST/SSE. |
| Cliente | Implementado em nível funcional | App Expo universal com login, logout, refresh automático, chat síncrono, streaming, histórico inicial, seleção de anexos e controles de mídia. |
| Histórico e memória | Implementados | Histórico paginado, memória recente limitada no prompt e perfil persistente de aprendizagem por usuário. |
| Aprendizagem adaptativa | Implementada em nível inicial | Taxonomia de domínios, tópicos detectados lexicalmente, nível/estilo configuráveis e contexto personalizado para a GeneralSkill. |
| Multimodalidade | Implementada com engines opcionais | Upload/download privado de anexos, imagens enviadas ao modelo visual configurável, transcrição Whisper opcional e geração Stable Diffusion opcional. |
| Desempenho Ollama | Implementado em nível configurável | `keep_alive`, `num_ctx`, `num_thread`, paralelismo controlado, cache persistente, cloud desabilitada por padrão e porta publicada somente em localhost. |
| Observabilidade | Implementada na fundação operacional | Actuator, healthchecks, métricas Micrometer, correlação, evidências de execução e runbook operacional. Dashboards históricos e alertas externos continuam como evolução. |

## Requisitos locais

O desenvolvimento principal ocorre em Windows com Docker Desktop, Compose, PowerShell e Git. O fluxo recomendado é Docker-only: PostgreSQL, Redis, Ollama, Open WebUI, MX Core e mx-web são construídos e executados pelo Compose. Java 21, Maven, Node.js, npm e Ollama instalados no host permanecem opcionais para desenvolvimento direto e depuração avançada.

Os modelos Ollama não são armazenados no Git. O arquivo `infrastructure/docker/env/.env.example` documenta somente valores de exemplo. Credenciais reais, bancos locais, logs, modelos, builds e distribuições de ferramentas devem permanecer fora do versionamento.

## Execução local

### Infraestrutura

A partir da raiz do projeto, o caminho recomendado é executar o launcher Docker-only:

```powershell
cd D:\MX
.\start.bat
```

O Compose atual inclui PostgreSQL, Redis, Ollama, Open WebUI, MX Core e mx-web. Todos os serviços MX usam `restart: unless-stopped`; PostgreSQL, Redis, Ollama e Open WebUI possuem dados persistentes, e Redis utiliza AOF. O launcher detecta o IP LAN, constrói o bundle Expo com a URL correta da API, configura CORS para a origem web da LAN, aguarda os healthchecks e informa os endereços finais. A interface web oficial fica em `http://localhost:8082` ou `http://<IP-LAN>:8082`; a API fica em `http://localhost:8080` ou `http://<IP-LAN>:8080`. A tarefa `MX-AI-Assistant-AlwaysOn` inicia o launcher automaticamente no Windows. A porta `MX_WEB_PORT` é configurável; 8082 é o padrão atual porque 8081 já estava ocupada por outro serviço local. As portas e mounts devem ser confirmados em `infrastructure/docker/compose/docker-compose.yml`. A pasta `frontend/` permanece legada.

### Backend

Para executar o backend diretamente no Windows, use o Maven Wrapper ou Maven instalado. A configuração de build usada nas validações evita misturar artefatos com a árvore do projeto:

```powershell
cd D:\MX\core-service
.\mvnw.cmd -Dmx.build.directory=C:\Windows\Temp\mx-target `
  -Dmaven.repo.local=C:\Windows\Temp\mx-m2 test
```

Se o Maven Wrapper não estiver disponível no ambiente, use `mvn` com os mesmos parâmetros. A aplicação utiliza o perfil `dev` quando configurada pelo Compose e depende de PostgreSQL e Ollama disponíveis.

### Cliente Expo universal

```powershell
cd D:\MX\clients\mx-app
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
npm install
npm run start
```

Comandos específicos do cliente:

```powershell
npm run web
npm run android
npm run ios
npm run typecheck
npx expo export --platform web
```

No web, a sessão usa `localStorage`; em Android e iOS, access token e refresh token usam armazenamento seguro nativo. O cliente não chama skills diretamente: todas as mensagens passam pelo MX Core.

## API v1

O contrato oficial está em [`docs/api/mx-v1.yaml`](docs/api/mx-v1.yaml). A API usa `Authorization: Bearer <JWT>` nos endpoints protegidos.

| Método | Rota | Finalidade |
|---|---|---|
| `POST` | `/api/auth/login` | Iniciar sessão e obter access/refresh token. |
| `POST` | `/api/auth/refresh` | Rotacionar o refresh token e obter novo access token. |
| `POST` | `/api/auth/logout` | Revogar a sessão atual. |
| `POST` | `/api/v1/conversations/messages` | Processar uma mensagem de forma síncrona. |
| `POST` | `/api/v1/conversations/messages/stream` | Processar uma mensagem com resposta incremental via SSE. |
| `GET` | `/api/v1/conversations/{conversationId}/messages` | Consultar histórico paginado e isolado por usuário. |
| `POST` | `/api/v1/attachments` | Armazenar um anexo privado com allowlist MIME, limite e checksum. |
| `GET` | `/api/v1/attachments/{attachmentId}` | Baixar um anexo pertencente ao usuário autenticado. |
| `POST` | `/api/v1/media/audio/{attachmentId}/transcription` | Transcrever áudio por engine local opcional. |
| `POST` | `/api/v1/media/images` | Gerar imagem por engine local opcional. |
| `GET/PUT` | `/api/v1/users/me/preferences` | Consultar e ajustar perfil adaptativo de aprendizagem. |
| `GET` | `/api/v1/runs/{runId}` | Consultar o run do usuário autenticado. |
| `POST` | `/api/v1/runs/{runId}/approve` | Aprovar um run pendente. |
| `POST` | `/api/v1/runs/{runId}/reject` | Rejeitar um run com motivo obrigatório. |

Exemplo de mensagem síncrona:

```bash
curl -X POST http://localhost:8080/api/v1/conversations/messages \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Explique como organizar este projeto","attachmentIds":[]}'
```

Para anexar um arquivo, faça primeiro `POST /api/v1/attachments` como `multipart/form-data` e envie os UUIDs retornados em `attachmentIds`. O cliente Expo automatiza esse fluxo no web, Android e iOS. A transcrição e a geração de imagem permanecem desligadas por padrão até que uma engine local seja configurada.

O endpoint de streaming retorna eventos SSE. O evento `started` apresenta `runId` e `correlationId`; eventos `token` carregam deltas textuais; `completed` informa o encerramento; `error` representa falha controlada do fluxo.

## Segurança e autonomia

A autenticação combina access token de curta duração, refresh token rotativo, sessão persistida e revogação. O filtro JWT consulta o estado da sessão e bloqueia tokens associados a sessões revogadas. Casos de uso de runs restringem consultas e decisões ao `userId` autenticado.

Tools não executam diretamente a partir de texto livre do modelo. Elas passam pelo parser estruturado, registry e **Policy Engine**, que avaliam efeito, allowlist, autonomia e contexto. A `WorkspaceWriteTool` limita o diretório ao workspace configurado, bloqueia path traversal e symlink, usa escrita atômica e possui limite configurável de bytes. Quando a política exige intervenção, o `ToolExecutor` cria um `ExecutionRun` em `AWAITING_APPROVAL`, com nonce, expiração e idempotência; o chat síncrono e o SSE propagam os metadados de aprovação pelo canal único do MX.

## Validações conhecidas

As validações executadas no ambiente de desenvolvimento foram:

| Validação | Resultado conhecido |
|---|---|
| Suíte TDD do backend | 96 testes, 0 falhas, 0 erros e 0 ignorados; testes de memória, preferências, anexos e payload multimodal incluídos. |
| TypeScript do cliente | `npm run typecheck` aprovado. |
| Exportação web Expo | `npx expo export --platform web` aprovado; bundle web de 446 kB gerado. |
| Compose | YAML válido, seis serviços preservados, Ollama publicado somente em `127.0.0.1:11434` e `OLLAMA_NO_CLOUD=1`. |
| Benchmark de aprendizagem | 161 chunks; carga 2,644 ms; mediana lexical 7,069 ms; P95 7,714 ms. |
| OpenAPI | 11 rotas e 15 schemas validados por `scripts/validate_openapi.py`. |
| E2E local | Registro, login, sessão persistida e chat síncrono foram validados anteriormente contra o Ollama local; a revalidação ao vivo depende do daemon Docker/containers ativos. |
| Always-On | Tarefa `MX-AI-Assistant-AlwaysOn` foi validada anteriormente; serviços MX mantêm `restart: unless-stopped`. |
|

As provas detalhadas e os comandos reproduzíveis estão em [`docs/portfolio/README.md`](docs/portfolio/README.md) e [`docs/evidence/validation-phase-8.md`](docs/evidence/validation-phase-8.md), incluindo inventário Docker, healthchecks, sumário TDD, benchmark do conhecimento, OpenAPI e limitações da validação. Execute novamente os comandos após qualquer alteração. O sucesso dessas verificações não equivale a alta disponibilidade de produção; backups restauráveis, observabilidade histórica e validação E2E com os containers ativos continuam como evoluções recomendadas.

## Estrutura do projeto

```text
D:\MX
├── core-service/                 # Backend Spring Boot e Clean Architecture
├── clients/mx-app/               # Cliente Expo universal web/Android/iOS
├── docs/                         # Contratos, arquitetura, segurança e operação
├── infrastructure/docker/        # Compose, ambiente de desenvolvimento e scripts
├── frontend/                     # Interface estática legada; não é o cliente oficial atual
├── data/                         # Dados locais; não versionar
├── models/                       # Modelos locais; não versionar
├── logs/                         # Logs locais; não versionar
├── README.md                     # Guia principal
├── IMPLEMENTATION_SUMMARY.md     # Histórico técnico P0–P3
├── CHANGELOG.md                  # Histórico resumido de evolução
└── CONTRIBUTING.md               # Fluxo de contribuição e validação
```

## Portfólio, evidências e próximos incrementos

A documentação profissional bilíngue está em [`docs/portfolio/README.md`](docs/portfolio/README.md), com versões em português e inglês, roteiro do vídeo, diagrama, provas de execução, inventário pós-limpeza e instruções de reprodução. O vídeo de apresentação está em [`docs/portfolio/mx-presentation-ptbr.mp4`](docs/portfolio/mx-presentation-ptbr.mp4).

Os próximos incrementos técnicos são reexecução idempotente após aprovação humana, sincronização cross-channel com reconexão, threat model ampliado, retenção/privacidade, backup e restauração testados, observabilidade histórica e integrações controladas com Git, notas e serviços externos.

O backlog detalhado está em [`docs/product-backlog.md`](docs/product-backlog.md). A documentação técnica complementar está organizada em [`docs/architecture-clean.md`](docs/architecture-clean.md), [`docs/mx-core-skills-architecture.md`](docs/mx-core-skills-architecture.md), [`docs/multiplatform-architecture.md`](docs/multiplatform-architecture.md), [`docs/streaming-sse.md`](docs/streaming-sse.md), [`docs/approvals-and-tools.md`](docs/approvals-and-tools.md), [`docs/security/threat-model.md`](docs/security/threat-model.md), [`docs/operations/local-runbook.md`](docs/operations/local-runbook.md) e [`docs/testing/test-strategy.md`](docs/testing/test-strategy.md).

## Licença e escopo

O MX é um projeto pessoal local em evolução. Os arquivos de configuração, credenciais, modelos, dados e logs da máquina do usuário não fazem parte do produto versionado.
