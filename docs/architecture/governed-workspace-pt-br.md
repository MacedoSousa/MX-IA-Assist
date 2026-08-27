# Workspace governado e execução após aprovação

## Princípio operacional

O modelo nunca recebe terminal livre, caminho absoluto arbitrário, credenciais, autorização de publicação ou permissão para iniciar processos diretamente. A comunicação de criação e alteração passa pelo MX Core e por tools registradas; comandos de host continuam reservados a executores locais de receitas declarativas.

| Etapa | Responsabilidade | Garantia |
|---|---|---|
| Proposta | Skill produz uma chamada estruturada para uma tool permitida | Não executa a escrita |
| Política | `PolicyEngine` valida nome, argumentos, autonomia e allowlist da skill | Operação sensível exige aprovação |
| Pendência | `ToolExecutor` registra tool, argumentos JSON, nonce com hash e expiração | O pedido não pode ser alterado no painel |
| Aprovação | Usuário autenticado apresenta o nonce para a execução de sua própria run | Ownership, uso único e expiração são verificados |
| Retomada | `ApprovedToolExecutionUseCase` desserializa somente os argumentos persistidos e chama a tool registrada | Não aceita shell, comando ou argumentos novos |
| Evidência | A run passa por verificação e termina com o resultado serializado da tool | Histórico auditável no endpoint de runs |

## Correção implementada

Anteriormente, aprovar uma run apenas movia seu estado de `AWAITING_APPROVAL` para `EXECUTING`; a ação pendente não era retomada. O MX agora retoma uma tool somente depois de aprovação válida e persiste a run como `COMPLETED` ou `FAILED`. Os argumentos pendentes passaram de uma representação textual ambígua para JSON estruturado antes de serem guardados.

O primeiro caso exercitado é `workspace.write_file`, que já restringe a escrita à raiz do workspace, bloqueia caminhos absolutos, travessia, NUL e links simbólicos, limita tamanho e utiliza substituição atômica. A ferramenta continua exigindo `EXECUTE_WITH_APPROVAL`.

> A aprovação não autoriza uma nova intenção. Ela consome apenas o nome e os argumentos que foram registrados antes do consentimento; qualquer argumento inválido, tool inexistente ou falha de execução termina em estado de erro auditável.

## Cobertura e próximos passos

`ApprovedToolExecutionUseCaseTest` comprova que uma tool controlada não roda antes da aprovação e só se torna concluída depois de nonce e consentimento válidos. `ToolExecutorTest`, `WorkspaceWriteToolPolicyTest`, `DevelopmentSkillTest` e os testes do controlador preservam as barreiras existentes.

Ainda não existe terminal de propósito geral, execução de scripts de projeto, publicação, preview público ou permissão para navegar fora de `D:\MX\workspaces`. O próximo incremento deve introduzir receitas nomeadas para inicialização, validação estática e preview local em `127.0.0.1`, com filas host-side, portas temporárias, logs e encerramento explícito. Qualquer receita que execute código de um projeto continuará exigindo perfil permitido, teste em área de staging e aprovação humana.
