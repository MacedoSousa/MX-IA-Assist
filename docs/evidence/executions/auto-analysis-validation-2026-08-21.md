# Evidência operacional — CRUD, conhecimento incremental e autoanálise do MX

**Data:** 2026-08-21
**Repositório:** `MacedoSousa/MX-IA-Assist`
**Commits principais:** `b276c1b` e `659e109`

## Validações executadas

| Área | Execução | Resultado |
|---|---|---|
| Cliente Expo | `npm run typecheck` | Passou sem erros |
| Bundle web | `npx expo export --platform web` no Windows | Gerado e servido pelo `mx-web` |
| CRUD web | Bundle contém `Projetos`, `Criar`, `Renomear`, `Arquivar`, `Excluir` e `Lixeira` | Confirmado no arquivo servido pelo Nginx |
| Testes backend | `bash ./mvnw -q test` | 115 testes, 0 falhas, 0 erros, 0 ignorados |
| Flyway | Inicialização do `mx-core` | 18 migrations validadas; V17 aplicada com sucesso |
| Core | `docker ps` | `mx-core` saudável em `8080` |
| Web | `docker ps` | `mx-web` ativo em `8082` |
| Ollama | `docker ps` | `mx-ollama` ativo em `11434` |
| Persistência | Compose | Auditoria runtime montada em `data/knowledge` |

## Autoanálise segura

A `GeneralSkill` agora consulta a cobertura lexical local antes de recorrer à internet. A busca externa é desligável, limitada por quantidade de resultados, restrita ao endpoint HTTPS allowlisted do DuckDuckGo Instant Answer e protegida por timeout. A consulta enviada é normalizada e remove e-mails, URLs, tokens Bearer e padrões explícitos de senha, segredo e chave de API.

Os resultados externos são dados não privilegiados, não são seguidos como URLs e entram apenas como chunks runtime deduplicados por SHA-256. A auditoria JSONL registra o hash da pergunta, score de cobertura, quantidade de resultados e motivo da decisão, sem persistir a pergunta bruta. Falhas do provedor externo não derrubam a resposta local.

## Conhecimento Alura

O bootstrap incremental incorporou 73 registros selecionados, 15 dossiês acadêmicos, inventário e classificação de direitos. O índice empacotado do `core-service` foi reconstruído com 390 chunks. A importação não altera pesos do Ollama e não executa instruções encontradas nos materiais.

## Operação no Windows

A primeira reconstrução do `mx-core` revelou que o clone Windows ainda não continha a migration V17. O arquivo foi sincronizado, a imagem foi reconstruída e o banco aplicou a migration com sucesso, avançando do schema 16 para o schema 17. O `mx-core` terminou em estado `healthy`; nenhum serviço preservado foi removido.

## English summary

The MX universal client was type-checked and its web bundle was rebuilt and served by `mx-web`. The complete Maven suite passed with 115 tests and no failures, errors, or skipped tests. Flyway validated 18 migrations and applied V17 successfully in the Windows PostgreSQL instance. The self-analysis loop uses local coverage first, sanitized and bounded DuckDuckGo fallback only when needed, SHA-256 deduplication, non-privileged evidence, and hash-only JSONL auditing. The Alura bootstrap imported 73 selected records and 15 academic dossiers, rebuilding the packaged index to 390 chunks without changing model weights or executing imported instructions.
