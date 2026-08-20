# Monitoramento e operação do MX

## Objetivo

O monitoramento do MX deve responder rapidamente a quatro perguntas: o serviço está disponível, o MX está roteando para a skill correta, as skills estão concluindo com qualidade e qual etapa está causando latência ou falha.

## Endpoints locais

| Endpoint | Finalidade | Exposição inicial |
|---|---|---|
| `/actuator/health` | Disponibilidade e dependências essenciais | Desenvolvimento |
| `/actuator/info` | Identificação da aplicação e versão | Desenvolvimento |
| `/actuator/metrics` | Catálogo de métricas Micrometer | Desenvolvimento |
| `/actuator/metrics/mx.skill.route` | Contagem de roteamentos por skill e resultado | Desenvolvimento |
| `/actuator/metrics/mx.skill.execution` | Contagem de conclusões e falhas por skill | Desenvolvimento |

Em ambientes fora do desenvolvimento, os endpoints devem permanecer protegidos pela configuração de segurança. O endpoint de métricas não deve ser exposto publicamente sem autenticação.

## Métricas mínimas

| Métrica | Tags | Interpretação |
|---|---|---|
| `mx.skill.route` | `skill`, `outcome` | Quantidade de solicitações direcionadas a cada skill |
| `mx.skill.execution` | `skill`, `outcome` | Execuções concluídas ou falhas por skill |
| `http.server.requests` | tags padrão do Spring | Latência e taxa de erro da API |
| `jvm.memory.used` | tags padrão da JVM | Pressão de memória do processo |

O núcleo deve manter as tags de baixa cardinalidade. Não devem ser usadas prompt, texto de resposta, ID de usuário ou exceção completa como tag de métrica. Esses dados pertencem ao log estruturado ou à auditoria com política de retenção.

## Correlação e auditoria

Cada interação deve possuir `correlationId`. O ID deve aparecer na resposta interna, nos logs de início e fim, no registro de skill e em falhas. A auditoria deve registrar somente o necessário: usuário, skill, versão, decisão, ferramentas propostas, aprovações, estado final, duração e código de erro. Prompts e respostas podem conter dados pessoais e devem obedecer à política de retenção do MX.

## Logs recomendados

Os eventos principais são `mx.request.received`, `mx.skill.routed`, `mx.skill.started`, `mx.skill.completed`, `mx.skill.failed`, `mx.tool.approval_required` e `mx.request.completed`. Logs devem ter nível `INFO` para transições normais e `WARN` ou `ERROR` para falhas, sempre com `correlationId` e sem segredos.

## Alertas locais

Na primeira versão, o operador deve observar falhas consecutivas da skill, crescimento da latência do modelo, indisponibilidade do Ollama, aumento de respostas vazias e consumo de memória. A política inicial é registrar e responder com erro seguro; retries e circuit breaker devem ser adicionados no gateway de modelo, não dentro de cada skill.

## Definition of Done operacional

Uma skill só é considerada pronta quando aparece no registry, possui métricas de roteamento e execução, pode ser identificada por `correlationId`, possui teste de sucesso e falha, não executa ferramentas fora da sua allowlist e deixa uma explicação suficiente para o operador entender por que foi escolhida.
