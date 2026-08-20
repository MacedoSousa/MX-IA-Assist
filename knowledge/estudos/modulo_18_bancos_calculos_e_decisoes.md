# Módulo 18 — Bancos de dados, cálculos e decisões quantitativas

## Objetivo

Estabelecer critérios técnicos para selecionar, modelar, operar e avaliar bancos de dados no MX e no assistente pessoal local, sem escolher tecnologias por moda.

## Camadas de persistência

| Necessidade | Opção inicial | Justificativa | Cuidados |
|---|---|---|---|
| Identidade, usuários, permissões, sessões transacionais e configurações | PostgreSQL | Relacionamentos, constraints, transações e consulta analítica | Migrations, índices, backup e limites de conexão |
| Documentos de estudo e metadados variáveis | PostgreSQL JSONB ou MongoDB | Estrutura flexível e evolução de atributos | Versionamento, validação e limites de documentos |
| Embeddings e busca vetorial | PostgreSQL com extensão vetorial ou banco especializado | Reduz complexidade inicial e mantém metadados próximos | Avaliar recall, latência, reindexação e custo |
| Cache, locks e rate limits | Redis | Baixa latência, TTL e estruturas simples | Persistência, eviction, concorrência e perda aceitável |
| Eventos de altíssimo volume | Kafka ou Cassandra conforme padrão de acesso | Escala horizontal e distribuição | Schema, ordenação, retenção, reprocessamento e operação |

## Contratos de dados

Toda tabela, coleção ou evento deve declarar proprietário, finalidade, classificação de sensibilidade, schema, chave, retenção, estratégia de backup, política de exclusão e consumidor. Eventos devem ser versionados e compatíveis para leitura; consumidores devem ser idempotentes e registrar offset ou checkpoint.

## Cálculos de capacidade

Para uma taxa média de `r` requisições por segundo e tamanho médio `s` bytes por evento, o volume diário aproximado é:

`volume_dia = r × s × 86.400`

Com fator de replicação `f` e overhead operacional `o`, a capacidade bruta estimada é:

`capacidade = volume_dia × f × (1 + o)`

Para filas, o tempo aproximado de esvaziamento de um backlog `b` com consumo `c` e entrada `r`, quando `c > r`, é:

`tempo = b / (c - r)`

Essas fórmulas são aproximações. O registro final deve informar unidade decimal ou binária, janela de pico, distribuição, compressão, retenção, crescimento e margem.

## Métricas de qualidade de ML/RAG

Para classificação, serão estudadas precisão, recall, F1, matriz de confusão e ROC-AUC, sempre com conjunto de teste separado. Para RAG, serão acompanhados recall de recuperação, precisão do contexto, fundamentação, taxa de citações válidas, latência, custo e taxa de abstention. Uma métrica isolada não autoriza promoção de modelo.

## Skills a produzir

O conhecimento será convertido em skills para seleção de persistência, modelagem PostgreSQL, uso controlado de NoSQL, cálculo de capacidade, avaliação de consultas, dimensionamento de filas e auditoria de métricas. Cada skill deverá possuir pré-condições, entradas, saída esperada, riscos e exemplos de teste.

## Aplicação no MX

O primeiro desenho deve manter PostgreSQL como fonte transacional, separar documentos e vetores por contratos claros, usar Redis apenas para necessidades de baixa latência e introduzir Kafka/RabbitMQ somente quando o processamento assíncrono trouxer benefício mensurável. Toda decisão deve ser registrada em ADR com alternativas, trade-offs, métricas de validação e plano de reversão.
