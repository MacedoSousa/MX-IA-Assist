# Módulo 03 — Spring AI, custos e observabilidade

## Fonte
Curso da Alura: [Spring AI: integre uma aplicação Spring com a OpenAI](https://cursos.alura.com.br/course/spring-ai-integre-aplicacao-spring-openai). A página informa nível básico, carga horária de 8 horas, transcrição integral e atualização em 28/01/2026.

## Conteúdo observado
O curso cobre criação de aplicação Java com Spring AI, integração com API externa, proteção da chave por variáveis de ambiente, análise de sentimentos, engenharia de prompt, parâmetros, diferenciação de modelos, contagem de tokens, seleção dinâmica de modelo, processamento em lote, tratamento de erros, retry, logs, monitoramento, multimodalidade e integração com outras APIs.

## Conhecimento aplicado ao MX
Este curso confirma que o backend Java do projeto deve tratar o modelo como dependência externa substituível. A configuração deve separar credenciais, endpoint, modelo, limites e parâmetros. A camada de serviço deve calcular ou estimar consumo de tokens, aplicar limites por usuário e operação, e registrar telemetria sem registrar prompts sensíveis ou chaves.

## Skill proposta: chamada de modelo em Spring

**Entrada:** prompt validado, contexto limitado, modelo permitido, temperatura ou parâmetros equivalentes, timeout e política de retry.

**Processo:** carregar configuração segura; validar tamanho e formato; selecionar modelo conforme tarefa; enviar requisição; tratar timeout, limite de requisição, autenticação e erro transitório; registrar métrica e correlação; validar resposta.

**Saída:** resposta normalizada para o domínio da aplicação, consumo estimado, latência, identificador de correlação e status operacional.

**Limites:** retry somente para falhas transitórias e com backoff; não repetir operações não idempotentes sem chave de idempotência; não expor conteúdo sensível em logs; não permitir que o modelo escolhido pelo usuário ignore a política do sistema.

## Padrão de observabilidade inicial
Os logs devem ser estruturados e conter `trace_id`, `conversation_id`, `model_id`, `operation`, `status`, `latency_ms`, `input_size`, `output_size`, `retry_count` e `error_type`. O conteúdo integral de prompt e resposta deve ser omitido ou mascarado por padrão. Métricas essenciais são latência p50/p95, taxa de erro por tipo, timeouts, retries, tamanho de contexto, consumo de tokens quando disponível, respostas rejeitadas na validação e custo estimado por operação.

## Relação com métricas, logs e monitoramento solicitados
Este curso é o primeiro elo direto entre IA e observabilidade. Ele deve ser combinado posteriormente com tracing distribuído, alertas, SLI/SLO, dashboards e monitoramento de qualidade do modelo. Métricas técnicas, de custo e de qualidade devem ser separadas para evitar que uma resposta rápida seja considerada boa quando está incorreta.

## Referências
[1]: https://cursos.alura.com.br/course/spring-ai-integre-aplicacao-spring-openai "Curso da Alura — Spring AI: integre uma aplicação Spring com a OpenAI"
