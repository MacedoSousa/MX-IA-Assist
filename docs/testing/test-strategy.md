# Estratégia de testes do MX

## Objetivo

A estratégia de testes transforma os contratos de arquitetura, segurança e produto em verificações executáveis. Cada incremento deve seguir **Red → Green → Refactor**: primeiro um teste que expressa o comportamento, depois a implementação mínima, e por fim a melhoria estrutural sem perder cobertura.

Uma história não está concluída apenas porque o caminho feliz funciona. A Definition of Done exige testes de sucesso, falha, autorização, limites e regressão relevantes ao risco da mudança.

## Pirâmide

| Camada | Foco | Dependências permitidas |
|---|---|---|
| Unitário | Aggregates, políticas, parsers, casos de uso e adapters puros | Fakes/mocks; sem banco, rede ou modelo real. |
| Contrato | Portas `ModelGateway`, streaming, skills, tools e DTOs | Implementações fake e fixtures estáveis. |
| Integração | Spring context, JPA, Flyway, Security e controllers | Banco de teste ou infraestrutura isolada. |
| E2E | Login → chat → skill → run → tool/approval | Ambiente local controlado com dados descartáveis. |
| Adversarial | Prompt injection, traversal, replay, cross-user e falha externa | Fixtures maliciosas e asserts de não execução. |
| Operacional | Compose, health, backup, restauração e performance | Máquina/ambiente próximo do uso local real. |

A maior parte da regra deve permanecer nos testes unitários. Testes E2E validam composição e fronteiras, não substituem a cobertura dos casos de uso.

## Convenções

Nomes devem explicar cenário e resultado, por exemplo `shouldRejectRevokedSession`, `shouldNotWriteOutsideWorkspace` e `shouldRejectApprovalFromAnotherUser`. Cada teste deve possuir uma única razão principal de falha e dados mínimos.

Fakes devem ser determinísticos e não depender de Ollama, internet, relógio real ou filesystem global. Quando tempo é relevante, use clock injetável. Quando UUID ou idempotência é relevante, use valores controlados no fixture.

## Cobertura mínima por área

### Autenticação

Testar login válido, senha inválida, usuário inexistente, sessão expirada, JWT inválido, sessão revogada, refresh válido, refresh reutilizado, logout e isolamento por usuário. Testar também que tokens não aparecem em respostas de erro ou logs capturados.

### MX Core e runs

Testar criação do run, correlação, roteamento, transições válidas e inválidas, resposta vazia, falha do modelo, erro de skill, persistência de timestamps e escopo por usuário. Streaming deve testar `started`, múltiplos `token`, `completed`, `error` e falha no meio do fluxo.

### Skills

Cada skill deve declarar contrato, versão, escopo, autonomia e ferramentas permitidas. Testar seleção determinística, fallback, baixa confiança, input ambíguo, resposta fora do contrato e recusa de autonomia não declarada.

### Tools e aprovação

Testar registry, schema de argumentos, `ALLOW`, `DENY` e `REQUIRE_APPROVAL`, traversal, path absoluto, symlink/junction, limite de bytes, escrita atômica, tool ausente, erro de filesystem, cross-user, expiração, nonce reutilizado, mutação de argumentos e corrida entre approve/reject.

O teste mais importante da ponte P3 é provar que `REQUIRE_APPROVAL` não executa a tool nem altera o workspace antes de um approval válido associado ao run correto.

### Cliente Expo

Executar typecheck e testar adapters de sessão web/native, bootstrap com sessão persistida, refresh deduplicado, logout, parser SSE, concatenação de deltas e tratamento de erro. Testes de interface devem cobrir loading, vazio, offline, `401`, erro do modelo e run pendente.

## Comandos de validação

Backend no Windows:

```powershell
cd D:\MX\core-service
.\mvnw.cmd -Dmx.build.directory=C:\Windows\Temp\mx-target `
  -Dmaven.repo.local=C:\Windows\Temp\mx-m2 test
```

Cliente:

```powershell
cd D:\MX\clients\mx-app
npm run typecheck
npx expo export --platform web
```

Antes do commit, também revisar:

```powershell
cd D:\MX
git diff --check
git status --short
git diff --cached --name-only
```

## Critérios de aprovação

Uma mudança pode ser integrada quando compila, os testes relevantes passam, o contrato público está documentado, os erros são controlados, não há segredo ou build gerado no índice, e a revisão confirma dependências apontando para dentro. Mudanças de segurança ou autonomia exigem pelo menos um teste de regressão negativo e atualização do threat model ou ADR correspondente.

O número de testes é um indicador, não o objetivo. A qualidade é medida pela capacidade de impedir regressões reais: acesso cruzado, execução sem aprovação, duplicação em retry, prompt injection e falhas de infraestrutura.
