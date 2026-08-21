# Validação do ciclo de autoextensão do MX — 2026-08-21

## Português

### Objetivo

Validar que o MX consegue transformar uma proposta estruturada em uma extensão de tipo `AGENT`, `SKILL` ou `TOOL`, validar o payload, executar somente comandos allowlisted em um worktree temporário, aplicar a mudança apenas após sucesso, gerar evidência SHA-256 e criar um commit Git local.

### Resultado

A implementação foi publicada no repositório privado `MacedoSousa/MX-IA-Assist` até o commit `5c1fe1c`. O smoke test executado pelo próprio `mx_evolution_runner.py` criou o commit `8220be8` depois de executar a validação `maven_test` em um worktree isolado.

| Verificação | Resultado |
|---|---|
| Testes Maven completos | 42 suítes, 121 testes, 0 falhas, 0 erros, 0 ignorados |
| Teste de política | Allowlist de paths, limite de payload, bloqueio de segredos e push desligado por padrão |
| Teste de submissão | Plano JSON estruturado convertido em job persistível |
| Teste da SelfImprovementSkill | Somente protocolo estruturado; sem shell livre fornecido pelo modelo |
| Teste dry-run | `DRY_RUN`, sem alteração do checkout |
| Smoke test real | `COMPLETED`, com `maven_test` em worktree temporário |
| Commit automático | `8220be8` criado pelo runner |
| Compose Windows | `docker-compose.exe config --quiet` concluído sem erro |
| Build Docker | `docker-compose.exe up -d --build --no-deps mx-core` concluído |
| Saúde do core | `GET /actuator/health` retornou `{"groups":["liveness","readiness"],"status":"UP"}` |
| Runner Windows | Python 3.12 instalado e `run_evolution.bat --once --dry-run` executado com sucesso |

### Fluxo implementado

1. A `SelfImprovementSkill` solicita ao modelo um plano JSON fechado.
2. A `self_extension.submit` valida o contrato e enfileira o job no diretório persistente de evolução.
3. O `mx_evolution_runner.py` lê o job fora do container Java, rejeita paths fora da allowlist, bloqueia segredos, limita tamanho e validações, e impede comandos shell arbitrários.
4. O runner cria um worktree temporário, aplica o conteúdo somente nesse ambiente e executa validações fixas como Maven, typecheck Expo, export web, Compose config e builds Docker.
5. Em caso de sucesso, o conteúdo é aplicado ao checkout principal, a evidência é gerada, o Git cria o commit local e o job é movido para `completed`.
6. Em caso de falha antes do commit, o worktree é removido, o checkout é restaurado e o job é movido para `failed`.

### Limites intencionais

O `mx-core` não recebe o Docker socket e não executa shell. O push remoto não é habilitado pelo payload: `allowPush` permanece falso por padrão e só pode ser liberado por configuração externa `MX_EVOLUTION_ALLOW_PUSH=true`. Mesmo nessa modalidade, a recomendação operacional é revisar a auditoria e o diff antes de permitir publicação remota.

### Operação no Windows

Para worker contínuo:

```bat
D:\MX\scripts\run_evolution.bat
```

Para uma rodada única de diagnóstico:

```bat
D:\MX\scripts\run_evolution.bat --once --dry-run
```

A fila persistente fica em `D:\MX\data\evolution\jobs\`. Os diretórios `pending`, `running`, `completed` e `failed`, além do `audit.jsonl`, não devem ser versionados.

## English

### Objective

Validate that MX can turn a structured proposal into an `AGENT`, `SKILL`, or `TOOL` extension, validate the payload, run only allowlisted commands in a temporary worktree, apply changes only after success, generate SHA-256 evidence, and create a local Git commit.

### Result

The implementation was published to the private `MacedoSousa/MX-IA-Assist` repository through commit `5c1fe1c`. The smoke test executed by `mx_evolution_runner.py` created commit `8220be8` after running `maven_test` inside an isolated temporary worktree.

| Check | Result |
|---|---|
| Full Maven suite | 42 suites, 121 tests, 0 failures, 0 errors, 0 skipped |
| Policy test | Path allowlist, payload limits, secret blocking, push disabled by default |
| Submission test | Structured JSON plan persisted as a job |
| SelfImprovementSkill test | Structured protocol only; no model-provided free shell |
| Dry-run test | `DRY_RUN`, no checkout mutation |
| Real smoke test | `COMPLETED`, with `maven_test` in a temporary worktree |
| Automatic commit | `8220be8` created by the runner |
| Windows Compose | `docker-compose.exe config --quiet` completed successfully |
| Docker build | `docker-compose.exe up -d --build --no-deps mx-core` completed successfully |
| Core health | `GET /actuator/health` returned liveness/readiness `UP` |
| Windows runner | Python 3.12 installed and `run_evolution.bat --once --dry-run` completed successfully |

### Deliberate boundaries

The `mx-core` container does not receive the Docker socket and does not execute shell commands. Remote push is not enabled by a job payload: `allowPush` is false by default and can only be enabled by the external `MX_EVOLUTION_ALLOW_PUSH=true` setting. Even then, reviewing the audit trail and diff before remote publication is the recommended operational practice.
