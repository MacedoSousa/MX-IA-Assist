# MX — Resumo de implementação

> Este documento registra a fundação implementada até agosto de 2026. O projeto está em evolução incremental; os itens marcados como parciais ou planejados não devem ser tratados como concluídos.

## Visão geral

O MX foi reorganizado em torno de um **MX Core único**, que recebe a mensagem do usuário, cria contexto de execução, roteia internamente para uma skill especialista, aplica políticas de autonomia, conversa com o gateway de modelo local e persiste o lifecycle do run. Web, Android e iOS usam o mesmo cliente Expo e não se comunicam diretamente com skills ou tools.

A implementação segue os princípios de **Clean Architecture, TDD e SOLID**. O núcleo depende de portas; adapters HTTP, JPA, Ollama e composição Spring ficam nas bordas. A segurança é aplicada por padrão, com isolamento por usuário, tokens revogáveis e execução de tools submetida ao Policy Engine.

## Incrementos implementados

### P0 — Autenticação multiplataforma

A autenticação possui access token JWT associado a `sessionId`, sessões persistidas, refresh token rotativo armazenado como hash SHA-256, logout revogável e filtro JWT que rejeita tokens de sessões revogadas. CORS e os pontos de entrada de autenticação foram organizados para os canais web e mobile.

O cliente Expo armazena access e refresh token por meio de adapters de sessão. No web utiliza `localStorage`; em Android e iOS utiliza armazenamento seguro nativo. O refresh automático é deduplicado para evitar múltiplas rotações concorrentes quando várias requisições recebem `401` ao mesmo tempo.

### P1 — Lifecycle de `ExecutionRun`

O MX Core cria e acompanha um `ExecutionRun` desde o recebimento até a conclusão ou falha. A sequência normal é:

```text
RECEIVED → ROUTED → EXECUTING → VERIFYING → COMPLETED
                                         └──→ FAILED
```

O aggregate também suporta `AWAITING_APPROVAL` e `CANCELLED`. O `runId` é propagado por `MxCoreResponse`, `ModelResponse`, `SendMessageResult` e `ChatV1Response`. O caso de uso `GetExecutionRunUseCase` e o endpoint `GET /api/v1/runs/{runId}` aplicam escopo por `userId`.

### P2 — Streaming SSE

Foi criada a porta `StreamingModelGateway` com o observer `ModelStreamObserver`. O adapter Ollama interpreta respostas NDJSON e emite callbacks de início, fragmento, conclusão e erro. O controller SSE usa executor virtual dedicado para não bloquear o fluxo de requisições.

O contrato de eventos é:

| Evento | Conteúdo | Finalidade |
|---|---|---|
| `started` | `runId`, `correlationId` | Identifica a execução iniciada. |
| `token` | `delta` | Entrega um fragmento incremental da resposta. |
| `completed` | status e `runId` | Finaliza o fluxo com estado conhecido. |
| `error` | código e mensagem | Comunica falha controlada. |

As skills `general` e `development` possuem suporte ao caminho de streaming. O cliente Expo implementa `sendMessageStream()`, parser SSE e atualização token a token da interface.

### P3 — Aprovação humana cross-channel

O status de aprovação foi normalizado para `AWAITING_APPROVAL`, com migration Flyway V6 para compatibilidade. Foram implementados `ApproveExecutionRunUseCase` e `RejectExecutionRunUseCase`, sempre com escopo por usuário. A aprovação move o run para `EXECUTING`; a rejeição exige motivo e encerra o run como `CANCELLED`. Tentativas de decidir um run em estado inválido resultam em conflito HTTP 409 por meio de `ExecutionRunApprovalException`.

Os endpoints são:

```text
POST /api/v1/runs/{runId}/approve
POST /api/v1/runs/{runId}/reject
```

O cliente Expo expõe `approveRun()` e `rejectRun()` para permitir decisão em qualquer canal.

### P3 — Tool de escrita sandboxed

`WorkspaceWriteTool` foi adicionada como a primeira tool com efeito `WRITE` e autonomia mínima `EXECUTE_WITH_APPROVAL`. A execução ocorre em workspace configurado, bloqueia path traversal e symlink, utiliza escrita atômica e rejeita payloads acima do limite configurado. A tool é registrada na composição Spring e sempre passa pelo Policy Engine.

A cobertura inclui testes unitários da tool e testes de política (`WorkspaceWriteToolTest` e `WorkspaceWriteToolPolicyTest`).

## Componentes relevantes

| Componente | Responsabilidade |
|---|---|
| `MxCoreService` | Ponto de entrada único, correlação, roteamento e coordenação da execução. |
| `ExecutionRun` | Aggregate de lifecycle e transições válidas. |
| `StreamingModelGateway` | Porta de streaming independente do adapter Ollama. |
| `PolicyEngine` | Avaliação de allowlist, efeito e autonomia da tool. |
| `ToolExecutor` | Execução controlada de tools após decisão de política. |
| `WorkspaceWriteTool` | Escrita atômica restrita ao workspace sandboxed. |
| `AuthController` | Login, refresh e logout. |
| `RefreshTokenService` | Rotação e hash de refresh tokens. |
| `ExecutionRunV1Controller` | Consulta, aprovação e rejeição de runs. |
| `ChatV1SseController` | Endpoint de resposta incremental via SSE. |
| `ConversationConfiguration` | Composição dos adapters, skills, tools e casos de uso. |
| `clients/mx-app` | Cliente universal Expo para web, Android e iOS. |

## Validações realizadas

| Validação | Resultado |
|---|---|
| Maven backend | 62 testes executados, 0 falhas, usando `C:\Windows\Temp\mx-target` e `C:\Windows\Temp\mx-m2`. |
| TypeScript Expo | `npm run typecheck` aprovado. |
| Exportação web | `npx expo export --platform web` aprovado. |

Essas validações demonstram a estabilidade da fundação atual, mas não substituem os testes E2E, de segurança adversarial, reconexão cross-channel e operação local que permanecem no backlog.

## Lacunas conhecidas

A principal lacuna do P3 é integrar automaticamente o retorno `REQUIRE_APPROVAL` do `ToolExecutor` à criação de um `ExecutionRun` persistido em `AWAITING_APPROVAL`, contendo snapshot controlado dos argumentos, tool, efeito, usuário, nonce e expiração. A aprovação deve revalidar a política e impedir replay ou mutação de argumentos.

Também permanecem pendentes sincronização idempotente entre canais, threat model e testes de prompt injection, política de retenção e exclusão, backup e restauração, benchmark p50/p95, timeout do Ollama, revisão de N+1, integrações controladas e notificações.

## Referências internas

- [`README.md`](README.md)
- [`docs/api/mx-v1.yaml`](docs/api/mx-v1.yaml)
- [`docs/product-backlog.md`](docs/product-backlog.md)
- [`docs/streaming-sse.md`](docs/streaming-sse.md)
- [`docs/approvals-and-tools.md`](docs/approvals-and-tools.md)
