# MX Self-Extension Architecture

## Objetivo

O MX poderá propor e executar sua própria evolução criando **agents, skills e tools** como arquivos versionáveis do projeto. O fluxo não entrega ao modelo um shell livre nem o socket Docker. O core gera um plano declarativo em uma fila persistente; um runner local separado aplica o plano, executa somente validações conhecidas, desfaz alterações em caso de falha e cria um commit Git auditável.

> O modelo propõe código; políticas determinísticas decidem quais arquivos, validações e efeitos podem ser executados.

## Fluxo

| Etapa | Responsável | Regra |
|---|---|---|
| 1. Solicitação | Usuário e `SelfImprovementSkill` | A solicitação é roteada por gatilhos como “criar agent”, “criar skill”, “criar ferramenta” ou “melhorar o MX”. |
| 2. Geração | Ollama local | O modelo produz exatamente uma chamada `self_extension.submit` com arquivos e nomes de validações, sem comandos shell arbitrários. |
| 3. Política | MX Core | Valida limites de tamanho, tipo (`AGENT`, `SKILL`, `TOOL`), caminhos permitidos, slug, mensagem de commit e estado habilitado. |
| 4. Fila | `SelfExtensionJobStore` | Grava JSON atômico em `data/evolution/jobs/pending`, montado no core como `/app/workspace/evolution`. |
| 5. Execução host-side | `scripts/mx_evolution_runner.py` | Rejeita worktree sujo, aplica arquivos, executa testes/builds declarativos e restaura o estado anterior se qualquer etapa falhar. |
| 6. Commit | Runner local | Adiciona apenas os caminhos declarados e a evidência gerada. Push remoto é opt-in e permanece desligado por padrão. |
| 7. Auditoria | Runner | Move o job para `completed` ou `failed` e grava status, hashes, comandos mapeados, duração e commit. |

## Allowlist de arquivos

O runner aceita somente caminhos relativos dentro dos prefixos `core-service/src/main/`, `core-service/src/test/`, `clients/mx-app/`, `skills/`, `scripts/`, `docs/` e `infrastructure/docker/`. Caminhos absolutos, `..`, `.git`, `.env`, chaves, certificados, tokens e diretórios de dados são sempre recusados.

A lista de validações é fechada. São suportados `maven_test`, `web_typecheck`, `web_export`, `compose_config`, `docker_build_core` e `docker_build_web`. O modelo nunca escolhe um comando shell; escolhe apenas identificadores que o runner traduz para comandos fixos por plataforma.

## Rollback e pré-condições

O runner exige um worktree limpo antes de modificar qualquer arquivo. Ele captura o conteúdo original dos caminhos declarados, aplica os arquivos em UTF-8, executa todas as validações e restaura exatamente o conteúdo anterior quando uma etapa falha. Não usa `git reset --hard`, não remove containers não relacionados e não opera em `data/`, `.git/` ou arquivos de segredo.

A mensagem de commit é normalizada para uma única linha e limitada a 120 caracteres. O runner cria commits locais com os caminhos explícitos. `MX_EVOLUTION_ALLOW_PUSH=true` é necessário para qualquer push e a opção enviada no job também precisa estar habilitada.

## Contrato da chamada

A `SelfImprovementSkill` só aceita o protocolo fenced `[MX_TOOL_CALL]...[/MX_TOOL_CALL]`, com `toolName` igual a `self_extension.submit`. O objeto `arguments` contém `type`, `slug`, `description`, `files`, `validations`, `commitMessage` e `allowPush`. Cada item de `files` possui somente `path` e `content`; não existe campo para comando shell. O runner calcula SHA-256 dos conteúdos declarados e repete as verificações de caminho antes de escrever.

## Execução contínua no Windows

O `mx-core` apenas enfileira jobs. O processo `scripts/mx_evolution_runner.py` deve permanecer ativo na máquina Windows junto do MX, por exemplo iniciado por `scripts\\run_evolution.bat` ou pelo Agendador de Tarefas do Windows. Ele usa o Docker CLI do host, o Git do clone `D:\\MX` e o mesmo workspace do projeto, sem conceder acesso ao daemon Docker ao container Java.

Para executar uma rodada única no clone, use `python scripts\\mx_evolution_runner.py --once --root D:\\MX`. Para verificar uma fila sem alterar arquivos, use `--dry-run`; o runner ainda exige worktree limpo. Para operação contínua, use `scripts\\run_evolution.bat`. Em Linux, o equivalente é `scripts/run_evolution.sh`.

O commit é local por padrão. O job deve manter `allowPush: false`; mesmo que essa opção seja alterada no JSON, o runner rejeita push sem `MX_EVOLUTION_ALLOW_PUSH=true`, e o core também mantém `MX_EVOLUTION_ALLOW_PUSH_REQUESTS=false` por padrão. A habilitação de push deve ser uma decisão operacional separada, nunca uma instrução produzida pelo modelo.

## English summary

MX can propose and implement new agents, skills, and tools through a declarative self-extension queue. Ollama generates a bounded plan, the core validates it, and a host-side runner applies only allowlisted files, maps a closed set of validation identifiers to fixed Maven/npm/Docker commands, rolls back on failure, and creates an auditable local Git commit. Remote push is disabled by default and requires an explicit environment flag. The runner must be kept alive on the local Windows machine; the Java container never receives unrestricted shell access or the Docker socket.
