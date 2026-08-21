# Dossiê detalhado — CMM, Maturidade, Métricas e Garantia da Qualidade

## Identificação e rastreabilidade

| Campo | Informação |
|---|---|
| Arquivo | `Qualidade de Software2.pdf` |
| Unidade | CMM — Capability Maturity Model |
| Conteúdo | páginas 5–17; material complementar na página 18; referências nas páginas 19–20 |
| Responsável pelo conteúdo | Prof. Ms. Douglas Almendro |
| Revisão textual | Profa. Ms. Selma Aparecida Cesarin |
| Objetivo | compreender maturidade de processos, áreas-chave, métricas e sistemas de gerenciamento da qualidade |

## 1. Processo e maturidade

Processo de software é um conjunto parcialmente ordenado de atividades, artefatos, pessoas, recursos, estruturas e restrições destinado a produzir e manter um produto. O CMM organiza uma evolução da execução ad hoc para uma prática disciplinada, definida, medida e continuamente melhorada.

A premissa central apresentada é que a qualidade do produto é profundamente influenciada pela qualidade do processo utilizado para construí-lo e mantê-lo. Isso não significa que processo maduro garanta produto perfeito; significa que processo controlado aumenta previsibilidade, capacidade de aprender e chance de detectar desvios antes que se tornem falhas.

## 2. Origem e propósito do CMM

O CMM surgiu na década de 1980 no contexto de avaliação de risco de fornecedores de software para o Departamento de Defesa dos Estados Unidos. O SEI, ligado à Carnegie Mellon University, desenvolveu um modelo para orientar melhoria de processos e tornar qualidade, custo e prazo mais previsíveis.

O modelo descreve cinco níveis ordinais. Cada nível, exceto o inicial, possui áreas-chave de processo, objetivos e práticas que estabilizam componentes importantes da organização.

## 3. Os cinco níveis

| Nível | Nome | Característica dominante | Risco principal |
|---|---|---|---|
| 1 | Inicial | processo ad hoc, dependente de heroísmo | imprevisibilidade de custo, prazo e qualidade |
| 2 | Repetível | práticas básicas de gestão de projeto e repetição de sucessos | variação entre projetos e estimativas frágeis |
| 3 | Definido | processos organizacionais padrão adaptados aos projetos | burocratização ou aplicação sem contexto |
| 4 | Gerenciado | controle quantitativo e metas mensuráveis | medir sem agir ou otimizar indicador errado |
| 5 | Otimizado | prevenção de defeitos, inovação e melhoria contínua | melhoria cosmética sem resultado de negócio |

### Nível 1 — Inicial

Os processos são instáveis, reativos e frequentemente dependem da competência individual. Resultados podem ser obtidos por esforço extraordinário, mas não são confiavelmente repetíveis. Projetos podem exceder orçamento e prazo.

### Nível 2 — Repetível

A organização estabelece práticas de gestão de requisitos, planejamento, acompanhamento, gestão de subcontratados, garantia da qualidade e configuração. O status do projeto torna-se visível e sucessos anteriores podem ser repetidos em escopos semelhantes.

### Nível 3 — Definido

A organização mantém processos padrão e permite que cada projeto os adapte de forma controlada. A diferença essencial para o nível 2 é que os processos deixam de ser apenas práticas locais do projeto e passam a ser derivados de uma base organizacional comum. Incluem foco organizacional, definição de processo, treinamento, gestão integrada, engenharia do produto, coordenação entre grupos e revisões conjuntas.

### Nível 4 — Gerenciado

O desempenho do processo é medido quantitativamente. A organização define objetivos mensuráveis de qualidade, acompanha progresso e adapta o processo sem perder controle estatístico ou aderência às especificações.

### Nível 5 — Otimizado

A organização previne defeitos, avalia mudanças tecnológicas e melhora continuamente seus processos. A melhoria deixa de ser reação a falhas isoladas e passa a investigar causas comuns e oportunidades sistêmicas.

## 4. Áreas-chave de processo

### Nível 2

| Área | Intenção |
|---|---|
| Gerenciamento de requisitos | manter planos, atividades e produtos coerentes com requisitos controlados |
| Planejamento de projeto | documentar estimativas, atividades, custos, prazos e compromissos |
| Acompanhamento | comparar resultados reais com plano e aplicar ações corretivas |
| Subcontratados | selecionar, acompanhar e alinhar fornecedores |
| Garantia da qualidade | verificar objetivamente aderência a padrões e requisitos |
| Gerenciamento de configuração | controlar versões, mudanças e baselines |

### Nível 3

As áreas incluem foco organizacional no processo, definição de processo padrão, programa de treinamento, gerenciamento integrado, engenharia de produto, coordenação intergrupos e revisão conjunta. O objetivo é que conhecimento não fique preso a indivíduos e que projetos possam operar com uma linguagem processual comum.

### Nível 4

O nível gerenciado acrescenta gerenciamento quantitativo do processo e gerenciamento da qualidade de software. As atividades devem ser planejadas, o desempenho deve ser controlado por dados e os objetivos mensuráveis de qualidade devem ser priorizados e acompanhados.

### Nível 5

O nível otimizado concentra-se em prevenção de defeitos, gerenciamento de mudanças tecnológicas e gerenciamento de mudanças no processo. A organização procura causas recorrentes, avalia o impacto de novas tecnologias e envolve os projetos na melhoria do processo padrão.

## 5. Interpretação correta do CMM

O material alerta que o CMM é descritivo e normativo em relação a atributos esperados, mas não prescreve uma receita única de implementação. A organização deve interpretar as práticas conforme contexto comercial, tamanho, domínio, riscos, cultura e tecnologia.

O CMM não é uma bala de prata. Não escolhe especialistas, tecnologias, estratégias de contratação, motivação ou retenção. Ele estrutura processos de gestão e engenharia; não substitui liderança, arquitetura, domínio, produto, segurança, competências ou decisão econômica.

## 6. Métricas de qualidade

Software não possui leis quantitativas simples equivalentes às de algumas engenharias físicas. Métricas são frequentemente indiretas e só fazem sentido dentro de regras, contexto e interpretação definidos.

| Área | Métricas úteis |
|---|---|
| Previsibilidade | variação de prazo, custo e escopo |
| Fluxo | lead time, throughput, WIP, tempo de espera |
| Qualidade | defeitos por etapa, escape rate, severidade e retrabalho |
| Processo | aderência, desvios, tempo de correção e ações preventivas |
| Operação | incidentes, disponibilidade, MTTR, SLO e mudança malsucedida |
| Produto | acurácia, latência, satisfação, uso e falhas de domínio |
| Melhoria | redução de causas recorrentes, benefício e custo da intervenção |

Métricas devem ser usadas para aprender e decidir, não para punir equipes. Quando um indicador vira meta isolada, pode gerar gaming: aumentar cobertura sem melhorar testes, fechar defeitos sem corrigir causa ou reduzir tempo registrado omitindo trabalho real.

## 7. Software Quality Assurance — SQA

SQA monitora métodos, padrões, processos e evidências para verificar se o desenvolvimento segue o que foi definido e se os desvios são identificados, documentados e tratados. O grupo de SQA não precisa ser o mesmo grupo que implementa o produto; sua independência relativa favorece revisão objetiva.

### Atividades recomendadas

1. preparar o plano de SQA durante o planejamento;
2. revisar a descrição do processo do projeto;
3. verificar aderência de atividades e artefatos;
4. registrar e acompanhar desvios;
5. assegurar que não conformidades sejam documentadas;
6. reportar discordâncias ao gerenciamento;
7. apoiar controle de mudanças;
8. coletar e analisar métricas;
9. manter registros de revisões e auditorias.

SQA complementa, mas não substitui, a responsabilidade dos engenheiros, que devem usar métodos sólidos, revisões técnicas formais, testes e medidas.

## 8. PSP — Personal Software Process

O PSP adapta princípios de processo para engenheiros individuais e equipes pequenas. Seu foco é desenvolver consciência sobre como o trabalho é executado, medir desempenho e ajustar métodos para alcançar objetivos pessoais de qualidade e produtividade.

Práticas do PSP incluem plano pessoal, registro de tempo, registro de defeitos, relatório resumido, uso histórico para planejar trabalhos futuros e análise de desempenho. A ideia é transformar experiência individual em dados úteis, sem esperar que uma grande estrutura organizacional resolva todos os problemas.

Para o MX, o PSP pode inspirar um diário técnico por módulo: estimativa, tempo, defeitos encontrados, causa, retrabalho, testes e lições. O objetivo deve ser melhoria, não vigilância improdutiva.

## 9. Qualidade, custo e prazo

A fonte apresenta uma visão realista: qualidade precisa ser balanceada com prazo, escopo, custo e necessidade do cliente. Um produto tecnicamente excelente entregue depois da janela de valor pode falhar como solução.

Isso não justifica aceitar risco desconhecido. Significa explicitar trade-offs: quais características são críticas, quais podem ser reduzidas, quais riscos serão aceitos, quais controles são obrigatórios e como a decisão será comunicada.

## 10. Aplicação ao MX

O MX pode adotar uma trilha progressiva, sem transformar CMM em burocracia:

| Estágio | Prática no MX |
|---|---|
| Inicial | registrar tarefas, decisões, versões e falhas básicas |
| Repetível | templates de requisitos, testes, releases e incidentes |
| Definido | processo modular padrão, adaptadores e treinamento |
| Gerenciado | métricas de qualidade, SLOs, riscos e análise de tendência |
| Otimizado | prevenção de defeitos, experimentos, automação e melhoria contínua |

### Evidências mínimas

Cada alteração importante deve possuir requisito, risco, responsável, revisão, teste, versão, evidência de execução, impacto e plano de rollback. O assistente deve conseguir responder o que mudou, por que mudou, qual teste foi executado e qual comportamento foi observado.

## 11. Exercícios autorais e gabaritos

### Exercício 1 — maturidade

Uma equipe entrega projetos por esforço heroico, sem planejamento confiável, mas ocasionalmente obtém bons resultados. Em que nível se encontra?

**Gabarito:** características do nível 1, inicial. O sucesso depende de indivíduos e não de processo institucionalizado.

### Exercício 2 — nível 2 versus nível 3

Qual é a diferença essencial entre uma equipe que repete práticas em seus próprios projetos e uma organização com processo definido?

**Gabarito:** no nível 2, práticas podem ser locais e variar entre projetos; no nível 3, existe um processo organizacional padrão, documentado, treinado e adaptado de modo controlado.

### Exercício 3 — métrica contraproducente

A direção exige reduzir defeitos reportados em 30%, sem observar severidade ou uso. Qual risco surge?

**Gabarito:** subnotificação e mascaramento de defeitos. A métrica deve combinar defeitos encontrados, severidade, escape rate, incidentes e causa raiz.

### Exercício 4 — SQA

SQA deve corrigir diretamente o código de todos os defeitos?

**Gabarito:** não. SQA monitora processo, padrões, evidências e desvios; a engenharia corrige o produto. SQA pode apoiar análise e verificar a eficácia da correção.

### Exercício 5 — CMM no MX

Como aplicar maturidade sem criar burocracia?

**Gabarito:** iniciar com evidências mínimas ligadas a riscos, automatizar verificações, medir resultados úteis e revisar o processo conforme impacto. Não implantar documentos sem finalidade operacional.

## 12. Análise crítica e atualização

O CMM clássico é uma referência histórica importante, mas organizações modernas podem combinar seus princípios com Agile, DevOps, SRE, gestão de produto, segurança desde o desenho, CI/CD, métricas DORA e melhoria baseada em evidências. A maturidade não deve ser confundida com quantidade de documentos ou com certificação.

A contribuição duradoura do modelo é estruturar a pergunta: o resultado depende de heroísmo ou de um processo compreendido, repetível, mensurado e melhorado? Para o MX, a resposta deve ser observável em código, dados, decisões, testes e operação.

## Referências

[1]: `Qualidade de Software2.pdf`, páginas 5–20, material salvo no projeto Estudos.

[2]: SEI. *An Overview of Capability Maturity Model Integration — Version 1.0*. Tutorial apresentado no SIMPROS, 2000.

[3]: GAMMA, Erich et al. *Padrões de projeto*. Porto Alegre: Bookman, 2000.

[4]: SOMMERVILLE, Ian. *Engenharia de software*. 8. ed. Pearson Addison-Wesley, 2007.

[5]: MCCALL, J.; RICHARDS, P.; WALTERS, G. *Factors in Software Quality*. NTIS, 1977.

[6]: PRESSMAN, Roger S. *Engenharia de software*. 6. ed. Porto Alegre: Bookman, 2006.

[7]: RAKITIN, Steven R. *Software Verification and Validation: a Practitioner’s Guide*. Artech House, 1997.
