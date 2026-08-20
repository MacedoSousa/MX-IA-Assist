# MX — Backlog Mestre do Produto e Plano de Execução

**Produto:** MX — assistente pessoal local centralizador
**Responsabilidade de produto:** Product Owner / Product Manager do projeto
**Responsabilidade técnica:** arquitetura fullstack, backend, frontend, infraestrutura e qualidade
**Versão de referência:** 0.3 — núcleo central com skills especialistas e canais web/mobile
**Status:** backlog executável; a fundação P0–P3 está implementada em nível avançado, e os itens abaixo distinguem entrega realizada de lacunas ainda abertas.

## Estado atual — agosto de 2026

A fundação principal foi implementada e validada: autenticação com sessão revogável e refresh rotativo; MX Core como ponto único de comunicação; lifecycle persistido de `ExecutionRun`; streaming SSE; skills `general` e `development`; Policy Engine; `workspace.list`; `workspace.write_file` com sandbox; endpoints de consulta, aprovação e rejeição; e cliente Expo universal com web, Android e iOS.

| Item do backlog | Estado atual | Próxima ação |
|---|---|---|
| MX-011 / MX-012 | Implementados em nível de fundação | Completar hardening, logout global e testes E2E. |
| MX-020 / MX-021 | Implementados no caminho central | Expandir histórico, paginação e testes de ownership. |
| MX-030 / MX-031 | Implementados para Ollama local | Completar timeout, health indicator e testes de indisponibilidade. |
| MX-040 / MX-070 | Implementados com `ExecutionRun` | Integrar todos os caminhos de tool ao lifecycle. |
| MX-041 / MX-042 | Implementados inicialmente | Ampliar skills, confiança, ambiguidade e avaliação de routing. |
| MX-060 / MX-061 / MX-063 | Implementados inicialmente | Expandir registry, schemas, limites e auditoria. |
| MX-062 / MX-145 | Parcialmente implementados | Criar automaticamente `AWAITING_APPROVAL` a partir de `REQUIRE_APPROVAL` e adicionar nonce, expiração e replay protection. |
| MX-100 / MX-101 / MX-102 | Cliente Expo funcional, UX ainda mínima | Adicionar painel de run, histórico e aprovação visual. |
| MX-140 / MX-141 / MX-143 | Contrato e cliente implementados em nível de fundação | Completar testes de contrato e validação nos três canais. |
| MX-144 / MX-147 | Abertos | Implementar cursor, idempotência, reconexão e estados offline. |
| MX-130 / MX-131 / MX-132 | Abertos | Threat model, prompt injection, retenção, E2E e cenários adversariais. |

A ordem imediata de execução é: (1) higiene Git e documentação; (2) ponte `ToolExecutor → ExecutionRun`; (3) aprovação segura com nonce/expiração/replay protection; (4) sincronização cross-channel; (5) threat model, testes E2E e operação local.

## 1. Visão do produto

O MX será o único ponto de comunicação do usuário com o sistema. O usuário não conversa diretamente com agents ou skills. O MX Core recebe a solicitação, entende o contexto, seleciona a capacidade especialista apropriada, aplica políticas de segurança e autonomia, executa o trabalho, supervisiona o resultado, registra o que ocorreu e devolve uma resposta única.

> **Princípio de produto:** skills são capacidades internas especializadas; o MX é responsável pela decisão, pelo contexto, pela segurança, pela qualidade e pela comunicação.

O produto será construído localmente, com prioridade para privacidade, baixa latência, transparência operacional e evolução incremental. A primeira meta não é criar um agente que faça tudo; é criar um núcleo confiável que possa incorporar especialistas sem espalhar lógica pelos controllers, prompts ou integrações.

## 2. Objetivos e não objetivos

| Objetivo | Resultado esperado |
|---|---|
| Conversação central | O usuário utiliza uma única interface do MX para qualquer solicitação |
| Especialização | Cada skill possui contrato, escopo, ferramentas, autonomia, testes e métricas próprias |
| Segurança | Nenhuma ação operacional ocorre fora de uma política explícita |
| Velocidade | O caminho de resposta é curto, mensurável e não abre transações durante chamadas de modelo |
| Confiabilidade | Execuções possuem estados, correlação, falha segura, idempotência e diagnóstico |
| Evolução | Adicionar uma skill não exige espalhar condicionais no controller ou no orquestrador |
| Operação local | O ambiente inicia, diagnostica, monitora e recupera dependências de maneira reproduzível |

Não faz parte do primeiro ciclo permitir shell irrestrito, escrita automática no workspace, autonomia financeira, decisões médicas ou jurídicas, publicação automática em serviços externos, treinamento de modelo próprio ou arquitetura distribuída prematura. Esses assuntos somente entram após política, auditoria, aprovação e testes adequados.

## 3. Modelo de priorização

A ordem combina **valor para o usuário**, **risco técnico**, **dependências**, **risco de segurança**, **reversibilidade** e **custo de atraso**. A prioridade P0 precisa ser concluída antes de expandir a autonomia das skills. P1 entrega capacidades úteis e controladas. P2 amplia memória, integrações e experiência. P3 representa otimizações e capacidades avançadas.

| Prioridade | Significado | Regra de execução |
|---|---|---|
| P0 | Fundação, segurança ou bloqueio de produto | Executar primeiro, sem iniciar épicos P1 dependentes |
| P1 | Valor principal do MVP e autonomia somente leitura | Executar após P0 e sempre com testes de contrato |
| P2 | Expansão controlada de memória, interface e integrações | Executar quando o fluxo principal estiver estável |
| P3 | Otimização, escala e autonomia avançada | Executar somente com métricas e auditoria suficientes |

## 4. Ordem macro de execução

| Ordem | Incremento | Resultado de negócio | Dependências |
|---:|---|---|---|
| 1 | Baseline reproduzível | O time consegue compilar, testar e diagnosticar o MX | Nenhuma |
| 2 | Clean Architecture e contratos | O núcleo pode evoluir sem acoplamento de infraestrutura | 1 |
| 3 | Identidade e isolamento | O usuário e seus dados possuem fronteira de segurança | 1 |
| 4 | Conversação persistente | O MX responde e preserva mensagens do usuário | 2, 3 |
| 5 | Model Gateway resiliente | Ollama é substituível, configurável e mensurável | 2, 4 |
| 6 | MX Core e Skill Registry | Uma interação única é encaminhada para uma skill | 2, 4, 5 |
| 7 | Policy Engine e Tool Registry | Skills podem propor ações sem contornar segurança | 3, 6 |
| 8 | Execuções auditáveis | O usuário e o operador entendem o que ocorreu | 6, 7 |
| 9 | Primeira skill de desenvolvimento | O MX auxilia programação com leitura controlada | 6, 7, 8 |
| 10 | Contexto e memória | O MX responde com continuidade e memória confirmada | 4, 8 |
| 11 | Contrato API-first | Web e mobile consomem o MX por uma fronteira versionada | 3, 6, 8 |
| 12 | Frontend web operacional | O usuário acompanha resposta, skill, progresso e aprovação no navegador | 11 |
| 13 | Aplicativos Android e iOS | O usuário continua o mesmo MX em dispositivos móveis | 11, 12 |
| 14 | Sincronização e notificações | Conversas, runs e aprovações atravessam canais | 8, 11, 12, 13 |
| 15 | Skills adicionais e integrações | Pesquisa, organização pessoal, testes e serviços externos | 7, 8, 9, 10, 12, 13 |

## 5. Épicos, histórias de usuário e tarefas técnicas

### EPIC-00 — Governança de produto e qualidade

#### MX-001 — Manter visão, escopo e decisões do produto — P0

**História de usuário:** como responsável pelo MX, quero manter visão, escopo, decisões e não objetivos documentados, para que a velocidade de desenvolvimento não destrua a coerência do produto.

**Critérios de aceitação:** a visão central do MX está documentada; o usuário conversa somente com o MX Core; skills são internas; decisões que alteram segurança, autonomia ou persistência possuem ADR; o backlog possui prioridade e dependências atualizadas.

**Tarefas:**

| ID | Tarefa | Saída |
|---|---|---|
| T-001 | Revisar README e apontar o estado real do sistema | README honesto |
| T-002 | Criar template de ADR | `docs/adr/000-template.md` |
| T-003 | Registrar decisão MX Core versus skills | ADR aprovado no repositório |
| T-004 | Definir glossário MX, skill, tool, run e memória | `docs/glossary.md` |

#### MX-002 — Fixar baseline de desenvolvimento — P0

**História de usuário:** como desenvolvedor, quero executar build e testes de maneira reproduzível, para obter feedback rápido e confiável.

**Critérios de aceitação:** Java, Maven, Spring Boot, Docker Compose e documentação usam versões coerentes; testes podem usar diretório de build limpo; scripts falham com diagnóstico; nenhum segredo real é versionado.

**Tarefas:** revisar POM e Dockerfile; corrigir perfil local; validar Compose no Windows; separar artefatos de build; criar comando de diagnóstico; registrar limitações de permissões do ambiente; incluir `.env.example` seguro.

#### MX-003 — Aplicar Clean Architecture e SOLID — P0

**História de usuário:** como equipe técnica, quero que regras de negócio dependam de portas e não de frameworks, para manter testabilidade e evolução.

**Critérios de aceitação:** casos de uso não importam HTTP, JPA ou cliente Ollama; adapters ficam nas bordas; dependências são injetadas; classes possuem responsabilidade única; testes unitários não sobem infraestrutura.

**Tarefas:** revisar pacotes por feature; remover dependências invertidas; definir convenções de nomes; adicionar testes arquiteturais quando o volume justificar; registrar decisões de composição Spring.

#### MX-004 — Implantar TDD e Definition of Done — P0

**História de usuário:** como desenvolvedor, quero iniciar cada comportamento com teste, para reduzir regressões e tornar o contrato executável.

**Critérios de aceitação:** cada história P0 possui teste de aceitação ou unidade; casos de falha são testados; a suíte completa é executada antes de concluir uma história; a Definition of Done é aplicada em pull request ou checklist local.

**Tarefas:** criar padrão Red-Green-Refactor; separar testes unitários, integração e contrato; definir nomes de testes; registrar comandos de validação; impedir conclusão com teste conhecido quebrado.

### EPIC-01 — Identidade, sessão e segurança

#### MX-010 — Registrar usuário — P0

**História de usuário:** como usuário, quero criar minha conta com senha protegida, para acessar o MX de forma autenticada.

**Critérios de aceitação:** email é normalizado; senha nunca é persistida em texto puro; validação não expõe detalhes internos; duplicidade retorna erro seguro; criação é auditável.

**Tarefas:** validar DTO; aplicar `PasswordEncoder`; criar caso de uso; criar porta de usuário; adaptar repositório; adicionar migration; testar email inválido, duplicidade e senha fraca.

#### MX-011 — Autenticar com sessão revogável — P0

**História de usuário:** como usuário, quero iniciar uma sessão vinculada ao token, para controlar expiração e revogação.

**Critérios de aceitação:** login cria `sessionId` ou `jti`; token expirado ou revogado falha; resposta não contém segredo; sessão possui timestamps e usuário; filtro valida sessão ativa.

**Tarefas:** criar entidade e repository de sessão; adicionar `jti` ao JWT; criar caso de uso de login; implementar filtro; testar token válido, expirado, inválido e revogado.

#### MX-012 — Encerrar sessão — P0

**História de usuário:** como usuário, quero fazer logout, para impedir o uso posterior daquela sessão.

**Critérios de aceitação:** logout revoga a sessão atual; token revogado não acessa rota protegida; logout global revoga sessões do usuário; evento é auditável.

**Tarefas:** criar endpoint; implementar revogação; testar concorrência básica; adicionar opção de logout global; registrar evento seguro.

#### MX-013 — Isolar dados por proprietário — P0

**História de usuário:** como usuário, quero acessar somente meus dados, para preservar minha privacidade.

**Critérios de aceitação:** consultas usam o proprietário autenticado; IDs de terceiros retornam 404 ou resposta indistinguível; há testes de acesso cruzado para conversas, runs, memórias e arquivos.

**Tarefas:** criar `CurrentUserPort`; remover busca de usuário espalhada em controllers; aplicar ownerId nas queries; revisar repositories; criar testes negativos.

#### MX-014 — Proteger segredos e configurações — P0

**História de usuário:** como operador, quero configurar credenciais por ambiente, para não expor segredos no código.

**Critérios de aceitação:** senha de banco, JWT secret e tokens externos vêm de ambiente; exemplos são falsos; logs não exibem segredos; inicialização falha quando segredo obrigatório está ausente em ambiente seguro.

**Tarefas:** revisar propriedades; criar `.env.example`; mascarar logs; validar configuração; documentar rotação local.

### EPIC-02 — Conversação central

#### MX-020 — Enviar mensagem pelo MX Core — P0

**História de usuário:** como usuário autenticado, quero enviar uma mensagem ao MX, para receber uma resposta centralizada.

**Critérios de aceitação:** prompt vazio é recusado; mensagem do usuário é persistida; o MX cria `correlationId`; uma skill é selecionada; resposta é persistida; falha do modelo não gera resposta falsa; retorno contém IDs e resposta.

**Tarefas:** consolidar `SendMessageUseCase`; criar DTOs estáveis; separar usuário e assistant message; adicionar tratamento global de erro; testar sucesso, entrada inválida, falha e resposta vazia.

#### MX-021 — Continuar conversa — P0

**História de usuário:** como usuário, quero enviar `conversationId`, para manter contexto entre mensagens.

**Critérios de aceitação:** conversa pertence ao usuário; nova conversa só é criada quando o ID não é informado; mensagens recentes possuem limite; conversa de terceiro não é acessível.

**Tarefas:** ampliar `ChatRequest` de modo compatível; criar `ConversationContextPort`; carregar histórico limitado; adicionar testes de ownership e limite; atualizar frontend.

#### MX-022 — Listar e consultar histórico — P1

**História de usuário:** como usuário, quero listar conversas e consultar mensagens, para revisar meu trabalho com o MX.

**Critérios de aceitação:** paginação ou limite explícito; ordenação previsível; dados de terceiros não aparecem; DTOs não expõem entidades JPA.

**Tarefas:** criar casos de uso; endpoints de leitura; paginação; testes de autorização; documentação OpenAPI.

#### MX-023 — Resposta com estado de processamento — P1

**História de usuário:** como usuário, quero saber quando o MX está processando, para distinguir latência de falha.

**Critérios de aceitação:** a interface apresenta recebido, roteado, processando, aguardando aprovação, concluído ou falho; o usuário recebe `correlationId`; timeout produz estado compreensível.

**Tarefas:** expor `RunStatus`; criar polling ou SSE depois do contrato; adicionar eventos de lifecycle; atualizar frontend.

### EPIC-03 — Model Gateway e velocidade

#### MX-030 — Definir contrato de modelo — P0

**História de usuário:** como aplicação, quero depender de `ModelGateway`, para substituir Ollama por fake ou outro modelo sem alterar casos de uso.

**Critérios de aceitação:** request contém usuário e prompt; response contém resposta, modelo e duração; erro é convertido para exceção de aplicação; testes usam fake.

**Tarefas:** consolidar records; definir limites de prompt; definir erro de indisponibilidade; adicionar testes de contrato.

#### MX-031 — Configurar adaptador Ollama — P0

**História de usuário:** como operador local, quero configurar URL, modelo e timeout, para rodar o MX dentro ou fora do Docker.

**Critérios de aceitação:** URL e modelo vêm de configuração; dentro do Compose o host é configurável; timeout existe; resposta não-2xx gera erro controlado; JSON inválido é diagnosticado; modelo ausente apresenta causa operacional.

**Tarefas:** remover valores hardcoded; configurar `OLLAMA_URL` e `OLLAMA_MODEL`; usar cliente HTTP com timeout; criar fake HTTP ou teste de contrato; adicionar health indicator.

#### MX-032 — Roteamento de modelos — P1

**História de usuário:** como operador, quero escolher modelos por skill e complexidade, para equilibrar velocidade e qualidade.

**Critérios de aceitação:** skill declara modelo preferido; fallback é configurável; decisão é auditável; modelo não autorizado não é usado.

**Tarefas:** criar `ModelProfile`; implementar seleção; configurar modelo rápido para classificação; medir p50/p95; testar fallback.

#### MX-033 — Resiliência do gateway — P1

**História de usuário:** como usuário, quero uma falha controlada quando o modelo estiver indisponível, para saber como prosseguir.

**Critérios de aceitação:** timeout não trava request indefinidamente; retries são limitados; respostas vazias falham; circuit breaker evita tempestade; banco não fica em transação durante rede.

**Tarefas:** definir timeout; implementar retry somente para erros transitórios; adicionar circuit breaker; testar timeout, 500, JSON inválido e resposta vazia.

### EPIC-04 — MX Core, routing e supervisão

#### MX-040 — Registrar ciclo de execução — P0

**História de usuário:** como operador, quero acompanhar cada execução, para explicar e recuperar falhas.

**Critérios de aceitação:** estados mínimos `RECEIVED`, `ROUTED`, `EXECUTING`, `AWAITING_APPROVAL`, `VERIFYING`, `COMPLETED`, `FAILED` e `CANCELLED`; transições inválidas são recusadas; runId e correlationId existem.

**Tarefas:** criar enum e aggregate de lifecycle; criar máquina de transição; escrever testes de transições válidas e inválidas; integrar com o caso de uso.

#### MX-041 — Registrar skills por capacidade — P0

**História de usuário:** como núcleo, quero descobrir skills pelo registry, para adicionar especialistas sem condicionais espalhados.

**Critérios de aceitação:** skill declara nome, versão, descrição, gatilhos, ferramentas e autonomia; duplicidade falha; skill ausente falha de forma clara; skill desabilitada não executa.

**Tarefas:** consolidar `SkillRegistry`; adicionar estado enabled; criar health check de skill; registrar skills nativas por composição.

#### MX-042 — Roteamento explicável — P0

**História de usuário:** como MX, quero selecionar a skill com confiança e justificativa, para reduzir decisões opacas.

**Critérios de aceitação:** decisão contém skill, confiança, motivo e necessidade de esclarecimento; confiança baixa não executa ação; fallback general existe; prompt não é exposto em métricas.

**Tarefas:** manter roteador determinístico inicial; criar contrato para classificador estruturado futuro; adicionar empate e ambiguidade; testar português e acentos.

#### MX-043 — Construir contexto autorizado — P1

**História de usuário:** como skill, quero receber somente contexto autorizado e relevante, para reduzir custo e vazamento.

**Critérios de aceitação:** contexto possui limite de tokens ou caracteres; ownerId é aplicado; instrução de sistema fica separada dos dados; histórico e memória têm origem identificável.

**Tarefas:** criar `ContextBuilder`; implementar janela recente; separar system, user e retrieved context; medir tamanho e latência; testar truncamento.

#### MX-044 — Supervisionar resultado — P1

**História de usuário:** como usuário, quero que o MX valide o resultado antes de responder, para evitar respostas vazias, ações fora do contrato ou formato inválido.

**Critérios de aceitação:** resultado vazio falha; formato esperado é validado; skill não pode ultrapassar autonomia; erro é seguro; resultado não validado não é persistido como concluído.

**Tarefas:** criar `SkillSupervisor`; criar validadores por skill; adicionar estado `VERIFYING`; testar falhas de schema, vazio e autonomia.

#### MX-045 — Pedir esclarecimento — P1

**História de usuário:** como usuário, quero que o MX pergunte quando minha solicitação for ambígua, para não executar a coisa errada.

**Critérios de aceitação:** baixa confiança não dispara ferramenta; pergunta é curta; estado é persistido; resposta seguinte continua a execução correta.

**Tarefas:** criar `ClarificationPolicy`; persistir pending clarification; testar baixa confiança, empate e resposta do usuário.

### EPIC-05 — SDK e skills especialistas

#### MX-050 — Criar padrão de skill especialista — P0

**História de usuário:** como desenvolvedor de skills, quero um contrato e template padronizados, para criar especialistas consistentes.

**Critérios de aceitação:** template contém definição, execução, política, testes, métricas, README e casos de recusa; skill não acessa infraestrutura diretamente; versão é declarada.

**Tarefas:** criar template; documentar lifecycle; definir convenção de pacotes; criar teste de conformidade de skill.

#### MX-051 — Skill de desenvolvimento — P1

**História de usuário:** como desenvolvedor, quero pedir análise de código, arquitetura, bugs e testes ao MX, para trabalhar mais rápido com orientação técnica confiável.

**Critérios de aceitação:** skill responde com diagnóstico e plano verificável; declara somente leitura inicialmente; não inventa execução; pode propor leitura de arquivos; usa Clean Architecture, SOLID e TDD como padrão.

**Tarefas:** definir prompt especializado; criar output estruturado; criar avaliação de respostas; conectar ferramentas somente leitura; criar testes de routing e comportamento.

#### MX-052 — Skill de pesquisa — P1

**História de usuário:** como usuário, quero solicitar pesquisa e síntese, para obter respostas com fontes e incerteza explícita.

**Critérios de aceitação:** fontes são separadas de opinião; fatos atuais exigem busca; incerteza é declarada; nenhuma ação externa é feita sem política.

**Tarefas:** criar contrato de evidência; adapter de busca; deduplicação; controle de fontes; testes de síntese.

#### MX-053 — Skill de organização pessoal — P1

**História de usuário:** como usuário, quero transformar ideias em planos e tarefas, para organizar trabalho e vida pessoal.

**Critérios de aceitação:** skill distingue sugestão de gravação; mudanças persistentes exigem confirmação na primeira versão; tarefas possuem título, prioridade e estado; dados são privados.

**Tarefas:** criar modelo de plano; criar preview de alteração; criar tool de persistência com aprovação; integrar tarefas existentes; testar idempotência.

#### MX-054 — Skill de testes e qualidade — P2

**História de usuário:** como desenvolvedor, quero que o MX analise falhas, cobertura e riscos de teste, para melhorar a qualidade do software.

**Critérios de aceitação:** skill somente lê resultados autorizados; diferencia evidência de hipótese; não afirma que um teste foi executado sem evidência; gera plano de correção.

**Tarefas:** parser de resultados; contrato de relatório; tool de leitura; métricas de cobertura; testes com fixtures.

#### MX-055 — Skill financeira, saúde e jurídico isolada — P3

**História de usuário:** como usuário, quero receber informação organizada em domínios sensíveis, para entender opções sem que o sistema decida por mim.

**Critérios de aceitação:** escopo informa limites; não executa decisões; exige fontes e contexto; apresenta aviso apropriado; possui política e testes próprios.

**Tarefas:** criar skills separadas; revisar riscos; adicionar bloqueios de ação; configurar retenção; validar linguagem de incerteza.

### EPIC-06 — Tools seguras e autonomia

#### MX-060 — Definir contrato de tool — P0

**História de usuário:** como MX, quero representar ferramentas com schema e política, para impedir execução arbitrária.

**Critérios de aceitação:** tool declara nome, versão, argumentos, efeitos, timeout, autonomia mínima e resultado; argumentos são validados; erro não revela segredos.

**Tarefas:** criar `Tool`, `ToolDefinition`, `ToolRequest`, `ToolResult`; validar JSON/schema; criar registry; testar argumentos inválidos.

#### MX-061 — Implementar Policy Engine — P0

**História de usuário:** como usuário, quero que toda ação seja avaliada antes da execução, para impedir alterações inesperadas.

**Critérios de aceitação:** política considera usuário, skill, tool, argumentos, workspace e autonomia; leitura pode ser permitida; escrita e destruição exigem aprovação; recusa é auditável; prompt não pode substituir política.

**Tarefas:** criar `PolicyDecision`; criar `PolicyEngine`; definir níveis `ALLOW`, `DENY`, `REQUIRE_APPROVAL`; criar matriz de efeitos; escrever TDD antes do adapter.

#### MX-062 — Aprovar ou recusar ação — P1

**História de usuário:** como usuário, quero aprovar uma ação proposta, para manter controle sobre efeitos externos.

**Critérios de aceitação:** proposta possui resumo, argumentos seguros, efeito e expiração; aprovação é vinculada ao run; aprovação de uma tool não libera outra; recusa encerra ou retorna à skill.

**Tarefas:** entidade de approval; endpoints; expiração; idempotência; testes de replay e ownership; frontend posterior.

#### MX-063 — `workspace.list` — P1

**História de usuário:** como skill de desenvolvimento, quero listar um workspace permitido, para analisar projetos sem acesso ao disco inteiro.

**Critérios de aceitação:** raiz autorizada; traversal bloqueado; limites de profundidade, quantidade e tamanho; somente leitura; execução auditável; diretório inválido falha de forma segura.

**Tarefas:** criar workspace config; resolver path seguro; implementar adapter; testes de traversal, symlink, inexistência e limite.

#### MX-064 — `workspace.read_file` — P1

**História de usuário:** como skill de desenvolvimento, quero ler arquivos autorizados, para diagnosticar código e documentação.

**Critérios de aceitação:** mesma sandbox de path; limite de tamanho; binário não é lido como texto; extensão pode ser bloqueada; conteúdo não vira instrução de sistema automaticamente.

**Tarefas:** implementar tool; detectar charset/binário; truncar com indicação; testar path externo e arquivo grande; auditar leitura.

#### MX-065 — `git.status` — P1

**História de usuário:** como skill de desenvolvimento, quero consultar o estado do Git, para contextualizar alterações locais.

**Critérios de aceitação:** comando é fixo e não recebe shell arbitrário; workspace permitido; timeout; saída limitada; status e erro são separados.

**Tarefas:** adapter Git seguro; parser mínimo; testes com fixture; bloquear repositório externo; registrar duração.

#### MX-066 — Execução de escrita aprovada — P2

**História de usuário:** como usuário, quero que o MX altere arquivos somente após aprovação clara, para obter automação sem perder controle.

**Critérios de aceitação:** diff é apresentado antes da escrita; aprovação expira; backup ou reversão existe; path é validado; execução é idempotente quando possível; escrita sem aprovação é impossível.

**Tarefas:** modelo de patch; preview; approval; aplicação atômica; rollback; testes de concorrência e falha parcial.

### EPIC-07 — Runs, tarefas assíncronas e recuperação

#### MX-070 — Persistir execução e estado — P0

**História de usuário:** como operador, quero persistir runs e transições, para explicar e recuperar uma solicitação.

**Critérios de aceitação:** cada run possui owner, correlationId, skill, estado, timestamps, input resumido, output seguro e erro codificado; transição inválida falha.

**Tarefas:** migration; entidade; repository; aggregate; casos de uso; testes de transição e ownership.

#### MX-071 — Cancelar execução — P1

**História de usuário:** como usuário, quero cancelar uma execução longa, para recuperar controle.

**Critérios de aceitação:** cancelamento autorizado pelo owner; run muda para `CANCELLED`; tool cooperativa respeita cancelamento; resposta informa estado; não há falsa conclusão.

**Tarefas:** token de cancelamento; endpoint; integração com gateway; testes de corrida.

#### MX-072 — Idempotência e retry — P1

**História de usuário:** como sistema, quero evitar duplicação em reenvios, para não criar mensagens ou ações repetidas.

**Critérios de aceitação:** requestId ou chave idempotente; retry limitado; tool com efeito possui chave; duplicata retorna resultado conhecido ou estado atual.

**Tarefas:** modelo de idempotency key; índice; política de retry; testes de repetição e falha no meio.

#### MX-073 — Fila de execução — P2

**História de usuário:** como usuário, quero que tarefas longas não bloqueiem a API, para continuar usando o MX.

**Critérios de aceitação:** request retorna runId; worker processa; estados são observáveis; fila possui limite; shutdown não perde estado.

**Tarefas:** escolher mecanismo local; worker; retry de job; shutdown gracioso; teste de recuperação.

### EPIC-08 — Memória e contexto

#### MX-080 — Janela de contexto recente — P0

**História de usuário:** como usuário, quero que o MX considere mensagens recentes, para manter continuidade sem enviar histórico ilimitado.

**Critérios de aceitação:** limite configurável; mensagens ordenadas; owner aplicado; contexto separado de instruções; tamanho medido.

**Tarefas:** `ConversationContextPort`; query limitada; truncamento; testes de ordem, limite e privacidade.

#### MX-081 — Memória confirmada — P1

**História de usuário:** como usuário, quero revisar e excluir memórias, para evitar que o MX trate suposições como fatos pessoais.

**Critérios de aceitação:** mensagem não vira memória automaticamente; memória possui origem, confiança, status e timestamps; usuário pode confirmar, editar e excluir.

**Tarefas:** entidade; casos de uso; endpoints; approval de gravação; testes de ownership e exclusão.

#### MX-082 — Recuperação semântica — P2

**História de usuário:** como usuário, quero que o MX recupere memórias relevantes, para responder melhor em grandes históricos.

**Critérios de aceitação:** embeddings são versionados; recuperação possui limite; dados filtrados por owner; latência medida; memória irrelevante não domina o contexto.

**Tarefas:** escolher armazenamento local; pipeline de indexação; adapter de embeddings; avaliação de relevância; reindexação.

### EPIC-09 — Observabilidade, auditoria e operação

#### MX-090 — Health checks — P0

**História de usuário:** como operador, quero saber se MX, banco, Redis e Ollama estão disponíveis, para diagnosticar falhas rapidamente.

**Critérios de aceitação:** endpoint de health diferencia serviço e dependências; detalhes sensíveis são protegidos; startup informa configuração inválida.

**Tarefas:** Actuator; health indicators; perfil local; teste de contexto; documentação de diagnóstico.

#### MX-091 — Métricas de skills e modelo — P0

**História de usuário:** como operador, quero medir roteamento, duração e falhas, para melhorar rapidez e qualidade.

**Critérios de aceitação:** métricas não usam prompt ou dados pessoais como tags; existe contagem por skill e outcome; duração de modelo é registrada; endpoints são protegidos.

**Tarefas:** `SkillTelemetry`; Micrometer adapter; métricas do gateway; limites de cardinalidade; testes de registro.

#### MX-092 — Logs estruturados e correlação — P1

**História de usuário:** como operador, quero rastrear uma execução por correlationId, para investigar problemas.

**Critérios de aceitação:** lifecycle gera eventos; segredos não aparecem; prompts e respostas obedecem política de retenção; erro possui código estável.

**Tarefas:** MDC; eventos; redaction; error taxonomy; testes de logging essencial.

#### MX-093 — Auditoria persistente — P1

**História de usuário:** como usuário e operador, quero saber quais skills e tools foram usadas, para ter transparência.

**Critérios de aceitação:** auditoria registra decisão, skill, versão, tool, aprovação, estado e duração; conteúdo sensível é minimizado; consulta respeita owner e retenção.

**Tarefas:** entidade; repository; caso de uso; política de retenção; endpoints administrativos locais; testes.

### EPIC-10 — Frontend e experiência do usuário

#### MX-100 — Shell central de conversa — P1

**História de usuário:** como usuário, quero uma interface única de conversa, para não precisar escolher agents manualmente.

**Critérios de aceitação:** chat envia ao MX; histórico aparece; erros são compreensíveis; autenticação é tratada; layout funciona em resolução local comum.

**Tarefas:** tela; client API; estados loading/error; persistência de conversationId; testes de componentes.

#### MX-101 — Mostrar skill e progresso — P1

**História de usuário:** como usuário, quero entender qual especialidade foi usada e em que estado está, para confiar no resultado.

**Critérios de aceitação:** skill, confiança e estado podem ser mostrados; detalhes técnicos não poluem a resposta; correlationId fica disponível para diagnóstico.

**Tarefas:** contrato de status; componentes; polling/SSE; fallback para resposta síncrona; testes.

#### MX-102 — Aprovação de ações — P1

**História de usuário:** como usuário, quero revisar e aprovar propostas, para permitir automação consciente.

**Critérios de aceitação:** resumo do efeito; diff ou argumentos; expiração; aprovar e recusar; não há aprovação genérica.

**Tarefas:** tela de approval; client; estados; tratamento de expiração; testes.

#### MX-103 — Histórico, memória e configurações — P2

**História de usuário:** como usuário, quero administrar conversas, memórias, workspace e preferências, para controlar o comportamento do MX.

**Critérios de aceitação:** operações respeitam owner; alterações importantes exibem confirmação; memória pode ser excluída; configuração inválida é explicada.

**Tarefas:** telas; filtros; paginação; confirmação; integração com endpoints.

### EPIC-11 — Infraestrutura local e DevOps

#### MX-110 — Compose reproduzível — P0

**História de usuário:** como operador, quero iniciar dependências localmente, para usar o MX sem configuração manual frágil.

**Critérios de aceitação:** Postgres, Redis e Ollama possuem configuração clara; healthchecks; volumes; rede; credenciais de desenvolvimento separadas; shutdown não perde dados persistidos.

**Tarefas:** revisar Compose; alinhar host Ollama; profiles; volumes; healthchecks; documentação Windows.

#### MX-111 — Scripts de início e diagnóstico — P0

**História de usuário:** como operador, quero um comando de início que valide pré-requisitos, para não investigar erros genéricos.

**Critérios de aceitação:** script detecta Java, Docker, Compose, portas e serviços; falha com causa e comando de correção; não apaga dados automaticamente.

**Tarefas:** `start.bat`; `start.sh`; `doctor`; códigos de saída; testes manuais documentados.

#### MX-112 — Backup e restauração local — P1

**História de usuário:** como usuário, quero preservar conversas, memórias e configurações, para não perder meu histórico.

**Critérios de aceitação:** backup inclui banco e configuração não secreta; restauração é documentada; arquivo é validado; backup não inclui segredos em texto desprotegido.

**Tarefas:** scripts; manifesto; checksum; teste de restauração; documentação.

#### MX-113 — Performance local — P1

**História de usuário:** como usuário, quero respostas rápidas, para usar o MX no trabalho diário.

**Critérios de aceitação:** p50 e p95 do caminho principal são medidos; prompt e contexto têm limites; consultas não fazem N+1; chamadas ao modelo possuem timeout.

**Tarefas:** benchmark; índices; cache somente onde seguro; medir memória; registrar baseline.

### EPIC-12 — Integrações controladas

#### MX-120 — Conector de repositório Git — P2

**História de usuário:** como desenvolvedor, quero analisar repositórios autorizados, para receber ajuda contextualizada.

**Critérios de aceitação:** workspace é explicitamente cadastrado; operações iniciais são somente leitura; credenciais não são passadas ao modelo; tool possui auditoria.

**Tarefas:** configuração de workspace; adapter Git; allowlist; testes; documentação.

#### MX-121 — Conector de notas pessoais — P2

**História de usuário:** como usuário, quero consultar notas autorizadas, para recuperar conhecimento pessoal.

**Critérios de aceitação:** escopo de pastas; leitura controlada; indexação opcional; exclusão respeitada; privacidade preservada.

**Tarefas:** adapter de filesystem; indexação; filtros; testes de traversal; política de retenção.

#### MX-122 — Integrações externas com aprovação — P3

**História de usuário:** como usuário, quero conectar serviços externos, para ampliar o MX sem perder controle.

**Critérios de aceitação:** cada integração possui credencial isolada; escopo mínimo; actions exigem aprovação; revogação é possível; falha externa não corrompe o run.

**Tarefas:** camada de connector; secrets; OAuth/API key; health; approval; auditoria; testes de contrato.

### EPIC-13 — Segurança avançada e confiabilidade

#### MX-130 — Resistir a prompt injection — P1

**História de usuário:** como usuário, quero que arquivos e fontes não consigam reprogramar o MX, para preservar a política do sistema.

**Critérios de aceitação:** dados recuperados são claramente separados de instruções; tool policy não pode ser alterada por texto; conteúdo suspeito é tratado como dado; testes adversariais existem.

**Tarefas:** modelo de mensagem; sanitização contextual; policy enforcement; fixtures de ataque; testes de regressão.

#### MX-131 — Privacidade e retenção — P1

**História de usuário:** como usuário, quero controlar o que é armazenado, para manter privacidade local.

**Critérios de aceitação:** retenção é configurável; exclusão remove histórico e memória conforme política; logs minimizam PII; exportação é controlada.

**Tarefas:** política; jobs locais; redaction; endpoint de exclusão; testes.

#### MX-132 — Testes end-to-end e threat model — P1

**História de usuário:** como equipe, quero validar o fluxo completo e ameaças principais, para evitar segurança apenas documental.

**Critérios de aceitação:** cenário login-chat-skill-tool é automatizado; cross-user access falha; traversal falha; approval replay falha; indisponibilidade Ollama é segura.

**Tarefas:** ambiente de teste; fixtures; testes E2E; threat model; relatório de riscos; correções priorizadas.

### EPIC-14 — API e canais multiplataforma

#### MX-140 — Publicar contrato API versionado — P0

**História de usuário:** como cliente web ou mobile, quero consumir uma API versionada e documentada, para que os canais evoluam sem duplicar regras do MX Core.

**Critérios de aceitação:** endpoints principais estão sob `/api/v1`; DTOs não expõem entidades JPA; erros possuem formato estável; OpenAPI descreve autenticação, conversação, runs e aprovação; compatibilidade de versão é documentada.

**Tarefas:** criar envelope de erro; definir DTOs; adicionar documentação OpenAPI; versionar controllers; gerar cliente/types compartilhados; testar contratos de sucesso e falha.

#### MX-141 — Autenticar web e mobile com sessão revogável — P0

**História de usuário:** como usuário, quero iniciar e encerrar sessão em qualquer canal, para acessar o MX com identidade e revogação consistentes.

**Critérios de aceitação:** access token é curto; refresh é controlado; logout revoga a sessão; web não expõe segredo em URL; mobile usa armazenamento seguro; token revogado falha em todos os canais.

**Tarefas:** concluir MX-011/MX-012; definir CORS e CSRF; criar cliente de auth web; criar armazenamento seguro mobile; testar expiração, refresh, logout e acesso cruzado.

#### MX-142 — Disponibilizar workspace central web — P1

**História de usuário:** como usuário, quero conversar com o MX pelo navegador, para utilizar o assistente em desktop e telas responsivas.

**Critérios de aceitação:** login, lista de conversas, envio de mensagem, estado de run, resposta Markdown e aprovação funcionam; carregamento, vazio, offline e erro possuem estados claros; nenhuma chamada é feita diretamente do componente para endpoint não tipado.

**Tarefas:** transformar `frontend/` em React + TypeScript; criar API client; criar layout; criar chat; criar painel de run; adicionar testes de componentes e fluxo principal.

#### MX-143 — Disponibilizar app compartilhado Android/iOS — P1

**História de usuário:** como usuário, quero usar o mesmo MX em Android e iOS, para continuar conversas fora do desktop.

**Critérios de aceitação:** projeto Expo inicia nos dois sistemas; login seguro; conversa e histórico funcionam; safe area e teclado são tratados; erros de API são visíveis; código compartilhado não usa APIs nativas sem adapter.

**Tarefas:** criar `mobile/`; configurar Expo Router; criar tema; criar auth; criar telas de chat e histórico; criar cliente API; adicionar testes de hooks e componentes; validar Android emulator, iOS simulator e web Expo.

#### MX-144 — Sincronizar conversas e runs entre canais — P1

**História de usuário:** como usuário, quero iniciar uma execução em um canal e acompanhá-la em outro, para não perder contexto.

**Critérios de aceitação:** `conversationId`, `runId` e `correlationId` são retornados; histórico pertence ao usuário; polling ou SSE recupera estados; reconexão não duplica mensagens; estado final é idempotente.

**Tarefas:** concluir MX-021/MX-023/MX-070; criar endpoints de consulta; criar cursor ou `updatedSince`; implementar cache e reconexão web/mobile; testar troca de canal.

#### MX-145 — Aprovar ações sensíveis em qualquer canal — P1

**História de usuário:** como usuário, quero aprovar ou rejeitar uma tool pelo web ou mobile, para manter controle sobre ações sensíveis.

**Critérios de aceitação:** aprovação é vinculada ao usuário e ao run; nonce não pode ser reutilizado; rejeição encerra ou retorna o run a estado seguro; outro usuário não consegue aprovar; UI mostra tool, efeito e motivo.

**Tarefas:** integrar `ExecutionRun` com `ToolExecutor`; criar endpoint de approval; persistir decisão; criar componente web; criar tela mobile; testar replay, expiração, concorrência e cross-user.

#### MX-146 — Notificar conclusão e aprovação pendente — P2

**História de usuário:** como usuário, quero ser notificado quando o MX terminar ou precisar de aprovação, para não manter o aplicativo aberto.

**Critérios de aceitação:** notificações podem ser habilitadas ou desabilitadas; payload não contém segredo; deep link abre o run correto; entrega duplicada é tolerada; falha de notificação não falha a execução.

**Tarefas:** criar porta de notificação; adapter local/push; registrar device; deep links; preferências; testes de idempotência e redaction.

#### MX-147 — Permitir rascunho e reconexão controlada — P2

**História de usuário:** como usuário, quero preservar rascunhos e recuperar a interface após perda de conexão, para não perder trabalho.

**Critérios de aceitação:** rascunho não é enviado automaticamente; fila offline não executa tools; retry possui limite; mensagens têm idempotency key; conflito é informado ao usuário.

**Tarefas:** storage local por canal; idempotência no backend; estados offline; reconciliação; testes de queda e retomada.

## 6. Primeiro ciclo de execução imediato

Como o MX Core, o registry, o roteador, as skills `general` e `development` e a telemetria básica já existem, o próximo incremento obrigatório é o **EPIC-06 em modo seguro**, começando pelo contrato de tool e pelo Policy Engine. A sequência imediata é:

| Ordem | Item | Tipo | Resultado esperado |
|---:|---|---|---|
| 1 | MX-060 | História | Contrato de tool e registry sem execução arbitrária |
| 2 | MX-061 | História | Decisão `ALLOW`, `DENY` ou `REQUIRE_APPROVAL` testada |
| 3 | MX-063 | História | `workspace.list` somente leitura e sandboxed |
| 4 | MX-070 | História | Run persistido com estados e correlationId |
| 5 | MX-093 | História | Auditoria dos eventos de skill e tool |
| 6 | MX-051 | História | Skill de desenvolvimento usa tools reais em leitura |
| 7 | MX-140 | História | API v1 e contrato OpenAPI |
| 8 | MX-141 | História | Sessão revogável consumível por web/mobile |
| 9 | MX-142 | História | Cliente universal web central |
| 10 | MX-143 | História | Cliente Expo Android/iOS |
| 11 | MX-144 | História | Sincronização de conversas e runs |
| 12 | MX-145 | História | Aprovação sensível em qualquer canal |

A implementação deve seguir Red-Green-Refactor: escrever os testes dos contratos, implementar o mínimo, integrar o adapter, executar a suíte e somente então avançar para a próxima história.

## 7. Estado atual de execução

| Item | Estado | Evidência |
|---|---|---|
| MX-041 — Registry de skills | Concluído | `SkillRegistry` com teste de unicidade |
| MX-042 — Roteamento explicável | Concluído | `SkillRouter` com confiança, motivo, fallback e testes |
| MX-051 — Primeiras skills | Inicial concluído | `general` e `development` registradas no MX Core |
| MX-060 — Contrato e registry de tools | Concluído | `Tool`, `ToolDefinition`, `ToolRequest`, `ToolResult`, `ToolRegistry` e testes |
| MX-061 — Policy Engine | Inicial concluído | `ALLOW`, `DENY`, `REQUIRE_APPROVAL` e testes de autonomia |
| MX-091 — Métricas básicas | Inicial concluído | Actuator, telemetria por skill e endpoint local de métricas |
| MX-063 — `workspace.list` | Concluído | Adapter somente leitura com root autorizado, traversal, symlink e limites testados |
| MX-070 — Runs persistidos | Fundação implementada | Aggregate, lifecycle, snapshot, porta, adapter JPA e migration V4; falta integrar ao fluxo central |
| MX-140 — API versionada | Concluído | `api/v1` de conversação, DTOs estáveis, mapper, controller e OpenAPI |
| MX-141 — Sessão multiplataforma | Fundação concluída | JWT com `sessionId`, validação persistida, logout revogável e CORS; refresh rotativo ainda pendente |
| MX-142 — Web | Fundação concluída | `clients/mx-app` executa como web Expo e consome API v1 tipada |
| MX-143 — Android/iOS | Fundação concluída | `clients/mx-app` é universal Expo, com SecureStore nativo e configuração de ambos os pacotes |
| MX-144 — Sincronização | Planejado | Requer histórico, runs consultáveis e polling/SSE |
| MX-145 — Aprovação cross-channel | Planejado | Requer integração do ExecutionRun ao ToolExecutor e endpoint de approval |

A suíte do backend foi executada com sucesso em target temporário após a implementação da sessão revogável e do CORS. O cliente universal foi validado com `npm run typecheck`, export web e `expo-doctor` com 18/18 verificações aprovadas. O próximo bloqueador de produto é completar refresh rotativo e integrar o lifecycle persistido ao fluxo central antes de construir sincronização e aprovações entre canais.

## 8. Definition of Ready

Uma história entra em desenvolvimento quando possui objetivo de usuário, escopo, critérios de aceitação, dependências, riscos, regra de segurança, estratégia de teste e definição explícita do que está fora do escopo. Uma dúvida só deve ser levada ao PO quando mudar arquitetura, segurança, dados irreversíveis ou prioridade; detalhes reversíveis serão decididos pela implementação.

## 9. Definition of Done

Uma história concluída possui testes automatizados relevantes, implementação alinhada à Clean Architecture, dependências SOLID, tratamento seguro de erros, auditoria quando aplicável, documentação atualizada, validação local, métricas mínimas e nenhuma regressão conhecida. A entrega deve ser pequena o suficiente para ser revisada e revertida.

## 10. Métricas de produto e engenharia

| Dimensão | Métrica | Meta inicial |
|---|---|---|
| Valor | Solicitações respondidas com sucesso | Crescente por sprint |
| Qualidade | Falha por chamada ao modelo | Redução contínua; nenhuma resposta falsa |
| Velocidade | p50/p95 da resposta local | Medir antes de otimizar |
| Segurança | Ações bloqueadas e aguardando aprovação | 100% das ações sensíveis protegidas |
| Confiabilidade | Runs presos em estado intermediário | Zero após recuperação |
| Produto | Solicitações roteadas para skill correta | Avaliar com fixtures e feedback |
| Engenharia | Tempo de feedback unitário | Curto o suficiente para TDD contínuo |
| Operação | Health e dependências disponíveis | Diagnóstico explícito |

## 11. Cadência de entrega

Cada incremento terá quatro momentos: planejamento técnico da história, TDD do contrato, implementação da menor solução segura e validação com atualização do backlog. Não serão abertas várias skills simultaneamente enquanto Policy Engine, Tools e Runs não estiverem estabilizados. O foco é criar uma plataforma central confiável antes de aumentar o número de especialistas.
