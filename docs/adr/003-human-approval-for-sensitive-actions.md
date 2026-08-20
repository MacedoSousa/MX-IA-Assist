# ADR-003 — Aprovação humana para ações sensíveis

- **Status:** accepted
- **Data:** 2026-08-20
- **Decisores:** maintainers do MX

## Contexto

O MX deve ajudar a programar e organizar tarefas, mas escrita em workspace, exclusão, execução remota e ações externas podem produzir efeitos irreversíveis. Texto de modelo e conteúdo recuperado não são autoridades confiáveis.

## Decisão

Toda tool declara efeito e autonomia mínima. `READ` pode ser permitido dentro de sandbox e limites; `WRITE`, `DELETE` e ações `EXTERNAL` devem resultar em `REQUIRE_APPROVAL` ou `DENY` na fundação. A decisão é tomada pelo Policy Engine antes do executor.

A proposta aprovada deve ser específica ao usuário, run, tool, versão, argumentos, efeito, nonce e prazo. O backend revalida a policy no momento da execução e rejeita replay, expiração, mutação ou decisão de outro usuário.

## Alternativas consideradas

Aprovação global por conversa foi rejeitada por permitir que uma confirmação libere uma ação diferente. Autonomia total do modelo foi rejeitada por não fornecer controle nem auditoria. Confirmação somente no cliente foi rejeitada porque o cliente não é a autoridade de segurança.

## Consequências

A experiência possui um passo adicional para ações sensíveis, mas o usuário mantém controle e o sistema pode explicar o que será feito. O backend precisa persistir proposta e decisão, implementar nonce/expiração e garantir idempotência. A integração automática entre `ToolExecutor` e `ExecutionRun` é requisito para considerar o ciclo fechado.

## Validação

Testar que `REQUIRE_APPROVAL` não executa, que a proposta fica em `AWAITING_APPROVAL`, que approve e reject respeitam ownership, que o nonce não é reutilizado, que a policy é reavaliada e que uma alteração nos argumentos exige nova aprovação.
