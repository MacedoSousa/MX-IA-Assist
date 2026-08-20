# Tools, Policy Engine e aprovação humana

## Princípio

Uma skill pode propor uma tool, mas não pode alterar o workspace, executar shell arbitrário ou chamar serviço externo por conta própria. Toda execução passa por registry e **Policy Engine**. A política avalia usuário, skill, tool, argumentos, workspace, efeito e nível de autonomia.

> Texto produzido por modelo é dado não confiável para a política. Um prompt, arquivo ou resposta do Ollama não pode elevar autonomia, alterar allowlist ou substituir uma decisão do Policy Engine.

## Contrato conceitual

Uma tool declara nome, versão, argumentos, efeito, timeout, autonomia mínima e resultado. Os efeitos são classificados pelo risco, por exemplo:

| Efeito | Exemplo | Regra inicial |
|---|---|---|
| `READ` | Listar ou ler arquivo permitido | Pode ser permitido dentro do sandbox e dos limites. |
| `WRITE` | Alterar arquivo no workspace | Exige aprovação humana na fundação atual. |
| `DELETE` | Excluir dados | Não é liberado pela primeira versão. |
| `EXTERNAL` | Chamar serviço externo | Requer connector isolado, escopo mínimo e aprovação. |

A decisão de política possui um resultado explícito: `ALLOW`, `DENY` ou `REQUIRE_APPROVAL`, sempre acompanhado de motivo auditável. `DENY` não executa a tool. `REQUIRE_APPROVAL` também não executa a tool; cria uma proposta pendente no fluxo de aprovação quando a integração completa estiver finalizada.

## Tools atuais

A composição Spring registra inicialmente:

| Tool | Tipo | Estado |
|---|---|---|
| `workspace.list` | Leitura | Disponível atrás do registry/policy e limitada ao workspace. |
| `workspace.write_file` | Escrita | Implementada com `WRITE` e autonomia mínima `EXECUTE_WITH_APPROVAL`; a ponte automática para `ExecutionRun` ainda está pendente. |

`WorkspaceWriteTool` resolve o caminho contra o workspace configurado, rejeita traversal (`..`), bloqueia symlinks, limita o payload em bytes e escreve de forma atômica. O workspace deve ser explícito e não pode ser a raiz indiscriminada do disco.

## Fluxo de aprovação

O fluxo desejado para uma ação sensível é:

```text
1. MX Core/skill propõe uma ToolRequest
2. ToolRegistry localiza a tool
3. PolicyEngine avalia contexto e argumentos
4. ALLOW             → ToolExecutor executa
5. DENY              → ToolExecutor retorna recusa segura
6. REQUIRE_APPROVAL  → ExecutionRun em AWAITING_APPROVAL
7. Cliente consulta o run e mostra resumo/efeito
8. Usuário aprova ou rejeita no web/mobile
9. Backend revalida owner, nonce, expiração e política
10. Aprovação → EXECUTING → execução → VERIFYING → estado final
11. Rejeição → CANCELLED, com motivo registrado
```

Hoje os casos de uso e endpoints de aprovação já existem, mas o passo 6 ainda precisa ser ligado ao `ToolExecutor`. Essa integração é o próximo incremento técnico prioritário.

## Requisitos da ponte `ToolExecutor → ExecutionRun`

A criação do run pendente deve ser atômica em relação ao registro da proposta. O snapshot deve conter apenas argumentos controlados e necessários para a revisão, além de tool, versão, efeito, skill solicitante, `userId`, `conversationId`, `correlationId`, data de expiração e nonce de uso único. Segredos, tokens e conteúdo desnecessário não devem ser persistidos.

A aprovação deve exigir o mesmo `userId` proprietário do run. O backend deve verificar que o estado ainda é `AWAITING_APPROVAL`, que o nonce não foi usado, que a proposta não expirou e que os argumentos apresentados são exatamente os argumentos aprovados. A política deve ser reavaliada imediatamente antes da execução, pois workspace, configuração ou allowlist podem ter mudado.

Rejeição deve exigir motivo não vazio, persistir a decisão e mover o run a `CANCELLED` ou a outro estado seguro definido pelo aggregate. A resposta não deve sugerir que a tool foi executada. Conflitos de estado retornam HTTP 409; run inexistente ou pertencente a outro usuário retorna 404 indistinguível.

## Endpoints atuais

```text
GET  /api/v1/runs/{runId}
POST /api/v1/runs/{runId}/approve
POST /api/v1/runs/{runId}/reject
```

O corpo de rejeição possui `reason` com tamanho limitado. A resposta de run informa `runId`, `correlationId`, status, skill, aprovação pendente, output seguro, código de erro e timestamps.

## Threats e testes obrigatórios

A suíte deve cobrir traversal, symlink, workspace inexistente, arquivo acima do limite, argumentos inválidos, tool ausente, `DENY`, `REQUIRE_APPROVAL`, acesso cruzado entre usuários, aprovação duplicada, nonce reutilizado, expiração, mutação de argumentos e concorrência entre aprovação e rejeição. Deve haver pelo menos um teste de prompt injection provando que texto de arquivo não altera a política.

A aprovação não é uma confirmação genérica da conversa. Ela autoriza somente a proposta identificada por run, tool, argumentos, efeito e nonce. Alterações nesses campos devem invalidar a aprovação e exigir nova decisão.
