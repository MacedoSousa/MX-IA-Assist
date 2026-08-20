# Changelog

## [Unreleased] — 2026-08

### Added

- MX Core como único ponto de comunicação entre cliente e skills internas.
- Autenticação JWT associada a sessão persistida e revogável.
- Refresh token rotativo com hash SHA-256 e cliente Expo com refresh deduplicado.
- Lifecycle de `ExecutionRun` com `runId`, `correlationId`, consulta protegida e estados `AWAITING_APPROVAL` e `CANCELLED`.
- Streaming SSE com eventos `started`, `token`, `completed` e `error`.
- Suporte de streaming nas skills `general` e `development`.
- Cliente Expo universal para web, Android e iOS, com armazenamento de sessão por plataforma.
- Casos de uso e endpoints de aprovação/rejeição de runs.
- `WorkspaceWriteTool` com sandbox, bloqueio de traversal/symlink, escrita atômica e limite de bytes.
- Documentação de arquitetura, streaming, tools, segurança, operação, testes, ADRs e contribuição.
- Regras de `.gitignore` para builds, dependências, segredos, modelos, logs e artefatos locais.

### Changed

- README e resumo de implementação reescritos para remover a descrição da SPA legada como produto oficial.
- Backlog reconciliado com a implementação P0–P3 e com a lacuna `ToolExecutor → ExecutionRun`.
- Documentação multiplataforma atualizada para `clients/mx-app` como cliente oficial atual.

### Known limitations

- `REQUIRE_APPROVAL` ainda precisa criar automaticamente um `ExecutionRun` persistido em `AWAITING_APPROVAL`.
- A UX visual de histórico, painel de run e aprovação ainda é mínima no shell Expo atual.
- Reconexão SSE com cursor, idempotência e sincronização cross-channel ainda estão abertas.
- Threat model executável, testes E2E/adversariais, retenção, backup, benchmark e timeout robusto do Ollama ainda precisam ser concluídos.

### Validation baseline

- Backend Maven: 62 testes, 0 falhas no ambiente de desenvolvimento.
- Cliente TypeScript: `npm run typecheck` aprovado.
- Exportação web: `npx expo export --platform web` aprovado.

## Histórico anterior

Versões anteriores continham uma SPA HTML/JavaScript e contratos legados. Esse material continua na árvore como compatibilidade ou referência histórica, mas não representa a fonte oficial do cliente multiplataforma atual.
