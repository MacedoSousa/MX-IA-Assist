# Módulo 05 — RAG, agentes e LangGraph

## Fonte
Formação da Alura: [Criando agentes de IA com LangChain e LangGraph](https://cursos.alura.com.br/formacao-criando-agentes-ia). A página informa 5 cursos e 46 horas de carga horária.

## Estrutura observada
A formação é organizada em cinco etapas. A primeira trata de arquiteturas RAG com embeddings, busca semântica, pré-processamento, ETL, chatbot contextualizado e redução de alucinações. A segunda aborda RAG avançado, ambientes isolados, embeddings, bancos vetoriais, consultas complexas, LangSmith e QA Eval. A terceira trata de LangGraph, ciclo pensamento–ação, nós, caminhos condicionais, estado com SQLite, scraping, análises e automações. As etapas posteriores são voltadas a protocolos e arquitetura para agentes — MCP, A2A, AG-UI e Backend for Agents — e a um projeto prático com LangChain, LangGraph e LangSmith.

## Conhecimento aplicado
A formação fornece o núcleo arquitetural do assistente pessoal local. O assistente não deve responder apenas com geração livre; deve recuperar contexto dos PDFs e demais materiais, atribuir fontes, controlar o fluxo de decisão e acionar ferramentas explicitamente. O estado do agente precisa ser persistido de forma controlada, separado de credenciais e sujeito a auditoria.

## Skill proposta: pipeline RAG para estudos

**Entrada:** documentos autorizados, metadados de disciplina, consulta do usuário e filtros de acesso.

**Processo:** extrair texto; normalizar; dividir em trechos com metadados; gerar embeddings; armazenar em índice vetorial; recuperar candidatos; reranquear ou filtrar; montar contexto limitado; gerar resposta; exigir referências; avaliar se a resposta está suportada.

**Saída:** resposta fundamentada, lista de trechos recuperados, identificadores dos documentos, score de recuperação e indicação de insuficiência de evidência quando aplicável.

**Limites:** não inventar fonte; não misturar documentos fora do escopo; não usar score de similaridade como prova de verdade; reindexar quando a versão do documento mudar; proteger documentos por autorização e registrar o caminho de recuperação.

## Skill proposta: agente com ferramentas

**Entrada:** intenção, estado da sessão, ferramentas permitidas e política de autorização.

**Processo:** classificar tarefa; selecionar fluxo; executar nós condicionais; chamar ferramentas com contratos; validar resultados; atualizar estado; interromper quando faltar evidência ou permissão.

**Saída:** resultado, eventos de execução, ferramentas chamadas, duração por etapa e estado final.

**Limites:** o modelo não pode criar novas permissões; ferramentas devem ser determinísticas ou explicitamente marcadas como probabilísticas; ações com efeitos externos exigem confirmação do usuário; loops e custos devem ter limites.

## Integração com o MX
O backend Java pode funcionar como gateway de autenticação, autorização e sessões. Um serviço de orquestração Python pode encapsular LangChain/LangGraph, desde que os contratos sejam estáveis e a comunicação seja observável. A memória de longo prazo deve ser dividida entre histórico conversacional, fatos aprovados pelo usuário e base documental; esses três tipos não devem ser tratados como equivalentes.

## Avaliação
O pacote de avaliação deve medir recuperação — precisão, cobertura e qualidade dos trechos —, geração — correção, completude, citações e ausência de alucinação — e operação — latência, falhas, custo, loops, consumo de contexto e sucesso das ferramentas. LangSmith e QA Eval podem ser estudados como referências, mas o projeto deve manter avaliações reproduzíveis e exportáveis localmente.

## Referências
[1]: https://cursos.alura.com.br/formacao-criando-agentes-ia "Formação da Alura — Criando agentes de IA com LangChain e LangGraph"
