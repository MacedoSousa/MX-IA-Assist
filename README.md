# MX — Assistente pessoal local

> **Status:** fundação avançada em evolução. A base P0–P3 está implementada e validada em nível de testes automatizados, mas o projeto ainda não deve ser tratado como produto final ou como sistema de produção.

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
| Skills | Implementado inicialmente | Skills `general` e `development` atrás do MX Core, incluindo suporte a streaming. |
| Tools | Fundação segura | `WorkspaceWriteTool` com sandbox, bloqueio de traversal/symlink, escrita atômica e limite de bytes. |
| Aprovação | Parcialmente integrada | Casos de uso, endpoints e cliente existem; a criação automática do run a partir de `REQUIRE_APPROVAL` ainda é uma lacuna. |
| Cliente | Implementado em nível funcional | App Expo universal com login, logout, refresh automático, chat síncrono, streaming e consulta/aprovação de runs. |
| Observabilidade | Fundação implementada | Actuator, métricas e correlação; dashboards, alertas e baseline de performance ainda estão planejados. |

## Requisitos locais

O desenvolvimento principal ocorre em Windows, com Java 21, Docker Desktop com Compose, Node.js, npm, Git e Ollama. PostgreSQL, Redis, Ollama e Open WebUI podem ser iniciados pelo Compose; o MX Core é executado como serviço Spring Boot no mesmo stack.

Os modelos Ollama não são armazenados no Git. O arquivo `infrastructure/docker/env/.env.example` documenta somente valores de exemplo. Credenciais reais, bancos locais, logs, modelos, builds e distribuições de ferramentas devem permanecer fora do versionamento.

## Execução local

### Infraestrutura

A partir da raiz do projeto, inicie o stack local:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose up -d --build
```

O Compose atual inclui PostgreSQL, Redis, Ollama, Open WebUI e o container do MX Core. As portas e mounts devem ser confirmados em `infrastructure/docker/compose/docker-compose.yml`; não presuma que os scripts legados `start.bat` e `start.sh` representam o cliente Expo atual, pois eles ainda servem o frontend estático legado.

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
| `GET` | `/api/v1/runs/{runId}` | Consultar o run do usuário autenticado. |
| `POST` | `/api/v1/runs/{runId}/approve` | Aprovar um run pendente. |
| `POST` | `/api/v1/runs/{runId}/reject` | Rejeitar um run com motivo obrigatório. |

Exemplo de mensagem síncrona:

```bash
curl -X POST http://localhost:8080/api/v1/conversations/messages \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Explique como organizar este projeto"}'
```

O endpoint de streaming retorna eventos SSE. O evento `started` apresenta `runId` e `correlationId`; eventos `token` carregam deltas textuais; `completed` informa o encerramento; `error` representa falha controlada do fluxo.

## Segurança e autonomia

A autenticação combina access token de curta duração, refresh token rotativo, sessão persistida e revogação. O filtro JWT consulta o estado da sessão e bloqueia tokens associados a sessões revogadas. Casos de uso de runs restringem consultas e decisões ao `userId` autenticado.

Tools não executam diretamente a partir do texto do modelo. Elas passam pelo registry e pelo **Policy Engine**, que avalia efeito, allowlist, autonomia e contexto. A `WorkspaceWriteTool` limita o diretório ao workspace configurado, bloqueia path traversal e symlink, usa escrita atômica e possui limite configurável de bytes. A integração automática entre `REQUIRE_APPROVAL` e `ExecutionRun` em `AWAITING_APPROVAL` ainda está no backlog técnico imediato.

## Validações conhecidas

As validações executadas no ambiente de desenvolvimento foram:

| Validação | Resultado conhecido |
|---|---|
| Suíte Maven do backend | 62 testes, 0 falhas, com diretórios temporários em `C:\Windows\Temp\mx-target` e `C:\Windows\Temp\mx-m2`. |
| TypeScript do cliente | `npm run typecheck` aprovado. |
| Exportação web Expo | `npx expo export --platform web` aprovado. |
|

Execute novamente os comandos após qualquer alteração. O sucesso dessas verificações não substitui os testes E2E, adversariais e operacionais ainda planejados.

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

## Próximos incrementos prioritários

O próximo incremento técnico é integrar `ToolExecutor → ExecutionRun`, criando um run auditável em `AWAITING_APPROVAL` quando a política exigir intervenção humana. Em seguida vêm sincronização cross-channel com reconexão idempotente, threat model e testes adversariais, retenção/privacidade, backup e restauração, timeout e benchmark do Ollama, além das integrações controladas com Git, notas e serviços externos.

O backlog detalhado está em [`docs/product-backlog.md`](docs/product-backlog.md). A documentação técnica complementar está organizada em [`docs/architecture-clean.md`](docs/architecture-clean.md), [`docs/mx-core-skills-architecture.md`](docs/mx-core-skills-architecture.md), [`docs/multiplatform-architecture.md`](docs/multiplatform-architecture.md), [`docs/streaming-sse.md`](docs/streaming-sse.md), [`docs/approvals-and-tools.md`](docs/approvals-and-tools.md), [`docs/security/threat-model.md`](docs/security/threat-model.md), [`docs/operations/local-runbook.md`](docs/operations/local-runbook.md) e [`docs/testing/test-strategy.md`](docs/testing/test-strategy.md).

## Licença e escopo

O MX é um projeto pessoal local em evolução. Os arquivos de configuração, credenciais, modelos, dados e logs da máquina do usuário não fazem parte do produto versionado.
