# Dossiê detalhado — Big Data: introdução, Vs e velocidade dos dados

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `BigData Aula1.pdf` |
| Tipo | Material teórico acadêmico fornecido ao projeto |
| Extensão observada | 15 páginas físicas, com conteúdo textual nas páginas 1, 3 e 5–13 |
| Unidade | Introdução a Big Data |
| Responsável indicado no material | Prof. Dr. Alberto Messias |
| Tema central | Conceitos iniciais de Big Data, volume, variedade e velocidade |
| Uso neste dossiê | Síntese técnica original, análise crítica, exemplos próprios e aplicação ao MX |

## 1. Objetivos e escopo

O material introduz Big Data como uma combinação de problemas de escala, diversidade e rapidez que ultrapassa a capacidade prática de ferramentas convencionais. O objetivo pedagógico é compreender as características iniciais da tecnologia, diferenciar Big Data de um data warehouse e aprofundar os conceitos de volume, variedade e velocidade.

O foco da aula não é apresentar uma plataforma específica, uma implementação de Hadoop ou um algoritmo isolado. O conteúdo estabelece o vocabulário necessário para analisar sistemas de dados que precisam armazenar, processar e interpretar grandes quantidades de informações em intervalos aceitáveis. A consequência arquitetural é importante: Big Data não é sinônimo de um banco de dados específico; é uma classe de desafios que pode exigir processamento paralelo, armazenamento distribuído, computação em nuvem, redes de comunicação e sistemas escaláveis.

## 2. Conceito operacional de Big Data

O material reúne definições de diferentes autores. Em conjunto, elas descrevem Big Data como o tratamento de conjuntos de dados suficientemente grandes, complexos ou rápidos para que armazenamento, pesquisa, visualização e análise deixem de ser resolvidos adequadamente por ferramentas tradicionais.

Uma formulação operacional útil para a squad é a seguinte:

> **Big Data é um problema de engenharia e análise no qual a escala, a diversidade, a velocidade ou a complexidade dos dados exige mudanças relevantes no armazenamento, no processamento, na governança e na forma de extrair valor.**

Essa formulação evita dois erros comuns. O primeiro é definir Big Data apenas pelo tamanho absoluto do conjunto de dados, pois um volume que é grande para uma equipe pode ser trivial para outra. O segundo é tratar Big Data como um produto pronto. Na prática, a classificação depende do limite operacional: capacidade de ingestão, janela de processamento, latência aceitável, custo, qualidade, segurança e capacidade de recuperação.

O material associa Big Data a bancos de dados de processamento paralelo massivo, grades de mineração de dados, sistemas de arquivos distribuídos, plataformas de computação em nuvem, redes de comunicação e sistemas de armazenamento escaláveis. Esses componentes aparecem porque a arquitetura precisa distribuir dados e trabalho, reduzir gargalos e manter uma relação aceitável entre volume, tempo e custo.

## 3. Os Vs de Big Data

A aula enfatiza três dimensões iniciais: **volume, variedade e velocidade**. Essas dimensões devem ser analisadas juntas. Um sistema pode ter grande volume, mas baixa velocidade e pouca variedade; outro pode ter volume moderado, mas exigir resposta em tempo real e combinar texto, eventos, áudio e imagens. O desenho técnico muda conforme a combinação.

| Dimensão | Pergunta principal | Exemplos de pressão arquitetural | Métricas úteis |
|---|---|---|---|
| Volume | Quanto dado existe e quanto continuará sendo gerado? | Particionamento, armazenamento distribuído, compressão, retenção e capacidade | bytes/dia, crescimento mensal, número de objetos, custo por TB |
| Variedade | Quais formatos, estruturas e semânticas coexistem? | Esquemas evolutivos, catálogo, normalização seletiva, parsing e governança | proporção estruturada/não estruturada, tipos de fonte, taxa de erro de parsing |
| Velocidade | Com que rapidez o dado chega, deve ser processado e precisa ser disponibilizado? | Streaming, filas, particionamento, backpressure, janelas e processamento incremental | eventos/s, bytes/s, p50/p95 de latência, lag e tempo de recuperação |

### 3.1 Volume

Volume representa a quantidade de dados armazenados e a taxa de crescimento do acervo. O material usa exemplos históricos de petabytes, zettabytes e grandes plataformas digitais para ilustrar que o problema não é apenas guardar arquivos: é localizar, transferir, indexar, proteger, versionar, processar e excluir dados no momento correto.

Para o MX, volume precisa ser decomposto em categorias. Mensagens de conversa, documentos, chunks, embeddings, logs, traces, eventos Kafka, arquivos de avaliação e artefatos de modelos possuem padrões de retenção diferentes. Misturar tudo em uma única política gera custo desnecessário e aumenta o risco de exposição.

Uma estimativa inicial de armazenamento pode ser expressa por:

```text
armazenamento_bruto = itens_por_periodo × tamanho_médio × períodos_de_retenção
armazenamento_total = armazenamento_bruto × (1 + replicação + índices + metadados + margem)
```

Por exemplo, se forem ingeridos 50.000 chunks por mês, cada um com 8 KiB de texto, durante 12 meses, o texto bruto será aproximadamente `50.000 × 8 KiB × 12`. O cálculo real deve acrescentar embeddings, índices, versões, backups, replicação e crescimento. A estimativa deve declarar unidade, período, taxa de crescimento e política de retenção; um número sem essas premissas não é uma capacidade confiável.

### 3.2 Variedade

Variedade descreve a coexistência de dados estruturados, semiestruturados e não estruturados. O material cita transações, logs, páginas web, cliques, índices de pesquisa, redes sociais, fóruns, e-mails, sensores, IoT, vídeos, áudios, documentos e dados não relacionais.

A distinção não deve ser reduzida à extensão do arquivo. Um JSON pode ser semiestruturado, mas conter campos inconsistentes e semântica ambígua. Um PDF pode conter texto, tabelas, imagens e metadados. Uma mensagem de chat pode parecer simples, mas incluir citações, tool calls, anexos, permissões e eventos de execução.

O material apresenta a ideia de que Big Data combina transações, interações e observações. Para o MX, isso significa combinar registros transacionais do domínio, interações do usuário e observações operacionais como logs, métricas e traces. Essa combinação exige catalogação, classificação de sensibilidade, versionamento de esquema e regras de acesso.

Uma política de ingestão deve responder, antes do armazenamento:

| Pergunta | Decisão esperada |
|---|---|
| Qual é a fonte e quem é o proprietário? | Identidade da origem e responsabilidade de dados |
| O dado é estruturado, semiestruturado ou não estruturado? | Parser, esquema e estratégia de indexação |
| Qual é a sensibilidade? | Classificação, mascaramento, criptografia e autorização |
| O conteúdo tem versão e validade? | Hash, versão, data de coleta e data de revisão |
| O dado pode ser usado para treino ou avaliação? | Permissão, finalidade, anonimização e rastreabilidade |
| Como corrigir ou excluir? | Chave de retenção, deleção lógica/física e auditoria |

### 3.3 Velocidade

Velocidade não é apenas a frequência com que um sistema consulta seu banco. No contexto de Big Data, ela envolve a rapidez de geração, transmissão, ingestão, processamento e disponibilização do dado. A aula chama atenção para dados em movimento e para fluxos que podem ultrapassar a capacidade de sistemas tradicionais.

No MX, exemplos de dados em movimento incluem tokens de uma resposta em streaming, eventos de ingestão documental, chamadas de ferramentas, atualizações de sincronização mobile, métricas, traces, mensagens Kafka e avaliações assíncronas. Cada fluxo tem uma exigência diferente. Um trace pode tolerar alguns segundos; uma confirmação de ferramenta pode exigir baixa latência; um processo de reindexação pode ser assíncrono.

A velocidade deve ser descrita por um contrato e não por uma palavra vaga como “tempo real”. Um contrato mínimo informa taxa esperada, pico, latência-alvo, ordem, duplicidade tolerada, garantia de entrega, retenção, estratégia de retry e comportamento em indisponibilidade.

```text
StreamingContract
- eventType
- schemaVersion
- producer
- partitionKey
- expectedRate
- peakRate
- latencyTarget
- orderingRequirement
- deliverySemantics
- retryPolicy
- deadLetterPolicy
- retention
- observabilityFields
```

## 4. Big Data e data warehouse

A aula propõe diferenciar Big Data de data warehouse. Um data warehouse tradicionalmente organiza dados estruturados, integrados e preparados para consultas analíticas, geralmente com esquemas conhecidos e processos de extração, transformação e carga. Big Data pode incluir o warehouse, mas amplia o problema para fontes heterogêneas, dados não estruturados, fluxos contínuos, processamento distribuído e esquemas que evoluem.

A diferença não é absoluta. Um warehouse pode operar em escala muito grande, e uma arquitetura Big Data pode conter tabelas relacionais altamente estruturadas. A comparação correta deve considerar finalidade, padrão de acesso, latência, governança, estrutura e custo.

| Critério | Data warehouse tradicional | Plataforma de Big Data |
|---|---|---|
| Estrutura | Esquema geralmente definido antes da carga | Pode combinar schema-on-write e schema-on-read |
| Fontes | Principalmente sistemas transacionais integrados | Transações, sensores, logs, mídia, documentos e streams |
| Uso | Relatórios, BI e análise histórica | BI, exploração, ML, eventos, streaming e análise de grande escala |
| Processamento | ETL/ELT e consultas analíticas | Batch, streaming, processamento distribuído e pipelines híbridos |
| Governança | Catálogo e qualidade de tabelas | Catálogo, linhagem, esquema evolutivo, qualidade e políticas por tipo |
| Decisão arquitetural | Otimizar consistência e consulta analítica | Equilibrar escala, variedade, latência, custo e flexibilidade |

Para o MX, PostgreSQL deve continuar como fonte transacional do núcleo. Um lakehouse, armazenamento de objetos, banco documental ou broker só deve ser adicionado quando o padrão de dados justificar a complexidade. O rótulo Big Data não é motivo suficiente para abandonar um banco relacional simples e bem dimensionado.

## 5. Relação com processamento paralelo e distribuído

O material associa Big Data a processamento paralelo massivo, grades, sistemas distribuídos e nuvem. A razão é que um único processo ou servidor pode não atender à capacidade necessária dentro da janela disponível. Distribuir o trabalho, porém, introduz custos: particionamento, comunicação, sincronização, falhas parciais, consistência, reprocessamento e observabilidade.

A análise deve seguir a relação entre trabalho e comunicação. Se uma tarefa puder ser dividida em partes quase independentes, o paralelismo tende a ser mais eficiente. Se cada parte precisar trocar dados constantemente, o custo de comunicação pode anular o ganho. Em uma arquitetura de ingestão, por exemplo, a divisão por `document_id`, origem ou partição pode reduzir contenção; em um grafo de dependências muito acoplado, o particionamento pode aumentar a complexidade.

Uma análise crítica mínima compara:

```text
benefício_distribuição = tempo_monolítico / tempo_distribuído
custo_total = compute + armazenamento + rede + observabilidade + operação
risco_operacional = probabilidade_de_falha × impacto_da_falha
```

Essas fórmulas são modelos de decisão, não leis universais. As premissas precisam ser registradas e testadas com carga representativa.

## 6. Aplicação ao assistente pessoal local e ao MX

A aplicação imediata da aula no MX é uma arquitetura de dados por finalidade. O PostgreSQL armazena conversas, autorização, fatos aprovados e metadados transacionais. O armazenamento de objetos pode preservar arquivos originais e versões. Um pipeline de ingestão extrai texto e metadados. Chunks e embeddings recebem versão, hash, origem, página, disciplina e política de acesso. Kafka ou RabbitMQ transporta eventos de ingestão e avaliação quando o volume ou a necessidade de desacoplamento justificarem o broker.

A camada de observabilidade deve registrar métricas agregadas e eventos de correlação sem incluir prompts completos, tokens, documentos sensíveis ou segredos. A classificação de variedade deve aparecer no contrato do documento, diferenciando PDF textual, PDF digitalizado, tabela, imagem, áudio, código e evento estruturado. O assistente precisa recusar respostas quando a recuperação não apresentar evidência suficiente, porque aumentar o volume de documentos não garante qualidade.

### Fluxo recomendado

```text
Fonte autorizada -> classificação -> validação -> armazenamento original
                 -> extração -> normalização -> chunking -> embeddings
                 -> indexação -> recuperação -> reranking -> resposta com fonte
                 -> avaliação -> telemetria -> revisão ou reprocessamento
```

Cada etapa deve ser idempotente, rastreável e reprocessável. Um `document_version` deve funcionar como unidade de processamento. O hash impede reingestão desnecessária, a versão permite comparar mudanças e a página ou seção preserva a localização da evidência.

## 7. Exercícios autorais

### Exercício 1 — classificação dos Vs

Uma squad recebe 2 TB de PDFs históricos por mês, 10.000 eventos de ferramenta por minuto e documentos que misturam texto, tabelas e imagens. Identifique quais Vs pressionam a arquitetura e proponha quatro métricas.

**Gabarito orientativo:** há pressão de volume pelo crescimento mensal e pela necessidade de retenção; velocidade pelos eventos por minuto e pela exigência de processamento; variedade pelos formatos e estruturas diferentes. Métricas adequadas incluem bytes ingeridos por dia, taxa de eventos por segundo, p95 de latência de ingestão, taxa de falha de extração e percentual de documentos classificados corretamente.

### Exercício 2 — warehouse ou arquitetura híbrida

O MX precisa produzir relatórios mensais sobre custo de modelos, uso de ferramentas e tempo de resposta, mas também precisa pesquisar documentos e emitir respostas com citações. Escolha uma arquitetura e justifique.

**Gabarito orientativo:** uma arquitetura híbrida é mais adequada. O PostgreSQL pode manter fatos operacionais e custos estruturados; um armazenamento de documentos e índice vetorial pode atender recuperação; um pipeline pode produzir tabelas analíticas. Não é necessário transformar o núcleo transacional inteiro em uma plataforma distribuída. A decisão deve considerar volume, latência, retenção, privacidade e custo.

### Exercício 3 — contrato de streaming

Defina um contrato de evento para `DocumentIngestionRequested` com taxa esperada, chave de particionamento, idempotência, retry e DLQ.

**Gabarito orientativo:** o evento deve possuir `event_id`, `event_type`, `schema_version`, `occurred_at`, `trace_id`, `document_version`, `source_uri_hash`, `partition_key`, `idempotency_key` e política de retenção. A chave pode ser `document_id` para preservar ordem por documento. Falhas transitórias devem usar retry limitado; falhas permanentes devem ir para DLQ com causa e versão do schema. O consumidor só confirma após persistência do estado.

### Exercício 4 — cálculo de capacidade

Projete uma estimativa de armazenamento para 100.000 chunks mensais, 12 KiB de texto médio, 1.536 dimensões de embedding em float32, 2 réplicas e 30% de margem para índices e metadados. Declare suas premissas e explique por que o cálculo é aproximado.

**Gabarito orientativo:** cada dimensão float32 ocupa 4 bytes; o embedding bruto ocupa aproximadamente `1.536 × 4 = 6.144 bytes`, sem contar overhead. O texto bruto ocupa `100.000 × 12 KiB` por mês. O total deve somar texto, embeddings, índices, metadados e replicação; depois aplicar a margem declarada. O resultado deve ser apresentado com unidade e período, e validado com o mecanismo real de armazenamento, pois índices e compressão variam.

## 8. Checklist de domínio

| Competência | Evidência de domínio |
|---|---|
| Definição de Big Data | Explica por que escala é relativa à capacidade operacional |
| Vs | Diferencia volume, variedade e velocidade com métricas |
| Warehouse | Compara finalidade, estrutura, latência e governança |
| Distribuição | Identifica ganho, comunicação, falhas parciais e custo |
| Ingestão | Define versão, hash, schema, idempotência e reprocessamento |
| Streaming | Especifica taxa, latência, ordem, entrega, retry e DLQ |
| MX | Mapeia documentos, conversas, eventos, embeddings e telemetria |
| Análise crítica | Evita adotar tecnologia apenas pelo rótulo “Big Data” |

## 9. Limitações e atualização

O material contém exemplos históricos de volumes e previsões associados a anos anteriores. Esses números devem ser tratados como ilustrações do crescimento de dados, não como estatísticas atuais. Decisões de capacidade do MX precisam usar medições atuais do próprio sistema, carga representativa e custos vigentes.

Este dossiê é uma documentação original baseada no PDF salvo no projeto. As definições e referências do material foram preservadas como fonte acadêmica, enquanto os contratos, exemplos de arquitetura, fórmulas operacionais e exercícios foram elaborados para a squad.

## Referências

[1]: `BigData Aula1.pdf`, páginas 5, 8–13, material teórico salvo no projeto Estudos.

[2]: ZIKOPOULOS, Paul; EATON, Chris. *Understanding Big Data: Analytics for Enterprise Class Hadoop and Streaming Data*. McGraw-Hill Osborne Media, 2011. Referência indicada no PDF.

[3]: SOUZA, Alberto Messias da Costa. *Uma nova arquitetura para Internet das Coisas com análise e reconhecimento de padrões e processamento com Big Data*. Tese de Doutorado, Universidade de São Paulo, 2015. Disponível conforme referência do PDF em: http://www.teses.usp.br/teses/disponiveis/3/3142/tde-20062016-105809/

[4]: TARAPANOFF, Kira. *Análise da informação para tomada de decisão, desafios e soluções*. Curitiba: Intersaberes, 2015. Indicação bibliográfica presente no material.
