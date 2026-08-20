# Módulo 10 — Mensageria Java, RabbitMQ, Kafka e Saga

## Fonte
[Mensageria com Java: RabbitMQ e Kafka](https://cursos.alura.com.br/course/mensageria-java-rabbitmq-kafka). A ementa cobre comunicação assíncrona, RabbitMQ, produtores e consumidores, garantias de entrega, DLQ, Kafka, tópicos, partições, offsets, escalabilidade, Avro, Schema Registry e Saga Pattern.

## Decisões para o MX
Use **RabbitMQ** para comandos e fluxos de trabalho em que roteamento, confirmação, fila de atraso e dead-letter sejam centrais. Use **Kafka** para eventos duráveis, alto volume, replay, múltiplos consumidores e integração de telemetria. Não usar a mesma abstração sem declarar semântica de entrega, ordenação, retenção e reprocessamento.

## Eventos de IA recomendados
`DocumentIngestRequested`, `DocumentChunked`, `EmbeddingCreated`, `IndexUpdated`, `AgentRunStarted`, `RetrievalCompleted`, `ToolApprovalRequested`, `ToolExecuted`, `ModelInvocationCompleted`, `EvaluationCompleted` e `AgentRunFailed`.

## Contrato de evento
Todo evento deve ter `event_id`, `event_type`, `schema_version`, `occurred_at`, `producer`, `trace_id`, `correlation_id`, `aggregate_id`, `idempotency_key` e `payload`. Avro/Schema Registry é indicado para validar evolução de contratos e evitar consumidores incompatíveis.

## Resiliência
Consumidores devem ser idempotentes. Mensagens inválidas ou que excederem tentativas devem ir para DLQ com causa e correlação. Retries devem distinguir falhas transitórias de falhas permanentes. O processamento deve registrar offset, estado e resultado de forma consistente.

## Saga no assistente
Uma ingestão documental pode ser modelada como saga: registrar versão; extrair; gerar chunks; gerar embeddings; indexar; publicar concluído. Se indexação falhar, compensar removendo chunks parciais ou marcar versão inconsistente. A saga não desfaz efeitos externos automaticamente sem uma ação compensatória definida.

## Streaming de respostas
Para tokens e progresso de uma conversa, preferir SSE/WebSocket ou eventos de curta duração ligados ao `conversation_id`. Kafka/RabbitMQ devem transportar eventos de processamento e não substituir a conexão de apresentação. O cliente deve tolerar reconexão, duplicidade e término fora de ordem.

## Aplicação ao MX Core
Criar um `EventPublisher` e `EventConsumer` independentes do broker. Manter transação de domínio separada da publicação usando outbox quando a consistência exigir. Propagar `trace_id` nos headers. Monitorar atraso de consumidor, taxa de DLQ, retries, throughput, lag, duração de processamento e taxa de duplicidade.

## Referências
[1]: https://cursos.alura.com.br/course/mensageria-java-rabbitmq-kafka "Curso da Alura — Mensageria com Java: RabbitMQ e Kafka"
