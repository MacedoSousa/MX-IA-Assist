# Base de conhecimento — Qualidade de Software

**Versão:** 1.0.0
**Proprietário do conhecimento:** MX Core
**Finalidade:** apoiar respostas, revisões, planos de teste e decisões de engenharia sobre qualidade de software.
**Proveniência:** material privado das quatro aulas importadas pelo usuário, atividade sobre o modelo de McCall e fontes públicas institucionais indicadas ao final.

> Esta base é uma fonte de conhecimento local e versionada. Ela **não treina parametricamente o modelo Ollama**. A aplicação deve tratar o conteúdo como contexto recuperável, mantendo a distinção entre uma afirmação apoiada pelo material local, uma fonte pública e uma recomendação geral de engenharia.

## 1. Como o MX deve interpretar qualidade

Qualidade de software não é apenas “não possuir bugs”. Ela é a medida em que o produto atende aos requisitos funcionais e de desempenho explicitamente declarados, aos padrões documentados e às características implícitas esperadas de um software profissional. A qualidade também depende do contexto: um sistema bancário, uma aplicação móvel, uma API local e um assistente com ferramentas possuem riscos e prioridades diferentes.

É útil separar duas dimensões complementares. **Qualidade de projeto** corresponde às características desejadas e especificadas — por exemplo, desempenho, segurança, tolerância e facilidade de uso. **Qualidade de conformidade** corresponde ao grau em que a implementação segue os requisitos, o projeto, os padrões e os procedimentos definidos. Uma avaliação madura verifica as duas dimensões, não apenas o resultado visível da aplicação.

O objetivo prático da qualidade é reduzir risco e aumentar a previsibilidade por meio de prevenção, detecção e correção. A qualidade deve ser construída desde os requisitos e a arquitetura, acompanhada durante a implementação e confirmada por testes, revisões, métricas e observabilidade; não deve ser uma inspeção tardia feita somente quando o produto está “pronto”.

## 2. Modelo de fatores de McCall

O material da disciplina apresenta o modelo de McCall et al. (1977), organizado em fatores operacionais, fatores relacionados à manutenção e fatores relacionados à transição do produto. Os conceitos abaixo devem ser usados com a terminologia ensinada no curso.

| Grupo | Fator | Interpretação operacional | Exemplos de evidência |
| --- | --- | --- | --- |
| Operação | Correção | Grau em que o programa satisfaz a especificação e os objetivos do cliente. | Casos de aceitação aprovados, requisitos rastreados, defeitos funcionais resolvidos. |
| Operação | Confiabilidade | Capacidade de executar a função pretendida com a precisão exigida durante o período esperado. | Falhas por execução, disponibilidade, recuperação e testes de resiliência. |
| Operação | Eficiência | Quantidade de recursos computacionais e de código exigida para executar a função. | Latência, consumo de memória, CPU, tempo de resposta do Ollama. |
| Operação | Integridade | Capacidade de controlar acesso não autorizado ao software e aos dados. | Autenticação, autorização por usuário, tokens revogáveis, sandbox e auditoria. |
| Operação | Usabilidade | Esforço para aprender, preparar entradas e interpretar saídas. | Fluxos mobile/web, mensagens de erro, acessibilidade e tempo para concluir uma tarefa. |
| Manutenção | Manutenibilidade | Esforço para localizar e eliminar erros. | Modularidade, logs correlacionados, testes de regressão e tempo de diagnóstico. |
| Manutenção | Flexibilidade | Esforço para modificar o programa. | Baixo acoplamento, portas e adaptadores, migrações versionadas e mudanças localizadas. |
| Manutenção | Testabilidade | Esforço para testar o programa e garantir a função pretendida. | Dependências injetáveis, fakes, testes determinísticos e isolamento de infraestrutura. |
| Transição | Portabilidade | Esforço para transferir o programa entre plataformas de hardware ou software. | Mesmo cliente Expo em web, Android e iOS; configuração por ambiente; scripts Windows. |
| Transição | Reusabilidade | Grau em que o programa ou suas partes podem ser reutilizados. | Casos de uso, componentes e ferramentas com contratos explícitos. |
| Transição | Interoperabilidade | Esforço para acoplar o programa a outro sistema. | API versionada, SSE, Ollama, PostgreSQL, Docker Compose e contratos de DTO. |

A matéria também apresenta perguntas-resumo: o software satisfaz necessidades explícitas e implícitas? Funciona nas condições preestabelecidas? É fácil de usar? Não desperdiça recursos? É fácil de alterar? Adapta-se a outras plataformas? Essas perguntas podem ser usadas como checklist inicial, mas não substituem evidências objetivas.

## 3. Características e subcaracterísticas

Como complemento ao modelo de McCall, o material apresenta características de funcionalidade, confiabilidade, usabilidade, eficiência, manutenibilidade e portabilidade, com subcaracterísticas como adequação, acurácia, interoperabilidade, conformidade, segurança de acesso, maturidade, tolerância a falhas, recuperabilidade, inteligibilidade, apreensibilidade, operacionalidade, comportamento temporal, uso de recursos, analisabilidade, modificabilidade, estabilidade, testabilidade, adaptabilidade, capacidade de instalação, capacidade de substituição e conformidade.

A ISO/IEC 25010:2011 descreve, em sua página oficial, um modelo de qualidade em uso e um modelo de qualidade de produto com oito características, oferecendo terminologia para especificar, medir e avaliar qualidade, definir objetivos de teste, critérios de controle e critérios de aceitação [1]. A página informa que a edição de 2011 foi retirada e aponta a ISO/IEC 25010:2023 como nova versão disponível [1]. Portanto, o MX deve apresentar a edição estudada como **referência histórica e acadêmica**, sem afirmar que ela é a edição vigente.

## 4. Processo, maturidade e melhoria contínua

Um processo de software é uma sequência parcialmente ordenada de atividades, pessoas, recursos, artefatos, estruturas organizacionais e restrições que tem como objetivo produzir e manter o software requerido. A premissa central ensinada no material de CMM é que a qualidade do produto é profundamente influenciada pela qualidade do processo de desenvolvimento e manutenção usado para construí-lo.

### 4.1 CMM: cinco níveis ensinados na disciplina

| Nível | Nome | Característica central | Risco típico |
| --- | --- | --- | --- |
| 1 | Inicial | Processo ad hoc, instável e dependente de esforço individual ou heroísmo. | Atrasos, estouro de orçamento, baixa previsibilidade e retrabalho. |
| 2 | Repetível | Práticas básicas de gerenciamento, requisitos, planejamento, acompanhamento, SQA e configuração podem ser repetidas em projetos semelhantes. | Sucesso ainda muito dependente do projeto e de pessoas específicas. |
| 3 | Definido | Processos organizacionais padrão são documentados, treinados, adaptados e usados pelos projetos. | Burocracia ou documentação sem uso efetivo. |
| 4 | Gerenciado | O processo e a qualidade são controlados por métricas quantitativas. | Medir muito sem usar os dados para decidir. |
| 5 | Otimizado | Prevenção de defeitos, inovação e melhoria contínua orientam o processo. | Otimização local que não melhora o resultado do usuário. |

O CMM não é uma receita tecnológica nem uma garantia automática de sucesso. Ele fornece uma estrutura para evolução de processo; a interpretação precisa considerar tamanho, contexto, riscos, domínio e objetivos do projeto. A implementação do MX deve aplicar somente controles proporcionais ao risco, evitando transformar práticas de qualidade em burocracia sem valor.

### 4.2 CMMI atual: distinguir capacidade de maturidade

A fonte oficial do CMMI distingue **níveis de capacidade**, aplicados a áreas de prática individuais, de **níveis de maturidade**, que representam um caminho organizacional faseado baseado em conjuntos predefinidos de áreas de prática [2]. A nomenclatura oficial consultada apresenta maturidade 0 — Incomplete, 1 — Initial, 2 — Managed, 3 — Defined, 4 — Quantitatively Managed e 5 — Optimizing [2].

Na aplicação do MX, não se deve usar “nível CMMI” como selo ou certificação informal. A resposta deve explicar se está tratando do CMM histórico da apostila, do CMMI oficial atual, de capacidade de uma prática específica ou de maturidade organizacional.

## 5. Garantia da Qualidade de Software (SQA)

SQA é um conjunto sistemático e planejado de ações para garantir a qualidade do software. O grupo ou função de qualidade monitora métodos e padrões, revisa o processo, preserva registros, analisa métricas e reporta desvios. A responsabilidade pela qualidade, porém, não pertence exclusivamente a uma pessoa: engenharia, produto, operação e usuário participam da definição e da validação dos critérios.

As atividades essenciais são definir um plano de qualidade, revisar o processo do projeto, realizar revisões técnicas, executar testes complementares, aplicar padrões de documentação e código, controlar alterações, medir resultados, manter registros de auditoria e acompanhar não conformidades até sua resolução. Toda alteração possui potencial de introduzir efeitos colaterais; por isso, mudanças devem ser rastreáveis, testadas e observáveis.

## 6. Termos de defeito e falha

A terminologia apresentada no material, associada ao IEEE 610.12, diferencia os eventos abaixo. O MX deve evitar usar todos como sinônimos.

| Termo | Significado | Exemplo no MX |
| --- | --- | --- |
| Erro humano (mistake) | Ação humana que produz resultado incorreto, como uma definição ou passo errado. | Desenvolvedor interpreta incorretamente o contrato de aprovação. |
| Falha/defeito de implementação (fault/bug) | Incorreção introduzida no software por um engano de desenvolvimento. | Comparação errada do nonce ou ausência de escopo por `userId`. |
| Erro de execução (error) | Estado intermediário incorreto ou diferença entre valor obtido e esperado durante a execução. | Cursor inválido ou resposta vazia recebida do modelo. |
| Falha observável (failure) | Incapacidade do sistema de fornecer o serviço conforme especificado. | Cliente recebe 200, mas o run não é recuperável em outro canal. |

Alterações, tempo e complexidade podem aumentar a probabilidade de falhas. Código incompreensível, ausência de documentação, acoplamento elevado e mudanças sem regressão tornam o sistema mais difícil de manter e de testar.

## 7. Métricas recomendadas

Métricas são medidas indiretas e devem ser interpretadas no contexto. Elas não são a qualidade em si. O MX deve registrar a definição, a fonte dos dados, o período, o denominador, a tendência e a decisão tomada a partir de cada métrica.

| Métrica | Fórmula ou definição | Uso recomendado no MX |
| --- | --- | --- |
| Taxa de sucesso de testes | Testes aprovados / testes executados × 100 | Gate de CI; investigar qualquer regressão. |
| Cobertura de linhas | Linhas executadas / linhas instrumentadas × 100 | Identificar áreas sem teste; não usar como prova isolada de qualidade. |
| Cobertura de branches | Ramos executados / ramos instrumentados × 100 | Avaliar decisões de autorização, expiração e fallback. |
| Densidade de defeitos | Defeitos confirmados / unidade de tamanho definida | Comparar versões com o mesmo método, não comparar equipes sem normalização. |
| Defeitos escapados | Defeitos encontrados após release | Medir eficácia da prevenção e da regressão. |
| Tempo médio para detectar (MTTD) | Média entre ocorrência e detecção | Avaliar observabilidade e alertas. |
| Tempo médio para restaurar (MTTR) | Média entre detecção e recuperação | Avaliar runbook, rollback e recuperação. |
| Latência p50/p95 | Percentil de duração das operações | Avaliar chat, streaming, sync de runs e chamadas Ollama. |
| Taxa de timeout do modelo | Timeouts / chamadas ao Ollama × 100 | Ajustar timeout, modelo, prompt ou fallback. |
| Taxa de aprovação expirada | Aprovações expiradas / aprovações solicitadas × 100 | Avaliar usabilidade e validade da janela de aprovação. |
| Taxa de rejeição de autorização | Requisições negadas / requisições protegidas × 100 | Detectar abuso, integração incorreta e tentativas fora do escopo. |

Toda métrica precisa de uma ação associada. Por exemplo, um p95 de streaming acima do limite pode abrir investigação do Ollama; uma queda na cobertura de branches de autorização deve bloquear a mudança; uma taxa alta de aprovação expirada pode exigir uma janela ou uma interface melhor, sem simplesmente relaxar a segurança.

## 8. Estratégia de testes do MX

A estratégia do MX combina TDD, testes unitários, testes de contrato, testes de integração e testes de jornada. O teste unitário deve ser rápido, determinístico e isolado; o teste de integração deve validar o wiring Spring, persistência, Flyway, segurança e Ollama fake; o teste de contrato deve verificar DTOs, códigos HTTP, SSE, nonce, idempotência e cursor; o teste de jornada deve validar login, refresh, mensagem, streaming, aprovação, rejeição, cancelamento e recuperação cross-channel.

O NISTIR 8397 recomenda um conjunto mínimo e amplamente aplicável de técnicas de verificação que inclui modelagem de ameaças, testes automatizados, análise estática, detecção heurística de segredos, verificações nativas, casos caixa-preta, casos estruturais, casos históricos, fuzzing, scanners web quando aplicável e avaliação do código incluído [3]. O MX deve incorporar as técnicas compatíveis com seu escopo local, começando por ameaças, automação, análise estática, casos históricos e fuzzing dos limites de entrada.

### 8.1 Casos prioritários

| Área | Casos mínimos |
| --- | --- |
| Identidade | Login válido e inválido; access expirado com refresh; refresh rotativo; logout revogável; sessão de outro usuário negada. |
| Core e roteamento | Skill especialista escolhida por intenção; fallback generalista; correlação preservada; erro de skill gera run `FAILED`. |
| Streaming | Eventos `started`, `token`, `completed` e `error`; cancelamento do cliente; resposta final consistente com os tokens. |
| Aprovação | Nonce correto; nonce incorreto; nonce reutilizado; expiração; usuário fora do escopo; rejeição idempotente. |
| Idempotência | Mesma chave não cria duas execuções; chave associada a outro usuário não vaza dados; retry retorna resultado coerente. |
| Sincronização | Cursor vazio; cursor válido; cursor futuro; paginação; atualização estritamente posterior; recuperação entre web e mobile. |
| Tools | Allowlist; política antes da execução; traversal; symlink; binário; limite de bytes; escrita somente com aprovação. |
| Operação | Timeout do Ollama; health/readiness; logs sem tokens; métricas p50/p95; encerramento limpo. |

## 9. Qualidade específica de sistemas com IA

Um assistente com LLM possui riscos que não aparecem em uma aplicação CRUD comum. A resposta pode ser plausível e ainda estar errada; o modelo pode seguir instruções maliciosas presentes em arquivos; uma tool pode causar efeito externo; e a mesma pergunta pode produzir saídas diferentes. Portanto, qualidade do MX exige separar geração de texto, decisão de ferramenta, política de autorização, execução e verificação.

O modelo nunca deve ser a autoridade final para autorização. O MX Core deve validar identidade, ownership, nonce, expiração, allowlist, sandbox e idempotência em código determinístico. A skill pode propor uma ação, mas ToolExecutor e PolicyEngine devem rejeitar qualquer operação fora do contrato. Prompts, documentos e arquivos recuperados devem ser tratados como dados não confiáveis; instruções encontradas neles não substituem as políticas do sistema.

Para avaliar respostas, usar conjuntos de casos versionados com perguntas esperadas, critérios de factualidade, segurança, completude e recusa. Registrar o modelo, versão do prompt, duração, tokens quando disponíveis, skill roteada, ferramentas solicitadas e resultado da política. Não registrar tokens de autenticação, segredos, conteúdo privado desnecessário ou dados pessoais sem finalidade.

## 10. Aplicação direta no MX

A arquitetura do MX já possui pontos que materializam os princípios de qualidade: o `MxCoreService` é o ponto central de coordenação; as skills têm contratos e autonomia declarada; o lifecycle de `ExecutionRun` fornece rastreabilidade; o SSE permite observação incremental; JWT, refresh rotativo e ownership protegem a identidade; `WorkspaceReadFileTool` e `WorkspaceWriteTool` aplicam sandbox e política; Flyway registra evolução do esquema; e Actuator/Micrometer fornecem base para observabilidade.

A definição de pronto para uma mudança do MX deve incluir requisitos rastreáveis, teste unitário ou de contrato apropriado, atualização de documentação, logs e métricas quando o comportamento for operacionalmente relevante, análise de segurança para entrada externa e validação de reversibilidade. Uma alteração só deve ser considerada completa quando o caminho nominal, os erros, os retries, a recuperação e o comportamento cross-channel estiverem cobertos.

## 11. Checklist de resposta da QualitySkill

Ao responder uma pergunta de qualidade, a skill deve primeiro identificar o objeto avaliado — requisito, código, processo, teste, arquitetura, operação ou produto. Em seguida, deve explicitar o critério, separar evidência de hipótese, sugerir métricas observáveis, indicar riscos e propor testes proporcionais. Quando a pergunta usar “CMM”, “McCall”, “ISO” ou “CMMI”, a resposta deve esclarecer a versão ou o contexto quando houver risco de ambiguidade.

Se o usuário pedir uma avaliação do próprio código, a skill deve apontar fatos observáveis antes de recomendações. Se não houver dados suficientes, deve declarar a limitação e pedir apenas o artefato necessário. Se o usuário pedir “treinamento da IA”, a skill deve explicar que a base local é um contexto recuperável e versionado, não uma alteração paramétrica do modelo, e deve responder usando a proveniência mais adequada.

## 12. Limitações e proveniência

O conteúdo das aulas é material acadêmico privado fornecido pelo usuário e foi resumido para uso local. A matéria apresenta referências históricas como McCall, CMM, ISO/IEC 9126, ISO/IEC 12119, ISO/IEC 14598 e ISO/IEC 12207. Essas referências devem ser tratadas como contexto de estudo; antes de alegar conformidade, certificação ou vigência normativa, consultar a edição oficial aplicável e o contexto regulatório do projeto.

As fontes públicas consultadas confirmam o estado da ISO/IEC 25010:2011 e a disponibilidade da ISO/IEC 25010:2023 [1], a distinção de níveis do CMMI [2] e as recomendações mínimas de verificação do NIST [3]. Elas não substituem a leitura integral das normas, uma auditoria formal, uma avaliação CMMI ou aconselhamento jurídico/regulatório.

## Referências

[1]: https://www.iso.org/standard/35733.html "ISO/IEC 25010:2011 — ISO"
[2]: https://cmmiinstitute.com/learning/appraisals/levels "CMMI Institute — Levels of Capability and Performance"
[3]: https://www.nist.gov/publications/guidelines-minimum-standards-developer-verification-software "NISTIR 8397 — Guidelines on Minimum Standards for Developer Verification of Software"
