# Threat model inicial do MX

## Escopo

Este documento cobre o MX executado localmente, com cliente Expo web/Android/iOS, backend Spring Boot, PostgreSQL, Redis, Ollama, skills internas e tools sandboxed. O modelo considera que o computador local pode conter dados pessoais, código-fonte, credenciais e modelos. Ele não assume que todo conteúdo recebido pelo modelo seja confiável.

A ameaça principal não é somente um invasor remoto. Também são relevantes arquivos maliciosos no workspace, prompt injection, erro de configuração, token revogado ainda aceito, acesso cruzado entre usuários, replay de aprovação e execução de tool fora da política.

## Ativos protegidos

| Ativo | Impacto de exposição ou alteração |
|---|---|
| Credenciais e refresh tokens | Sequestro de sessão e acesso persistente. |
| Histórico de conversas e memória | Exposição de dados pessoais, profissionais ou sensíveis. |
| Workspace e código-fonte | Alteração, destruição, exfiltração ou corrupção. |
| Configuração e allowlist | Elevação de autonomia e bypass de segurança. |
| Runs, aprovações e auditoria | Perda de rastreabilidade ou execução indevida. |
| Segredos de connectors | Ações externas em nome do usuário. |
| Disponibilidade do Ollama e banco | Perda de serviço ou estado inconsistente. |

## Fronteiras de confiança

```text
[Usuário / dispositivo]
        │ sessão autenticada
        ▼
[Cliente Expo]
        │ HTTPS/HTTP local + JWT
        ▼
[API / MX Core]
        │ portas e casos de uso
        ├── [Skills internas]
        ├── [Policy Engine / ToolExecutor]
        ├── [PostgreSQL / Flyway]
        └── [Ollama local]
             │ modelo e conteúdo gerado não são autoridade de política
             ▼
        [Workspace sandboxed / connectors futuros]
```

O cliente é uma fronteira de entrada não confiável; o texto do usuário e os dados recuperados são dados, não instruções de sistema. Ollama é um adapter de modelo, não uma autoridade de segurança. Tools são a fronteira de maior risco operacional e devem ser autorizadas por política explícita.

## Ameaças e controles

| ID | Ameaça | Controle existente | Lacuna ou teste necessário |
|---|---|---|---|
| T-01 | Roubo ou replay de access token | JWT associado a sessão persistida e revogável. | Access token curto, rotação, expiração e testes E2E. |
| T-02 | Uso de refresh token antigo | Rotação com hash SHA-256 e invalidação do anterior. | Testar concorrência, replay e revogação global. |
| T-03 | Acesso a run de outro usuário | Casos de uso consultam por `userId`; controller protegido. | Testes negativos para todos os endpoints e estados. |
| T-04 | Prompt injection em arquivo ou resposta | Política separada da geração; tools passam pelo Policy Engine. | Fixtures adversariais e separação explícita de system/user/retrieved context. |
| T-05 | Path traversal | Sandbox de workspace e bloqueio de `..`. | Testar encoding, paths absolutos, junctions e symlinks em Windows. |
| T-06 | Symlink ou junction escapando do workspace | `WorkspaceWriteTool` rejeita symlinks. | Testes em filesystem real e condições de corrida. |
| T-07 | Escrita sem aprovação | Tool declara efeito `WRITE` e autonomia `EXECUTE_WITH_APPROVAL`. | Integrar `REQUIRE_APPROVAL` ao `ExecutionRun` e provar que não há bypass. |
| T-08 | Replay de aprovação | Estados de run e conflito HTTP 409 existem. | Nonce, expiração, uso único e testes concorrentes ainda faltam. |
| T-09 | Exposição de segredo em logs | Telemetria tolerante a falhas e contrato de erro. | Redaction sistemática, retenção e testes de logs. |
| T-10 | Ollama malformado ou indisponível | Erro controlado no gateway e estado `FAILED`. | Timeout, retry limitado, circuit breaker e health indicator. |
| T-11 | Segredo em Git | `.env` e dados locais são ignorados. | Scan de segredo antes do primeiro push e revisão staged. |
| T-12 | Dependência comprometida | Build local e dependências Maven/npm. | Lockfiles, atualização controlada e verificação de dependências. |

## Requisitos de segurança por fluxo

### Sessão

Login deve devolver somente tokens e metadados necessários. Logout deve revogar a sessão; qualquer chamada posterior com access token associado deve falhar. Refresh deve invalidar o token anterior e impedir reutilização. O web não deve colocar tokens em URL, e os canais nativos devem utilizar armazenamento seguro.

### Conversação

Toda requisição protegida precisa de identidade e escopo. `conversationId`, `runId` e qualquer memória devem ser filtrados pelo proprietário. Erros não devem revelar se um identificador de terceiro existe. Prompts e respostas devem seguir uma política de retenção explícita.

### Tool

Antes de executar, o sistema deve validar tool, versão, argumentos, owner, workspace, efeito e autonomia. A política não pode ser alterada pelo conteúdo do prompt. A proposta de aprovação deve ser específica, expirar e não ser reutilizável. A execução deve ser atômica, limitada e auditável.

## Critérios de aceitação de segurança

O núcleo não deve ser considerado pronto para ampliar autonomia até que os seguintes cenários sejam automatizados:

1. Usuário A não consulta nem decide o run de usuário B.
2. Token revogado falha imediatamente em rota protegida.
3. Refresh token rotacionado não pode ser reutilizado.
4. Arquivo com instrução maliciosa não altera a decisão da policy.
5. Traversal, caminho absoluto, symlink e junction fora do workspace falham.
6. `REQUIRE_APPROVAL` não grava nada antes da decisão humana.
7. Aprovação duplicada, expirada, mutada ou concorrente não executa duas vezes.
8. Ollama indisponível gera falha segura e não marca resposta falsa como concluída.
9. Logs e métricas não carregam tokens, prompts completos ou dados pessoais não necessários.
10. Segredos, modelos, bancos e builds não entram no commit.

## Risco residual

O MX é local, mas “local” não significa automaticamente confiável. Um processo com acesso ao mesmo usuário do sistema pode ler arquivos locais, capturar tokens ou alterar configurações. O objetivo desta arquitetura é reduzir a superfície do MX e preservar controle sobre ações, não substituir as proteções do sistema operacional, criptografia de disco, backups ou boas práticas de credenciais.
