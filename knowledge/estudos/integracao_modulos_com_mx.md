# Integração dos módulos AI Engineer com o projeto MX

## Diretriz
O material estudado será aplicado ao MX como uma camada de conhecimento e arquitetura, sem acoplamento prematuro a um provedor específico. O núcleo existente em Spring Boot, Java 21 e PostgreSQL deve permanecer responsável por identidade, autorização, sessões, contratos de domínio e persistência transacional. Componentes de IA podem ser implementados inicialmente no Java com Spring AI ou isolados em Python quando LangChain/LangGraph, processamento documental ou experimentação justificarem a separação.

## Mapeamento de responsabilidades

| Componente | Responsabilidade | Módulos relacionados |
|---|---|---|
| MX Core | API, autenticação, autorização, sessões, contratos e auditoria | AI-02, AI-03, AI-05 |
| Model Gateway | Adaptadores OpenAI-compatible, Anthropic e runtime local | AI-01, AI-03, AI-04 |
| Conversation Service | Contexto, histórico resumido, memória factual aprovada | AI-02, AI-04 |
| Document Ingestion | Extração de PDF, chunking, metadados, versionamento | AI-05, AI-06 |
| Retrieval Service | Embeddings, vector store, filtros, reranking e citações | AI-05, AI-06 |
| Agent Orchestrator | Fluxos, estado, nós condicionais e ferramentas | AI-02, AI-05 |
| Tool Runtime | Execução autorizada, timeout, idempotência e eventos | AI-02, AI-05 |
| Telemetry | Logs, métricas, traces, custo e qualidade | AI-03, AI-05 |
| Evaluation | Casos versionados, baseline, regressão e modelo de registro | AI-01, AI-05, AI-06 |

## Fluxo conversacional recomendado

```text
Cliente -> MX Core -> Autorização -> Conversation Service
       -> Classificação da tarefa
       -> Retrieval Service (se necessário)
       -> Agent Orchestrator (se houver ferramenta ou fluxo)
       -> Model Gateway
       -> Validação de resposta
       -> Citações / Tool result / Memória aprovada
       -> Telemetry -> Cliente
```

O modelo nunca deve decidir sozinho sobre permissões. O MX Core fornece o conjunto de ferramentas permitido, e o Tool Runtime valida novamente cada chamada. O conteúdo recuperado dos documentos é contexto, não instrução de sistema.

## Primeiro incremento técnico
O primeiro incremento deve implementar apenas uma conversa com modelo configurável, timeout, retry limitado, logs estruturados, correlação e validação de saída. O segundo deve ingerir os PDFs do projeto com páginas e metadados, responder com citações e registrar score de recuperação. O terceiro deve habilitar ferramentas read-only. Somente depois devem ser adicionadas ferramentas com efeitos externos, streaming e mensageria.

## Persistência mínima
As entidades iniciais são `conversation`, `conversation_message`, `memory_fact`, `document`, `document_version`, `document_chunk`, `retrieval_event`, `tool_definition`, `tool_execution` e `model_invocation`. Dados de conversa, fatos aprovados, documentos e telemetria devem ter políticas de retenção e acesso distintas.

## Streaming e mensageria — próxima etapa
Respostas em streaming podem usar SSE ou WebSocket entre MX Core e interface, com eventos de início, delta, citação, tool call, erro e término. A ingestão documental deve ser assíncrona, com fila ou tópico, chave de idempotência por `document_version`, backpressure e dead-letter. O streaming de tokens não deve liberar uma ação externa; tool calls precisam de validação e, quando necessário, confirmação.

## AWS e Azure — princípio de portabilidade
A arquitetura deve abstrair armazenamento, filas, observabilidade e modelos para permitir implantação local, AWS ou Azure. A comparação deve ser feita por responsabilidade — identidade, compute, object storage, vector store, mensageria, monitoramento e serviço de IA — e não por nomes de produtos. Segredos devem permanecer em mecanismo de secrets da implantação; o código não deve conter credenciais.

## Critérios de integração
A integração será considerada adequada quando o assistente responder com fonte, recusar perguntas sem evidência, impedir ferramentas não autorizadas, tolerar indisponibilidade transitória, emitir correlação ponta a ponta e permitir trocar o modelo sem alterar o domínio.

## Limitação atual
A tarefa referenciada é um contexto de trabalho separado. Este arquivo e o pacote de skills no diretório compartilhado são o **artefato de transferência** do aprendizado: devem ser anexados ou copiados para o repositório do MX quando a implementação continuar naquela tarefa.


## Contratos de eventos e mensageria

A ingestão documental e os processos de avaliação devem emitir eventos com `event_id`, `event_type`, `schema_version`, `occurred_at`, `trace_id`, `producer` e payload validado. RabbitMQ pode transportar comandos de processamento e notificações dirigidas; Kafka pode manter eventos duráveis, replay, telemetria e integração entre consumidores. O reconhecimento do evento só ocorre depois de persistência durável. Retries devem ser classificados e falhas permanentes devem seguir para DLQ com motivo, versão de schema e possibilidade de reprocessamento controlado.

## Servindo modelos auxiliares

Classificadores de intenção, filtros de segurança, rerankers e modelos de previsão devem ser tratados como serviços com contrato versionado. O endpoint deve separar `health`, `ready`, `predict` e `metadata`, validar schema, aplicar autenticação, informar versão do modelo e emitir latência e erro sem expor dados sensíveis. O MX Core permanece dono da autorização e da decisão de negócio.

## Pipeline MLOps

O pipeline deve seguir dados -> validação -> transformação -> features -> treinamento -> avaliação -> registro -> aprovação -> deploy -> monitoramento -> drift -> retreino. Champion e challenger devem ser comparados com critérios de qualidade, custo, latência e segurança. Artefatos, dependências, dados, métricas e aprovação precisam ser versionados. Rollback deve ser exercitável antes da promoção.

## Observabilidade ponta a ponta

A correlação usa `trace_id` e `request_id` entre MX Core, retrieval, modelo, ferramentas, broker e worker. O dashboard deve separar infraestrutura, aplicação, mensageria, retrieval e qualidade de IA. Métricas mínimas incluem p50/p95/p99, erros, timeouts, tokens, custo estimado, taxa de citações válidas, abstention, lag, DLQ, drift e tempo de recuperação. Prompts completos, documentos sensíveis, segredos e tokens não entram em logs.

## Implantação AWS e Azure

A camada de infraestrutura deve ser substituível por responsabilidade. Em AWS, CloudWatch atende métricas, logs, dashboards e alarmes. Em Azure, Application Insights atende telemetria de aplicação; VNet, NSG, Firewall e Load Balancer estruturam a exposição e a resiliência. O código do MX não deve depender diretamente de um provedor nem conter credenciais. A implantação local deve permanecer válida para desenvolvimento e privacidade.


## Integração ampliada — dados e operação

### Persistência
O PostgreSQL continua como fonte transacional do MX Core. Documentos e metadados devem manter versão, hash, disciplina, página e autorização. Redis será reservado para cache, sessão, locks e filas curtas com TTL. MongoDB só deve ser adotado se a variabilidade documental ou o padrão de acesso justificarem; Kafka permanece no domínio de eventos duráveis e replay.

### Qualidade e entrega
Cada mudança deve atravessar os gates adequados: unitário, componente, integração, contrato, end-to-end, carga, segurança e avaliação de IA. O pipeline gera artefatos versionados, executa comparação com baseline, preserva evidências e fornece rollback. Contratos HTTP, eventos, tool calls e modelos devem ter compatibilidade verificável.

### Frontend e mobile
A camada web consome contratos estáveis e não contém regras de autorização. React/TypeScript e Design System devem representar estados de processamento, erro, ausência de evidência, confirmação e sucesso. Clientes Flutter, Android/Kotlin, iOS/Swift ou React Native aplicam offline-first, sincronização idempotente, permissões mínimas, notificações controladas e testes por plataforma.

### UX/UI e acessibilidade
Fluxos do assistente devem ser validados por pesquisa e testes de usabilidade. Componentes devem possuir foco, contraste, navegação por teclado ou leitor assistivo, tamanhos de toque e linguagem clara. Respostas geradas devem mostrar citações, incerteza e consequência das ações.

### Operação e gestão
O trabalho será acompanhado por Kanban com WIP, lead time, cycle time, throughput, aging, bloqueios e retrabalho. ADRs registram decisões arquiteturais. Relatórios de custo separam computação, rede, armazenamento, observabilidade e IA; toda estimativa declara moeda, período e premissas.

### Novos contratos

```text
PersistenceDecision
- entityType
- accessPattern
- consistency
- retention
- technology
- capacityFormula
- assumptions
- backupPolicy
- rollbackPlan

QualityGateResult
- changeId
- testLayers
- baselineVersion
- qualityMetrics
- performanceMetrics
- securityStatus
- decision
- evidenceRefs

MobileSyncEvent
- eventId
- deviceId
- platform
- entityId
- operation
- idempotencyKey
- occurredAt
- syncVersion
- conflictStatus

FlowCostSnapshot
- period
- currency
- wip
- throughput
- leadTime
- blockedItems
- computeCost
- modelCost
- storageCost
- observabilityCost
- assumptions
```


## Infraestrutura, release e análise estrutural

Kubernetes, Helm e Terraform serão camadas operacionais substituíveis. O MX Core, workers, ingestão e avaliação podem ser empacotados separadamente, com imagens imutáveis, charts versionados, `values` por ambiente, probes, requests/limits, secrets externos, persistência explícita e rollout progressivo. Pipelines devem executar testes, segurança, build, proveniência, deploy de teste, smoke test, avaliação de IA, aprovação e promoção; toda release deve possuir changelog, risco, evidência e rollback.

Linux será a base preferencial para containers, workers e automação. Windows será suportado para serviços corporativos, PowerShell, IIS, Active Directory, legado e integração com ambientes existentes. DNS, rotas, firewall, proxy reverso, SSH e certificados entram no diagnóstico operacional. O assistente pode emitir comandos de observação equivalentes para os dois sistemas, mas não pode alterar DNS, rotas, firewall ou produção sem autorização explícita.

Criptografia, identidade e autorização são controles transversais. TLS protege transporte; dados em repouso dependem de gestão e rotação de chaves; senhas usam derivação segura; JWT exige validação de emissor, audiência, expiração, algoritmo e escopo. Logs nunca devem conter tokens, chaves, prompts completos ou documentos sensíveis.

## Engenharia de linguagens

O sistema deve separar linguagem, versão, runtime, compilador, paradigma, dependências, build, testes e limitações. O Java puro permanece uma base importante do MX Core, enquanto Python atende experimentação, dados e IA; TypeScript/JavaScript atendem frontend e serviços web; Kotlin, Swift, Dart e React Native atendem canais móveis; C, C++, Rust e Go atendem integração nativa, desempenho e serviços; Bash, PowerShell, HCL, YAML e SQL atendem automação e infraestrutura.

Clipper será tratado como linguagem legada. Antes de qualquer migração, devem ser identificados dialeto, versão, compilador, arquivos, dependências e comportamento observável. Harbour ou outra compatibilidade xBase será hipótese de trabalho, não equivalência presumida. A migração deve usar testes de caracterização, adapters, contratos, fatias pequenas, backup e rollback.

### Contrato de geração e migração de código

```text
CodeEngineeringRequest
- language
- version
- runtime
- paradigm
- targetPlatform
- objective
- existingCodeRefs
- compatibilityConstraints
- dependencies
- buildCommand
- testCommand
- observabilityRequirements
- migrationRisk
- confidence

ReleaseDecision
- artifactVersion
- chartVersion
- imageDigest
- environment
- testEvidence
- securityEvidence
- rolloutStrategy
- approval
- rollbackVersion
- dnsAndNetworkImpact

SecurityKeyPolicy
- keyPurpose
- algorithm
- storage
- rotationPeriod
- accessPrincipals
- auditRef
- compromiseProcedure
```

## Análise crítica estrutural

Toda decisão deve responder se o problema é de domínio, contrato, dados, código, rede, infraestrutura, processo ou governança. Kubernetes não corrige acoplamento; criptografia não corrige autorização; pipeline não corrige testes ausentes; observabilidade não corrige ausência de operação; e uma linguagem nova não corrige modelagem ruim. O assistente deve explicitar a camada do problema, dependências, trade-offs, evidências, risco de lock-in e caminho de reversão antes de recomendar uma mudança.


## Biblioteca multidisciplinar — gestão, negócios e áreas gerais

A biblioteca da squad será tratada como uma camada de conhecimento organizada, não como um depósito indiscriminado de conteúdo. O catálogo conserva metadados e referências; a síntese original transforma conceitos em material de estudo; as skills transformam conhecimento em comportamento operacional; e os casos de avaliação verificam se a aplicação é coerente.

| Área | Aplicação no MX e na squad | Evidência esperada |
|---|---|---|
| Empreendedorismo e produto | Hipóteses, proposta de valor, roadmap, descoberta e validação | Registro de hipótese, experimento, métrica e decisão |
| Finanças e custos | Orçamento, TCO, custo de IA, capacidade e priorização | Fórmula, premissas, moeda, período e análise de sensibilidade |
| Liderança e pessoas | Papéis, feedback, gestão de impedimentos e desenvolvimento | Acordos, decisões, feedback e indicadores de fluxo |
| Comunicação e storytelling | ADRs, releases, documentação, incidentes e apresentações | Contexto, decisão, risco, responsável e próximo passo |
| Educação corporativa | Onboarding, trilhas internas, revisão e avaliação | Objetivo, pré-requisito, exercício e critério de conclusão |
| LGPD e governança | Minimização, retenção, autorização, auditoria e resposta a incidentes | Política versionada e evidência sem exposição de dados |
| UX/UI e acessibilidade | Estados de IA, confirmação, erro, citação e uso inclusivo | Teste de usabilidade e critérios de acessibilidade |
| Customer Success | Valor entregue, adoção, satisfação, resolução e retenção | Métricas de valor e feedback rastreável |

### Contrato de conhecimento da squad

```text
KnowledgeItem
- id
- sourceType
- sourceReference
- title
- domain
- competence
- prerequisites
- originalSynthesis
- authoredExercises
- applicationContext
- relatedSkill
- evidenceRefs
- version
- collectedAt
- reviewDueAt
- usageRestrictions

DecisionRecord
- decisionId
- context
- problemLayer
- alternatives
- assumptions
- costImpact
- riskImpact
- userImpact
- selectedOption
- evidenceRefs
- owner
- reviewDate
- rollbackOrExitPlan
```

O assistente não deve apresentar uma síntese como fato normativo sem indicar sua origem e sua data de revisão. Conteúdos de finanças, legislação, saúde ou segurança exigem contexto, escopo e revisão humana apropriada; eles não devem ser transformados automaticamente em recomendações pessoais ou comandos de produção. A biblioteca apoia raciocínio e preparação, mas decisões críticas permanecem sujeitas a validação humana.

### Integração com o ciclo Kanban

Cada item de conhecimento pode percorrer `Backlog -> Em estudo -> Sintetizado -> Exercitado -> Avaliado -> Aprovado -> Em revisão`. A promoção para `Aprovado` exige síntese original, exercício ou projeto, evidência de avaliação, fonte registrada e ausência de conflito não resolvido. Itens com conteúdo desatualizado ou dependência alterada retornam para `Em revisão`.

### Fluxo operacional

```text
Catálogo -> Classificação -> Síntese original -> Exercício autoral
         -> Aplicação no MX -> Avaliação -> Skill ou ADR
         -> Revisão periódica -> Nova versão
```

Esse fluxo conecta a biblioteca ao MX sem confundir conhecimento documental com pesos de modelo. O assistente pode consultar e aplicar os itens aprovados, mas deve relatar incerteza, solicitar confirmação para decisões de risco e registrar evidências de qualquer evolução.
