# Módulo 11 — Kafka com Python e sistemas distribuídos

## Fonte
[Mensageria com Python e Kafka: integração assíncrona em sistemas distribuídos](https://cursos.alura.com.br/course/mensageria-python-kafka). A ementa aborda eventos, partições, grupos de consumidores, offsets, commits manuais, falhas, retries, mensagens problemáticas, dead-letter, reprocessamento, consistência eventual e sagas.

## Aplicação ao assistente local
O serviço Python pode ser usado para ingestão documental, avaliações e workers de IA, enquanto o MX Core continua como autoridade de domínio. Cada worker deve ter grupo de consumidores e contrato de mensagem versionado. A chave de partição deve preservar a ordem necessária — por exemplo, `document_version_id` para ingestão ou `conversation_id` para eventos de uma conversa — sem concentrar todo o tráfego em uma única partição.

## Semântica de consumo
O commit do offset deve ocorrer depois de persistir o resultado ou registrar uma falha reprocessável. O consumidor deve evitar confirmar uma mensagem antes da operação ser durável. Reprocessamento exige idempotência, correlação e limite de tentativas. Dead-letter deve preservar payload, headers, erro, timestamp, origem e contagem de tentativas.

## Pipeline de ingestão
`DocumentIngestRequested -> extractor -> chunker -> embedding-worker -> indexer -> DocumentIndexed`. Cada etapa pode ser escalada separadamente. O estado da saga fica no MX; Kafka transporta eventos e permite replay controlado.

## Observabilidade
Medir consumer lag, taxa de processamento, tempo de permanência, retries, DLQ, duplicidade, tamanho de lote, erro por tópico e tempo de ponta a ponta. Correlacionar `trace_id` e `document_version_id` em todos os serviços.

## Referências
[1]: https://cursos.alura.com.br/course/mensageria-python-kafka "Curso da Alura — Mensageria com Python e Kafka"
