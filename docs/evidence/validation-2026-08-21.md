# MX Validation Evidence — 2026-08-21

## Português

Esta evidência registra a retomada do projeto no clone `/home/ubuntu/mx-commit`, evitando o erro anterior do CMD/Windows durante a execução do Maven Wrapper.

| Verificação | Comando | Resultado |
|---|---|---|
| Testes direcionados do expansor e especialistas | `cd core-service && bash ./mvnw -q -Dtest=KnowledgeQueryExpanderTest,StudyKnowledgeContextTest,DomainSpecialistSkillTest,SpecialistRoutingTest,SkillRegistryRouterTest test` | Aprovado |
| Suíte completa do backend | `cd core-service && bash ./mvnw -q test` | **104 testes, 0 falhas, 0 erros, 0 ignorados** |
| TypeScript do cliente | `cd clients/mx-app && npm run typecheck` | Aprovado |
| Exportação web Expo | `cd clients/mx-app && npx expo export --platform web` | Aprovado; diretório `dist` gerado |
| Configuração Compose no sandbox | `docker` não disponível no sandbox | Validada no Docker Desktop do Windows |
| Configuração Compose no Windows | `docker-compose.exe config` | Aprovado |

O comando `docker compose config --quiet` não foi usado como evidência porque a instalação do Windows expõe o executável legado `docker-compose.exe` e o parâmetro `--quiet` não é aceito pela versão disponível. A validação final foi feita com `docker-compose.exe config`, que retornou sucesso.

Os testes adicionados cobrem o contrato das novas `InfrastructureSkill`, `DataSkill` e `TeachingSkill`, a presença de gatilhos por domínio, o uso do contexto local de estudos e o roteamento pelo `SkillRouter`. Os testes do `KnowledgeQueryExpander` também confirmam que tokens curtos como `ml` não entram no conjunto tokenizado e que `retrieval` é uma expansão válida para consultas de busca.

A operação always-on permanece baseada em `restart: unless-stopped`, volumes persistentes, health checks e dependências condicionais no Compose. O Docker Desktop local não fornece autoscaling horizontal real; o projeto usa limites configuráveis, paralelismo limitado do Ollama, fila máxima, keep-alive, cache KV e uma única instância carregada como mecanismos adequados ao ambiente local.

## English

This evidence records the project resumption in `/home/ubuntu/mx-commit`, avoiding the previous CMD/Windows execution issue while running the Maven Wrapper.

| Check | Command | Result |
|---|---|---|
| Focused expander and specialist tests | `cd core-service && bash ./mvnw -q -Dtest=KnowledgeQueryExpanderTest,StudyKnowledgeContextTest,DomainSpecialistSkillTest,SpecialistRoutingTest,SkillRegistryRouterTest test` | Passed |
| Full backend suite | `cd core-service && bash ./mvnw -q test` | **104 tests, 0 failures, 0 errors, 0 skipped** |
| Client TypeScript | `cd clients/mx-app && npm run typecheck` | Passed |
| Expo web export | `cd clients/mx-app && npx expo export --platform web` | Passed; `dist` generated |
| Compose configuration in sandbox | `docker` unavailable in sandbox | Validated on Windows Docker Desktop |
| Compose configuration on Windows | `docker-compose.exe config` | Passed |

The old Windows Compose executable was used because the installed Docker CLI does not expose the `docker compose` subcommand and the `--quiet` flag is unsupported. The final `docker-compose.exe config` validation returned success.

The new tests cover the contracts of `InfrastructureSkill`, `DataSkill` and `TeachingSkill`, domain triggers, local study-context usage and `SkillRouter` selection. `KnowledgeQueryExpanderTest` also confirms that short tokens such as `ml` are not emitted by the tokenizer and that `retrieval` is a valid expansion for search queries.

Always-on operation remains based on `restart: unless-stopped`, persistent volumes, health checks and conditional service dependencies. Local Docker Desktop does not provide real horizontal autoscaling; the project instead uses configurable resource limits, bounded Ollama parallelism, queue limits, keep-alive, KV cache and one loaded model as local-machine controls.

## Related references

- [Ollama performance notes](../architecture/ollama-performance-notes.md)
- [MX history, topic and adaptive learning architecture](../architecture/mx-history-topic-learning.md)
- [Alura learning evidence](./alura-reference-aug-2026.md)
