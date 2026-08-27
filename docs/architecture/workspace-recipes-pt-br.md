# Receitas governadas de workspace

## Capacidades entregues

O MX passou a oferecer duas operações de workspace **nomeadas e aprováveis**. Elas não aceitam comandos, URLs, portas ou caminhos fornecidos livremente pelo modelo.

| Tool | Entrada permitida | Efeito após aprovação | Limites aplicados |
|---|---|---|---|
| `workspace.initialize_static_project` | `project`: slug minúsculo com até 48 caracteres | Cria HTML, CSS e README mínimos em `D:\MX\workspaces\<project>` | Nome validado; criação apenas se o projeto não existir; sem links simbólicos |
| `workspace.preview_static` | `project`: slug de um projeto criado | Enfileira uma receita `static-http` para o executor local | Somente `127.0.0.1`; portas `48000–48099`; exige `index.html`; não inicia processo pelo Core |

A skill de desenvolvimento pode apenas **propor** essas operações. O usuário precisa aprovar a run correspondente, com nonce válido, antes de a tool ser executada e de a fila de preview ser escrita.

## Executor local de preview

O `scripts/mx_evolution_runner.py` foi estendido com dois modos explícitos:

```text
python scripts/mx_evolution_runner.py --workspace-preview-once
python scripts/mx_evolution_runner.py --stop-workspace-preview <request-id>
```

O executor aceita exclusivamente a receita JSON persistida pelo MX, revalida slug, raiz e ausência de links simbólicos e inicia o servidor HTTP padrão do Python com host fixo `127.0.0.1`. Ele seleciona somente uma porta livre da faixa reservada, registra PID, URL, data e log sob `.mx/preview-requests/` e permite interromper o mesmo processo pelo identificador da solicitação.

> O preview é propositalmente local. Ele não é publicado na internet, não é exposto na rede LAN nem inicia `npm`, Docker ou código arbitrário do projeto.

## Evidências

Os testes `WorkspaceStaticProjectToolTest` cobrem criação válida, rejeição de tentativa de escape e enfileiramento de preview. A prova executada pelo executor iniciou um projeto estático descartável em `127.0.0.1:48000`, confirmou a resposta HTTP e encerrou o processo pelo `requestId`. A suíte completa do Core foi executada após a implementação.

## Evolução controlada

As próximas receitas devem ser adicionadas individualmente com contrato, teste, allowlist e aprovação: validação estática, build de projeto e preview de perfis explicitamente suportados. O modelo não poderá passar strings de shell, escolher binários, definir portas públicas, acessar caminhos fora do workspace, publicar alterações ou iniciar tarefas recorrentes por conta própria.
