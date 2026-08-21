# Auditoria Sistêmica de Fluxos — MX Core

**Data:** 21 de agosto de 2026  
**Escopo:** Conversa, contexto, anexos, skills, ferramentas, execução e segurança do MX Core.

## Resumo executivo

Esta auditoria eliminou caminhos que faziam anexos visuais contornarem o **MX Core**, corrigiu a semântica de idempotência para permitir novas tentativas após falha ou cancelamento, e tornou efetiva a allowlist declarada por cada skill. O fluxo agora preserva o mesmo contexto — incluindo imagens — desde a entrada da conversa até a skill selecionada e o gateway de modelo local.

| Área | Risco encontrado | Correção aplicada | Estado |
|---|---|---|---|
| Idempotência | Uma execução com a mesma chave permanecia bloqueada mesmo em `FAILED` ou `CANCELLED`. | Somente estados ativos (`RECEIVED`, `ROUTED`, `EXECUTING`, `AWAITING_APPROVAL` e `VERIFYING`) bloqueiam uma nova tentativa. | Corrigido |
| Anexos visuais | O gateway encaminhava imagens diretamente ao modelo visual e ignorava skills, telemetria, run e políticas do Core. | Imagens são transportadas em `SkillRequest` e seguem pelo MX Core até a skill e o modelo. | Corrigido |
| Roteamento | O fallback de prompt com imagem não explicitava a natureza visual do pedido. | O roteador seleciona `general` de forma explícita para anexos visuais sem trigger especialista. | Corrigido |
| Contexto textual | Memória, anexos e solicitação atual podiam se misturar visualmente no prompt. | Blocos delimitados, rotulados e declarados como dados não confiáveis. | Corrigido |
| Ferramentas | A allowlist declarada por skills era informativa; o executor não a aplicava. | A política agora nega ferramentas fora da allowlist da skill selecionada. | Corrigido |
| Endpoints paralelos | O histórico legado aceitava criação direta de mensagens com papel fornecido pelo cliente. | A criação direta foi removida; mensagens seguem o caso de uso central. | Corrigido |
| Observabilidade | Métricas detalhadas estavam incluídas entre rotas públicas. | Apenas saúde e informação permanecem públicas; métricas exigem autenticação. | Corrigido |

## Fluxo canônico após a correção

```text
Cliente universal
  → SendMessageUseCase
  → memória + anexos delimitados + ModelRequest(imagens)
  → MxCoreModelGateway
  → MxCoreService
  → SkillRouter(SkillRequest)
  → Skill selecionada
  → PolicyEngine + ToolExecutor, quando aplicável
  → gateway Ollama local
  → resposta persistida na conversa
```

O **MX Core** permanece como o único ponto de orquestração. Skills não são endpoints alternativos: recebem o contexto selecionado, executam sua especialidade e devolvem um único resultado ao Core.

## Contratos de segurança reforçados

O conteúdo de memória e anexos é tratado como dado não confiável, não como autorização. Ferramentas continuam sujeitas a autonomia, tipo de efeito, aprovação explícita quando necessária e, agora, à allowlist da skill. A skill de desenvolvimento pode propor ou executar leitura e pode solicitar escrita somente pelo fluxo que exige aprovação; a skill de qualidade declara exclusivamente ferramentas de leitura registradas.

## Cobertura de regressão

A suíte Maven foi executada com sucesso após as alterações. Foram incluídos contratos para:

| Contrato validado | Cobertura |
|---|---|
| Retentativa com a mesma chave após `FAILED` e `CANCELLED` | `MxCoreServiceTest` |
| Bloqueio enquanto a execução está ativa | `MxCoreServiceTest` |
| Preservação de imagem até a skill | `MxCoreServiceTest` |
| Encaminhamento visual pelo Core, sem bypass | `MxCoreModelGatewayTest` |
| Delimitadores de memória no prompt | `SendMessageUseCaseTest` |
| Negação de ferramenta fora da allowlist | `ToolExecutorTest` |

## Evoluções arquiteturais que permanecem deliberadamente fora deste patch

O roteamento ainda é baseado em triggers textuais e deverá evoluir para classificação semântica com avaliação rastreável. O contexto recente continua limitado por número de mensagens e deve migrar para orçamento por tokens, sumarização e recuperação semântica por usuário e conversa. Essas melhorias exigem desenho de dados, métricas de qualidade e validação de desempenho; não foram incluídas nesta correção de baixo risco.

---

# System Flow Audit — MX Core

**Date:** August 21, 2026  
**Scope:** MX Core conversation, context, attachments, skills, tools, execution, and security paths.

## Executive summary

This audit removed paths that allowed visual attachments to bypass the **MX Core**, fixed idempotency semantics so that failed or cancelled work can be retried, and made each skill's declared tool allowlist enforceable. The same context — including images — now travels from the conversation boundary to the selected skill and the local model gateway.

| Area | Identified risk | Applied correction | Status |
|---|---|---|---|
| Idempotency | A matching key remained blocked after `FAILED` or `CANCELLED`. | Only active states block a retry. | Fixed |
| Visual attachments | Images could bypass skills, telemetry, run tracking, and Core policies. | Images travel through `SkillRequest` and the MX Core. | Fixed |
| Routing | Visual fallback was not explicit. | Image-only requests select the central `general` skill when no specialist matches. | Fixed |
| Text context | Memory, attachment content, and the current request were not structurally separated. | Explicit bounded sections mark memory and attachments as untrusted data. | Fixed |
| Tools | Skill allowlists were declarative only. | Tool policy denies a tool outside the selected skill's allowlist. | Fixed |
| Parallel endpoint | The legacy history endpoint could create client-declared message roles directly. | Direct message creation was removed; messages use the central use case. | Fixed |
| Observability | Detailed metrics were publicly reachable. | Only health and info stay public; metrics require authentication. | Fixed |

## Deliberately deferred architectural work

Keyword routing should later evolve into an evaluated semantic classifier. Recent-conversation context should move from a message-count limit to token budgeting, summarization, and user/conversation-scoped semantic retrieval. Those changes require data-model design, quality metrics, and performance validation, so they are not part of this low-risk corrective patch.
