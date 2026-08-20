# Módulo 06 — RAG detalhado: embeddings, chunking e indexação

## Fonte
[Arquiteturas RAG com LLMs: embeddings, busca semântica e criação de agentes com LangChain](https://cursos.alura.com.br/course/langchain-chatbots-rag). A página informa nível avançado, 8 horas, avaliação média 9,2, transcrição integral e atualização em 06/05/2026.

## Ementa registrada
O curso cobre fundamentos de LLMs e Transformer; alucinações; comparação entre fine-tuning e RAG; embeddings; similaridade de cosseno e distância euclidiana; embeddings open source e proprietários; chunking, overlapping e metadados; vector stores, FAISS e ChromaDB; pipeline RAG com LangChain; busca avançada e reranking.

As aulas estão organizadas em seis blocos: fundamentos e motivação para RAG; arquitetura RAG e construção de agentes; embeddings e similaridade semântica; processamento de documentos e chunking; armazenamento vetorial e indexação; e pipeline RAG completo com reranking e modularização.

## Aplicação aos materiais de estudo
Os PDFs do projeto devem ser tratados como documentos versionados. A ingestão precisa preservar disciplina, arquivo, página, seção, data de extração e hash do documento. O chunking deve respeitar limites semânticos e registrar a posição original para que o assistente possa citar a página correta.

## Pipeline recomendado
1. Extrair texto e identificar páginas.
2. Normalizar caracteres sem remover títulos, tabelas ou referências.
3. Dividir por estrutura semântica, com sobreposição controlada.
4. Gerar embeddings com modelo documentado e versão registrada.
5. Persistir vetores e metadados em índice local.
6. Recuperar candidatos por similaridade e aplicar filtros disciplinares.
7. Aplicar reranking quando a consulta for ambígua ou exigir maior precisão.
8. Montar contexto com limite de tokens e instrução de citação.
9. Gerar resposta e verificar se cada afirmação relevante possui suporte.
10. Registrar avaliação e reprocessar quando o documento mudar.

## Critérios de qualidade
A qualidade do RAG deve ser medida separadamente em recuperação e geração. Na recuperação, medir precisão dos trechos, cobertura dos fatos necessários e posição do trecho relevante. Na geração, medir correção, completude, fidelidade às fontes e taxa de respostas que reconhecem insuficiência de evidência. Também medir latência, custo, tamanho de contexto e estabilidade do índice.

## Riscos
Chunking inadequado pode separar definição e explicação; metadados incompletos impedem citações; embeddings incompatíveis prejudicam a busca; similaridade alta não garante verdade; e documentos desatualizados podem gerar respostas obsoletas. O sistema deve permitir reindexação, auditoria e exclusão por documento.

## Referências
[1]: https://cursos.alura.com.br/course/langchain-chatbots-rag "Curso da Alura — Arquiteturas RAG com LLMs"
