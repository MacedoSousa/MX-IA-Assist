# Evolução de histórico, tópicos e aprendizagem adaptativa do MX

## Objetivo

O MX continuará sendo o único ponto de comunicação do usuário. As skills permanecem capacidades internas, selecionadas por contratos explícitos e por evidências do prompt, do histórico e da taxonomia de estudos. O histórico passa a ser consultável por usuário e por tópico, sem misturar dados entre proprietários.

## Decisões de domínio

| Necessidade | Decisão |
|---|---|
| Histórico por usuário | Endpoint paginado de conversas autenticado pelo usuário atual, com ordenação por atividade mais recente. |
| Histórico por tópico | Tópico persistido na conversa, normalizado por uma taxonomia lexical local; o cliente também pode filtrar por `topic`. |
| Idioma | Idioma detectado por heurística PT/EN e salvo na conversa. O perfil do usuário mantém preferência explícita quando declarada. |
| Gírias e estilo | O perfil registra sinais normalizados de comunicação, sem enviar texto sensível bruto ao prompt; o contexto informa somente regras agregadas e não privilegiadas. |
| Busca ativa | Antes de usar conhecimento, o roteador consulta o índice documental local e usa o resultado para reforçar a skill, sem executar instruções encontradas nos documentos. |
| Especialização | Cada skill declara triggers, domínios e capacidade; a decisão deve considerar confiança, domínio detectado e preferência do usuário, com fallback para `general`. |
| Escalonamento local | Não será declarado autoscaling horizontal como se fosse cloud. O MX usa escalonamento vertical, fila limitada, paralelismo configurável, cache, keep-alive e perfil de modelo por hardware. |

## Segurança

Todo endpoint usa o usuário autenticado como autoridade. Identificadores de conversa, mensagens e anexos são resolvidos com `user_id`; um ID existente de outro usuário produz o mesmo resultado de “não encontrado”. Tópicos e idioma são metadados de navegação, não instruções para o modelo. Conteúdo recuperado do índice, histórico ou anexos é sempre marcado como dado não privilegiado.

## Idiomas

A primeira versão usa detecção local de português brasileiro e inglês por termos de alta frequência. A transcrição local recebe o idioma escolhido (`pt`, `en` ou `auto`) e o motor Whisper pode retornar o idioma utilizado. A resposta do MX preserva o idioma da pergunta, salvo solicitação explícita diferente.

## Desempenho

O modelo rápido permanece como padrão até que uma medição no hardware real demonstre vantagem de um modelo maior. O perfil de alta capacidade fica opt-in. O histórico enviado ao modelo é limitado por quantidade e tamanho, a recuperação documental é seletiva e o índice é carregado sob demanda. O benchmark deve registrar carga, p50, p95 e volume de dados.

## API planejada

`GET /api/v1/conversations?page=0&size=20&topic=...&query=...` lista conversas do usuário atual. `GET /api/v1/conversations/{id}/messages` mantém a paginação já existente. `GET/PUT /api/v1/users/me/preferences` expõe preferências de aprendizagem, idioma e estilo. O chat mantém compatibilidade retroativa e aceita referências de anexos.

## Critérios de aceite

Uma conversa criada pelo usuário A nunca aparece para o usuário B. O filtro por tópico é determinístico e indexado. Pressionar Enter envia uma mensagem no cliente web sem inserir nova linha; `Shift+Enter` mantém a quebra de linha. A seleção de modelo não aumenta o custo padrão sem evidência de hardware. Todas as alterações devem possuir testes unitários, validação de migração e documentação bilíngue resumida.

## English summary

MX remains the single user-facing coordinator. Conversation metadata stores a normalized topic and detected language, while paginated user-scoped APIs support browsing by topic. Local retrieval is active but untrusted document content is always treated as data, never as instructions. Local Docker scaling is described honestly as vertical tuning and bounded concurrency, not cloud-style autoscaling. The fast model remains the default until hardware benchmarks justify an optional high-capacity profile.
