# Login web e uso do conhecimento de estudos

## Acesso web

O cliente web do MX deve chamar a API pelo mesmo domínio que entregou a aplicação quando `EXPO_PUBLIC_MX_API_URL` não estiver configurada. Essa escolha é necessária para acesso remoto pelo Tailscale: em um celular, `localhost` representa o próprio celular, não o computador que executa o MX.

O Nginx do `mx-web` encaminha as rotas `/api/` para `mx-core:8080` e mantém o streaming SSE sem buffering. Assim, o navegador pode abrir `https://mx-ai.taila61bd3.ts.net/` e usar a mesma origem para login, sessão, chat, aprovações e consultas de execução. O perfil `dev` do MX Core também autoriza explicitamente `https://mx-ai.taila61bd3.ts.net` no CORS; sem essa origem, navegadores enviam `Origin` e o Spring Security pode responder `403`. Para integrações específicas, `EXPO_PUBLIC_MX_API_URL` ainda pode ser definido explicitamente durante o build.

## Conhecimento importado

O pacote em `knowledge/estudos/`, `skills/estudos/ai-assistant/` e `evaluation/estudos/` é uma biblioteca documental versionada. A síntese em `knowledge/estudos/sintese-assistente.md` resume os domínios e sugere linhas de raciocínio, enquanto o recurso em `core-service/src/main/resources/knowledge/estudos/sintese-assistente.md` fornece esse contexto à `GeneralSkill` em runtime.

A síntese não deve ser copiada como resposta automática. O MX deve selecionar o trecho pertinente, combinar a orientação com a pergunta e com evidências do projeto, diferenciar fato de hipótese e recomendação, declarar incerteza e preferir a skill especialista quando o assunto exigir profundidade. Um documento recuperado nunca autoriza ignorar autenticação, allowlist, sandbox, PolicyEngine, aprovação humana ou qualquer política do núcleo.

O ciclo recomendado é **ler, interpretar, aplicar, testar, avaliar e registrar**. A evolução permanente depende de alterações versionadas, casos de avaliação, revisão humana e auditoria; a simples leitura dos arquivos não treina os pesos do Ollama.

## Diagnóstico de chat offline

Se o login funcionar, mas uma mensagem simples fizer o cliente mostrar `Offline`, verificar o streaming SSE antes de trocar credenciais. O cliente deve interpretar quebras de linha reais nos eventos `started`, `token` e `completed`. O MX Core também permite o `DispatcherType.ASYNC` do `SseEmitter` depois da autenticação inicial; sem essa permissão, o Spring Security pode registrar `AuthorizationDeniedException` após a resposta já ter sido iniciada.

A validação mínima é executar uma mensagem curta pelo Core local e pelo endpoint Tailscale, confirmando `HTTP 200`, um evento `started`, pelo menos um evento `token`, um evento `completed` e zero eventos `error`. A evidência correspondente está em `docs/evidence/executions/learning-login-synthesis/chat-stream-check.md`.
