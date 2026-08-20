# Currículo modular priorizado — AI Engineer

## Objetivo
Transformar a trilha AI Engineer da Alura em um percurso técnico aplicável ao assistente pessoal local com IA. A trilha autenticada informa 83 cursos e 681 horas; por isso, a sequência abaixo prioriza dependências técnicas e valor para o projeto, em vez de simplesmente reproduzir a ordem visual da plataforma.

## Módulos e prioridade

| Prioridade | Módulo | Conteúdos centrais | Aplicação no assistente local | Produto de aprendizado |
|---|---|---|---|---|
| P0 | Fundamentos de IA generativa | Modelos de linguagem, tokens, contexto, prompting, limitações, alucinação e avaliação | Definir o comportamento do assistente e critérios de qualidade | Glossário, prompts-base e checklist de avaliação |
| P0 | Python, Java e integração de APIs | Clientes HTTP, autenticação, tratamento de erros, streaming de respostas e SDKs | Integrar o backend Java/MX com modelos locais e provedores externos | Skill de integração de modelos e exemplos seguros |
| P0 | RAG e gestão de conhecimento | Embeddings, chunking, busca vetorial, reranking, citações e atualização de documentos | Permitir que o assistente consulte materiais de estudo e memória | Pipeline RAG modular e conjunto de testes |
| P0 | Agentes e ferramentas | LangChain, LangGraph, planejamento, tool calling, estado, memória e guardrails | Executar tarefas controladas, consultar dados e acionar ferramentas | Skill de agente, contratos de ferramentas e políticas de segurança |
| P1 | Engenharia de software para IA | Arquitetura, testes, configuração, versionamento, CI/CD e segurança | Tornar o assistente sustentável, testável e evolutivo | Padrões de projeto, testes e checklist de revisão |
| P1 | Observabilidade | Métricas, logs estruturados, traces, correlação, alertas, SLI/SLO e custo | Diagnosticar falhas, latência, consumo e qualidade das respostas | Padrão de telemetria e painel operacional |
| P1 | Mensageria e streaming | Filas, tópicos, pub/sub, eventos, backpressure, idempotência e processamento assíncrono | Desacoplar ingestão, processamento e respostas do assistente | Contratos de eventos e fluxos de referência |
| P1 | Dados e machine learning | Preparação de dados, classificação, regressão, validação, métricas, fine-tuning conceitual e MLOps | Criar memória, classificação de intenções e avaliação contínua | Dataset versionado, esquema de avaliação e cards de modelo |
| P1 | Bancos de dados e memória | SQL, MySQL, bancos documentais, vetores, cache e políticas de retenção | Persistir usuários, sessões, documentos, memórias e auditoria | Modelo de dados e camada de persistência |
| P2 | Nuvem AWS | IAM, compute, armazenamento, redes, filas, observabilidade e serviços de IA | Preparar implantação híbrida ou cloud quando necessário | Matriz serviço–responsabilidade e arquitetura AWS |
| P2 | Nuvem Azure | Identidade, compute, storage, mensageria, monitoramento e serviços de IA | Oferecer alternativa de implantação e integração corporativa | Arquitetura Azure equivalente e matriz de decisão |
| P2 | Containers e orquestração | Docker, imagens, volumes, redes, Kubernetes e configuração | Reproduzir o ambiente local e facilitar implantação | Compose local e referência de implantação |
| P2 | Frontend e experiência | React, consultas de API, streaming visual, histórico e feedback | Construir uma interface útil para conversar e acompanhar tarefas | Contrato frontend–backend e fluxo de interação |
| P3 | Especializações | Visão, áudio, Android, Gemini, Vertex AI, Spring AI, n8n e automações | Expandir canais e modalidades do assistente | Módulos opcionais independentes |

## Sequência de execução

A primeira entrega prática deve cobrir fundamentos, integração em Python/Java, RAG e agentes. Em seguida, o estudo deve avançar para testes, observabilidade, mensageria e persistência. Somente depois faz sentido aprofundar AWS, Azure, Kubernetes e especializações, pois esses temas dependem de uma arquitetura de aplicação já definida.

| Etapa | Resultado esperado | Critério de conclusão |
|---|---|---|
| 1 | Assistente conversacional mínimo | Responde com modelo configurável e tratamento de falhas |
| 2 | Memória e RAG | Consulta documentos com fonte e controle de contexto |
| 3 | Ferramentas e agentes | Executa ferramentas com permissões, limites e auditoria |
| 4 | Operação observável | Emite logs, métricas e traces sem expor segredos |
| 5 | Processamento assíncrono | Usa eventos ou filas com idempotência e retentativas |
| 6 | Machine learning aplicado | Possui dataset, avaliação reproduzível e monitoramento de qualidade |
| 7 | Implantação | Pode ser executado localmente e possui caminho documentado para AWS/Azure |

## Regra de modularização
Cada curso estudado será convertido em um pacote com quatro camadas: uma explicação conceitual curta; uma skill operacional com entradas, saídas, pré-condições e limites; um exemplo de implementação independente; e um registro de avaliação contendo testes, riscos, métricas e relação com o assistente local. O conteúdo aprendido não será tratado como treinamento automático do modelo: ele será armazenado como documentação, exemplos, regras e dados avaliados, e só será usado em treinamento ou ajuste fino quando houver justificativa técnica e dados adequados.

## Primeiro lote recomendado
O primeiro lote deve ser formado por: **IA: explorando o potencial da inteligência artificial generativa**; **Python e GPT: crie seu chatbot com IA**; **Anthropic e Python: desenvolva assistentes e chatbots personalizados**; **Spring AI: integre uma aplicação Spring com a OpenAI**; **Flash Skills: RAG e Agentes de IA**; **Criando agentes de IA com LangChain e LangGraph**; e **LangChain: orquestrando aplicações de IA Generativa**. Esses itens formam o núcleo mais diretamente relacionado ao assistente local e permitem validar a arquitetura antes de ampliar para cloud, streaming e observabilidade.

## Integração com o projeto referenciado
O projeto do assistente pessoal local será tratado como consumidor dos módulos. A integração deverá preservar a separação entre núcleo conversacional, memória, recuperação documental, ferramentas, autenticação, observabilidade e adaptadores de modelo. O backend Java existente poderá receber os contratos de integração, enquanto componentes Python podem ser isolados como serviços de IA quando isso reduzir acoplamento ou facilitar experimentação.
