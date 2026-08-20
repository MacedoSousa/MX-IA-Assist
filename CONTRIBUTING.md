# Contribuindo com o MX

## Princípios

O MX é um projeto local orientado por **Clean Architecture, TDD, SOLID, segurança por padrão e evolução incremental**. O usuário conversa somente com o MX Core; skills e tools são capacidades internas, com contratos e políticas explícitos.

Uma contribuição deve resolver um comportamento observável, manter dependências apontando para dentro e incluir testes para o caminho feliz e para as falhas relevantes. Não adicione autonomia, connector externo ou persistência sensível sem atualizar policy, threat model, ADR e documentação correspondente.

## Fluxo de trabalho

Crie uma branch curta a partir de `master`, com nome que indique o tipo da alteração:

```text
feat/mx-145-tool-approval-bridge
fix/refresh-replay
security/prompt-injection-fixtures
docs/local-runbook
```

Faça commits pequenos e explique a intenção. O primeiro commit de uma história deve preferencialmente conter contrato/teste; commits seguintes podem conter implementação e refatoração. Antes de abrir pull request, rebase ou atualize a branch com o estado remoto e resolva conflitos preservando os contratos atuais.

## Red → Green → Refactor

Comece pelo teste que deve falhar. Implemente o menor comportamento que o faça passar. Refatore com testes verdes. Caso a mudança altere a API, atualize OpenAPI e cliente na mesma alteração ou documente a compatibilidade temporária.

## Validações locais

Backend no Windows:

```powershell
cd D:\MX\core-service
.\mvnw.cmd -Dmx.build.directory=C:\Windows\Temp\mx-target `
  -Dmaven.repo.local=C:\Windows\Temp\mx-m2 test
```

Cliente Expo:

```powershell
cd D:\MX\clients\mx-app
npm run typecheck
npx expo export --platform web
```

Antes do commit:

```powershell
cd D:\MX
git diff --check
git status --short
git diff --cached --name-only
```

Não use builds no diretório do projeto para mascarar falhas. `target`, `node_modules`, `.expo`, `dist`, logs, bancos, modelos, pacotes zip, `.env` e configurações pessoais não pertencem ao commit.

## Backend

Casos de uso não devem importar controllers, JPA, cliente Ollama ou filesystem. Use portas e fakes nos testes unitários. Controllers devem validar/converter payload e delegar; repositories JPA devem permanecer adapters. Chamadas de rede não devem manter transação aberta.

Toda tool deve declarar schema, efeito, autonomia, timeout e resultado. A execução sempre passa pelo Policy Engine. `WRITE`, `DELETE` e `EXTERNAL` não podem ser liberados por texto do modelo. Mudanças na ponte de approval devem cobrir ownership, expiração, nonce, replay, mutação de argumentos e concorrência.

## API e cliente

Rotas públicas novas devem ter DTOs estáveis, erro controlado, autenticação e documentação OpenAPI. O cliente universal não chama skills diretamente e não duplica regra de autorização. Tokens não devem aparecer em URLs, logs, screenshots ou fixtures.

Alterações no lifecycle devem preservar `runId`, `correlationId`, `conversationId` e os estados documentados, incluindo `AWAITING_APPROVAL`. Mudanças no SSE devem atualizar parser, contrato e testes de reconexão quando aplicável.

## Pull request

A descrição deve conter problema, solução, riscos, arquivos principais, testes executados, impacto de migração e plano de rollback. Para segurança ou autonomia, inclua threat model/ADR e cenários negativos. Para alteração de contrato, inclua exemplos de request/response e impacto no web/Android/iOS.

O revisor deve conferir: isolamento por usuário, ausência de segredos, política antes da tool, atomicidade, idempotência, observabilidade tolerante a falhas, documentação e estado honesto do backlog. Não use “complete” ou “production-ready” quando existirem lacunas conhecidas.

## Commits

Use mensagens no formato convencional quando possível:

```text
feat: add execution approval bridge
fix: reject revoked refresh token
security: harden workspace path validation
docs: update local runbook
```

O push deve ocorrer somente depois de revisar `git diff --cached --name-only` e confirmar que o índice contém apenas código, documentação, migrations, testes e configurações de exemplo intencionais.
