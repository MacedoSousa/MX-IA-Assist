# Módulo 08 — LangGraph, estado, HITL e multiagentes

## Fonte
[LangGraph: Orquestrando agentes e multiagentes](https://cursos.alura.com.br/course/langgraph-multiagentes). A página apresenta um curso avançado com foco em pensamento e ação, grafos de decisão, nós condicionais, SQLite, persistência, streaming, Human in the Loop, scraping e orquestração multiagente.

## Conteúdo observado
O curso trabalha modelo ReAct, funções, agentes em loop, componentes LangGraph, busca com Tavily, buscas simples com DDGS, scraping, persistência, streaming, memória e contexto, aprovação humana, snapshots, prompts e nós de multiagentes, além de um assistente de e-mail e finalização de projeto.

## Modelo de estado para o MX
O estado deve ser explícito e versionado. Campos recomendados: `conversation_id`, `request_id`, `user_intent`, `messages_summary`, `retrieved_context_ids`, `tool_plan`, `tool_results`, `authorization_status`, `human_approval_required`, `current_node`, `retry_count`, `created_at` e `updated_at`. O estado não deve conter segredos nem depender apenas da memória do processo.

## Grafo recomendado
`classify -> retrieve? -> plan -> authorize -> execute_tool? -> validate -> respond`, com ramos de `human_approval`, `retry_safe`, `fallback_model`, `abstain` e `error`. Cada nó deve ter uma responsabilidade e contrato testável. O MX Core pode persistir checkpoints e o orquestrador pode ser implementado em Java ou isolado em Python, desde que o contrato seja estável.

## Human in the Loop
A aprovação humana deve ser um estado persistido, não apenas uma pausa na memória do processo. O pedido deve apresentar ação, parâmetros, risco, validade e origem. Após aprovação, o sistema revalida autorização, versão do estado e idempotência antes de executar. Rejeição, expiração e alteração do pedido devem encerrar o fluxo com auditoria.

## Streaming
Streaming é uma sequência de eventos, não uma resposta parcialmente executada. Eventos mínimos: `run_started`, `token_delta`, `retrieval`, `tool_proposed`, `approval_requested`, `tool_result`, `validation`, `run_completed` e `run_failed`. Uma ferramenta sensível só pode ser executada após o evento de aprovação confirmado.

## Multiagentes
Use múltiplos agentes apenas quando a divisão reduzir complexidade ou melhorar uma métrica. Cada agente deve ter função, entrada, saída e ferramentas delimitadas. Um supervisor deve controlar orçamento de tokens, número de ciclos, conflitos e encerramento. Para o assistente local, uma composição inicial segura é: agente de recuperação, agente de planejamento e agente de resposta; ferramentas permanecem sob controle do runtime.

## Skill derivada
**Orquestrar fluxo com estado persistido:** carregar checkpoint; validar versão; executar o próximo nó; registrar evento; persistir estado; interromper para aprovação quando necessário; retomar apenas com autorização válida; finalizar com resposta validada ou abstention.

## Referências
[1]: https://cursos.alura.com.br/course/langgraph-multiagentes "Curso da Alura — LangGraph: Orquestrando agentes e multiagentes"
