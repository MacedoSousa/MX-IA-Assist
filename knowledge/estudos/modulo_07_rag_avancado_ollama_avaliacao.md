# Módulo 07 — RAG avançado, Ollama e avaliação

## Fonte
[LangChain: Técnicas Avançadas de RAG](https://cursos.alura.com.br/course/langchain-rag-avancado). A página informa nível avançado, carga horária de 10 horas, avaliação média 8,8, transcrição integral e atualização em 22/10/2025.

## Ementa observada
O curso aborda ambientes isolados com virtualenv e Jupyter, revisão de RAG, debug com LangSmith, document loaders, chunking, embeddings, vector stores, tokenização, integração com Ollama, otimização de consultas, custos, tokens, validação de recomendações e avaliação de pipelines com LangSmith e QA-Eval.

## Aplicação ao assistente local
Ollama deve ser tratado como um adaptador de runtime local, não como dependência espalhada pelo domínio. O assistente deve poder executar embeddings e geração localmente quando privacidade ou indisponibilidade de rede forem prioritárias, mantendo contratos equivalentes aos adaptadores externos.

## Skill: avaliar uma pipeline RAG

**Entrada:** conjunto de perguntas, documentos de referência, respostas esperadas ou critérios de avaliação e configuração da pipeline.

**Processo:** executar consultas com configuração versionada; registrar documentos recuperados, scores, contexto, resposta, latência e consumo; calcular métricas de recuperação e geração; comparar com baseline; identificar regressões por disciplina e modalidade.

**Saída:** relatório de precisão, cobertura, fidelidade, citações válidas, abstention, latência e custo; lista de casos que falharam; recomendação de ajuste.

**Limites:** avaliação automática não substitui revisão humana; score de similaridade não comprova correção; resultados só são comparáveis com mesmo conjunto, versão de documentos, modelo e configuração.

## Melhorias técnicas derivadas
A busca deve aplicar filtros de metadados antes ou depois da similaridade conforme o índice. Consultas ambíguas podem ser reescritas, mas a consulta original deve permanecer auditável. O reranking deve ser usado quando melhora uma métrica definida, não como etapa automática sem medir custo e latência. Todo ajuste de chunking, embedding, prompt ou modelo deve gerar uma nova versão de avaliação.

## Integração com o MX
Adicionar ao `retrieval_event` os campos `query_hash`, `retriever_version`, `embedding_model`, `candidate_count`, `selected_chunk_ids`, `reranker`, `retrieval_latency_ms` e `evaluation_run_id`. O `model_invocation` deve registrar se o modelo foi local ou externo, sem armazenar segredo nem prompt integral por padrão.

## Referências
[1]: https://cursos.alura.com.br/course/langchain-rag-avancado "Curso da Alura — LangChain: Técnicas Avançadas de RAG"
