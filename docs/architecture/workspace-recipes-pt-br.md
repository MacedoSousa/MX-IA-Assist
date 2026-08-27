# Receitas governadas de workspace

## Capacidades entregues

O MX passou a oferecer quatro operações de workspace **nomeadas e aprováveis**. Elas não aceitam comandos, URLs, portas ou caminhos fornecidos livremente pelo modelo.

| Tool | Entrada permitida | Efeito após aprovação | Limites aplicados |
|---|---|---|---|
| `workspace.initialize_static_project` | `project`: slug minúsculo com até 48 caracteres | Cria HTML, CSS e README mínimos em `D:\MX\workspaces\<project>` | Nome validado; criação apenas se o projeto não existir; sem links simbólicos |
| `workspace.preview_static` | `project`: slug de um projeto criado | Enfileira uma receita `static-http` para o executor local | Somente `127.0.0.1`; portas `48000–48099`; exige `index.html`; não inicia processo pelo Core |
| `workspace.validate_static` | `project`: slug de projeto estático existente | Enfileira a receita `static-validate` | Perfil único `static-html-v1`; HTML declarado; até 500 arquivos, 5 MiB e 20 s; resultado e hash do `index.html` |
| `workspace.build_static` | `project`: slug de projeto estático existente | Enfileira a receita `static-build` em staging | Mesmo perfil e limites; cópia somente para `.mx/builds/<requestId>` com manifesto SHA-256; sem publicação |

A skill de desenvolvimento pode apenas **propor** essas operações. O usuário precisa aprovar a run correspondente, com nonce válido, antes de a tool ser executada e de a fila de preview ser escrita.

## Executor local de preview

O `scripts/mx_evolution_runner.py` possui três operações explícitas:

```text
python scripts/mx_evolution_runner.py --workspace-preview-once
python scripts/mx_evolution_runner.py --stop-workspace-preview <request-id>
python scripts/mx_evolution_runner.py --workspace-recipe-once
```

O executor aceita exclusivamente a receita JSON persistida pelo MX, revalida slug, raiz e ausência de links simbólicos e inicia o servidor HTTP padrão do Python com host fixo `127.0.0.1`. Ele seleciona somente uma porta livre da faixa reservada, registra PID, URL, data e log sob `.mx/preview-requests/` e permite interromper o mesmo processo pelo identificador da solicitação.

> O preview é propositalmente local. Ele não é publicado na internet, não é exposto na rede LAN nem inicia `npm`, Docker ou código arbitrário do projeto.

As receitas `static-validate` e `static-build` não chamam shell, Node, npm, Docker ou binários escolhidos pelo modelo. Elas revalidam a estrutura HTML, recusam links simbólicos, limitam arquivo/volume/tempo e, no build, copiam apenas o projeto validado para uma área de staging rastreável. A execução deve ser disparada explicitamente pelo operador local depois da aprovação da run; nenhum modo recorrente é iniciado pela tool.

## Evidências

Os testes `WorkspaceStaticProjectToolTest` cobrem criação válida, rejeição de tentativa de escape e enfileiramento de preview. `scripts/test_mx_evolution_runner.py` cobre JSON inválido, traversal, host remoto, faixa de portas alterada, início/parada do preview, recipe desconhecida e a validação/build estáticos em staging. A suíte completa do Core foi executada após a implementação.

## Evolução controlada

Perfis adicionais só podem ser adicionados individualmente, com contrato, teste, allowlist, limite de recurso e aprovação. O modelo não poderá passar strings de shell, escolher binários, definir portas públicas, acessar caminhos fora do workspace, publicar alterações ou iniciar tarefas recorrentes por conta própria.
