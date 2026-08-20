# Skill: Agente com ferramentas

## Objetivo
Planejar e executar fluxos de IA que usam ferramentas externas sem permitir que o modelo ultrapasse permissões ou limites do sistema.

## Entradas
Intenção, estado da sessão, ferramentas permitidas, esquemas de entrada, política de autorização e necessidade de confirmação humana.

## Procedimento
Classifique a tarefa. Escolha um fluxo explícito. Valide os argumentos contra o esquema. Verifique autorização. Execute com timeout, retry apenas quando seguro, idempotência e limite de iterações. Valide o resultado, atualize o estado e produza eventos de auditoria.

## Saídas
Resultado, ferramentas chamadas, decisão de autorização, duração, erros, estado final e correlação.

## Restrições
O modelo não concede permissões. Ações com efeitos externos exigem confirmação quando forem sensíveis. Não permitir loops ilimitados. Não repetir operação não idempotente automaticamente. Interromper quando faltar evidência ou autorização.

## Avaliação
Testar argumentos inválidos, ferramenta indisponível, timeout, repetição de evento, prompt injection, acesso não autorizado e falha parcial.
