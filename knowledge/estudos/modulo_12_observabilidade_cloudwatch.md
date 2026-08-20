# Módulo 12 — Observabilidade com AWS CloudWatch

## Fonte
[Observabilidade na AWS: utilizando o CloudWatch](https://cursos.alura.com.br/course/observabilidade-aws-utilizando-cloudwatch). O curso aborda o serviço CloudWatch, dashboards, alarmes, métricas, CloudWatch Agent e envio e consulta de logs.

## Conteúdo observado
A sequência inclui entendimento do CloudWatch, alarme de custos, criação e customização de dashboards, monitoramento detalhado, métricas de CPU, combinação de métricas e alarmes, instalação do agente, coleta de métricas específicas, direcionamento de logs e consulta de logs.

## Aplicação ao MX
A observabilidade deve combinar infraestrutura, aplicação, mensageria e qualidade de IA. O dashboard mínimo deve mostrar disponibilidade, latência p50/p95/p99, erros por endpoint, consumo de CPU/memória, lag de consumidores, taxa de DLQ, duração de retrieval, tokens de entrada/saída, custo estimado, taxa de respostas sem citação, taxa de fallback e aprovações humanas.

## Alarmes recomendados
Criar alarmes para erro sustentado, latência acima do SLO, ausência de eventos esperados, crescimento de DLQ, lag excessivo, saturação de CPU/memória, custo diário fora do orçamento e aumento de chamadas ao modelo. Alarmes devem ter severidade, runbook, janela de avaliação, limiar e destinatário. Evitar alertas baseados em uma única métrica isolada quando uma métrica composta reduzir falsos positivos.

## Logs
Logs estruturados devem conter `timestamp`, `level`, `service`, `trace_id`, `request_id`, `conversation_id` quando permitido, `event_type`, duração, resultado e erro normalizado. Nunca registrar prompt completo, documentos sensíveis, tokens, chaves ou dados pessoais sem política explícita. Para investigação, usar IDs de correlação e amostras redigidas.

## Skill derivada
**Instrumentar serviço de IA:** definir métricas e SLOs; emitir logs estruturados e traces; registrar custo e latência; construir dashboard; configurar alarmes com severidade; testar acionamento; documentar resposta; revisar falsos positivos e retenção de dados.

## Referências
[1]: https://cursos.alura.com.br/course/observabilidade-aws-utilizando-cloudwatch "Curso da Alura — Observabilidade na AWS: utilizando o CloudWatch"
