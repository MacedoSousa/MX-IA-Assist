# Dossiê detalhado — ISO, Ciclo de Vida e Avaliação de Processos

## Identificação e rastreabilidade

| Campo | Informação |
|---|---|
| Arquivo | `Qualidade de Software4.pdf` |
| Unidade | ISO — International Organization for Standardization |
| Conteúdo | páginas 5–14; material complementar na página 15; referências nas páginas 16–17 |
| Responsável pelo conteúdo | Prof. Ms. Douglas Almendro |
| Revisão textual | Prof. Ms. Claudio Brites |
| Objetivo | compreender organismos normativos, série ISO 9000, ciclo de vida e avaliação de processos de software |

## 1. Organismos normativos

Organismos normativos produzem regras e referências técnicas baseadas no trabalho de especialistas. Essas regras apoiam especificação de produtos, organização de serviços e elaboração de legislação. A fonte apresenta níveis internacional, nacional e regional, com ISO, ABNT, ANSI, IPQ, IANORQ, AMN e COPANT.

A hierarquia é importante para identificar quem publica a norma, quem adapta o texto ao país ou região e quem pode realizar avaliação ou certificação. Uma organização que busca certificação deve distinguir norma, organismo de acreditação, organismo certificador, auditoria e evidência de conformidade.

## 2. ISO e IEC

A ISO é apresentada como organização internacional de padronização, fundada em Genebra em 1947. A sigla foi escolhida a partir de `isos`, termo grego associado à igualdade, para permanecer estável em diferentes idiomas.

A IEC é a Comissão Eletrotécnica Internacional, voltada a tecnologias elétricas e relacionadas. Em tecnologia da informação, normas podem ser publicadas em conjunto como ISO/IEC, indicando colaboração entre as organizações.

## 3. Série ISO 9000

A série ISO 9000 é tratada como família de normas para sistemas de gestão da qualidade. Seu foco é estruturar processos, documentação, auditorias, manutenção de equipamentos, ações preventivas e corretivas, além da satisfação do cliente.

A certificação não significa que cada linha de código esteja livre de defeitos. Ela indica que um sistema de gestão foi avaliado segundo critérios definidos e que a organização possui processos e evidências dentro do escopo auditado.

## 4. ISO 9001 e ciclo PDCA

A ISO 9001 é apresentada como estrutura de sistema de gestão orientada à melhoria e satisfação do cliente. A abordagem PDCA organiza o ciclo:

| Etapa | Pergunta operacional |
|---|---|
| Planejar | o que precisa ser alcançado e como medir? |
| Fazer | quais atividades serão executadas? |
| Checar | o resultado atende ao plano e aos critérios? |
| Agir | o que deve ser corrigido, padronizado ou melhorado? |

Benefícios esperados incluem melhor gerenciamento de riscos, redução de desperdício, documentação, comunicação entre setores e tratamento preventivo de problemas.

## 5. ISO 9000-3 e software

A ISO 9000-3 é apresentada como aplicação de diretrizes de gestão da qualidade ao desenvolvimento, fornecimento e manutenção de software. A unidade observa que ela trata de procedimentos e documentação, mas não oferece, por si só, um roteiro completo de melhoria contínua equivalente ao CMMI ou à ISO/IEC 15504.

A fonte também registra versões históricas e a defasagem de uma adoção nacional baseada em edição antiga. Esse ponto é essencial: qualquer projeto atual deve confirmar a edição vigente e a norma substituta aplicável antes de usar a referência em contrato ou auditoria.

## 6. ISO/IEC 12207

A ISO/IEC 12207 estabelece linguagem comum entre desenvolvedor, cliente e demais stakeholders por meio de processos do ciclo de vida do software. Dois princípios destacados são:

**Modularidade:** processos são conectados, mas suas interfaces devem ser claras e limitadas. **Responsabilidade:** cada processo possui responsável definido, evitando que atividades críticas fiquem sem dono.

### Classes de processo

| Classe | Processos e finalidade |
|---|---|
| Fundamentais | aquisição, fornecimento, desenvolvimento, operação e manutenção |
| Apoio | documentação, configuração, garantia da qualidade, verificação, validação, revisão conjunta, auditoria e resolução de problemas |
| Organizacionais | gerência, infraestrutura, melhoria e recursos humanos |

### Processos fundamentais

**Aquisição** obtém produtos ou serviços externos. **Fornecimento** entrega o produto de software. **Desenvolvimento** transforma requisitos em sistema funcional. **Operação** coloca o sistema no ambiente de uso e acompanha problemas. **Manutenção** realiza correções e modificações após a entrada em uso.

### Processos de apoio

Documentação registra informações técnicas e operacionais. Gerência de configuração preserva a integridade dos produtos de trabalho. Garantia da qualidade verifica conformidade. Verificação analisa se o produto reflete requisitos especificados; validação confirma se os requisitos são atendidos no uso. Revisões conjuntas preservam entendimento entre grupos, auditorias confrontam evidências com requisitos e resolução de problemas trata não conformidades.

### Processos organizacionais

Gerência organiza, controla e monitora processos. Infraestrutura sustenta ambiente estável. Melhoria avalia e aperfeiçoa o ciclo de vida. Recursos humanos alinha competências e capacidade às necessidades do negócio.

A norma deve ser adaptada ao contexto e aos objetivos do projeto. Adaptação não significa eliminar controles críticos; significa selecionar, detalhar e combinar processos de modo proporcional ao risco.

## 7. ISO/IEC 15504 — SPICE

A ISO/IEC 15504 é apresentada como framework para melhoria de processo e determinação de capacidade. Ela combina referências de modelos como CMM e normas da série ISO 9000, oferecendo níveis para autoavaliação e avaliação externa.

| Nível | Nome | Característica |
|---|---|---|
| 0 | Incompleto | processo ausente ou parcialmente executado |
| 1 | Executado | tarefas necessárias são realizadas |
| 2 | Gerenciado | execução planejada e comparada com resultado |
| 3 | Estabelecido | processo descrito e construído sobre prática existente |
| 4 | Previsível | gerenciamento quantitativo e análise estatística |
| 5 | Otimizado | adaptação e melhoria contínua do processo quantitativo |

## 8. Dimensões e categorias do SPICE

O modelo de referência possui dimensão de processo e dimensão de capacidade. A dimensão de processo usa práticas essenciais de engenharia, como as relacionadas ao ciclo de vida. A dimensão de capacidade avalia o grau de execução, gestão, definição, previsibilidade e otimização.

As categorias de processo citadas são:

| Categoria | Escopo |
|---|---|
| Cliente-fornecedor | relação entre cliente, fornecedor e entrega |
| Engenharia | tecnologia, práticas e construção do produto |
| Suporte | documentação, manutenção e apoio aos projetos |
| Gestão | gestão de projetos, processos e setores |
| Organização | necessidades organizacionais e melhoria |

A aplicação do SPICE deve identificar pontos fortes e fracos, gerar plano de melhoria e repetir a avaliação. Um número de capacidade sem plano de ação tem pouco valor operacional.

## 9. ISO no MX

O MX pode usar a ISO/IEC 12207 para organizar o ciclo de vida de seus módulos e a lógica de melhoria do SPICE para avaliar capacidades específicas. A ISO 9001 pode inspirar o sistema de gestão, enquanto CMMI e SPICE ajudam a estruturar evolução e evidências.

```text
necessidade do usuário
  -> aquisição/definição de requisito
  -> desenvolvimento e verificação
  -> validação no ambiente de uso
  -> operação observada
  -> manutenção controlada
  -> auditoria e melhoria
```

### Registro mínimo por mudança

| Campo | Evidência |
|---|---|
| Requisito | origem, versão e critério de aceitação |
| Responsável | pessoa ou agente accountable |
| Configuração | commit, artefato e dependências |
| Verificação | testes técnicos e análise estática |
| Validação | teste de uso, domínio ou aceitação |
| Operação | métricas, logs e incidentes |
| Melhoria | causa, ação, resultado e decisão |

## 10. Certificação e auditoria

Certificação deve ser compreendida como avaliação formal de um sistema ou escopo, não como garantia absoluta de qualidade de todos os produtos. Auditoria competente verifica evidências, entrevistas, registros, amostras, controles e aderência aos critérios.

Uma organização pode estar conforme em determinado escopo e ainda apresentar riscos não cobertos pela auditoria. O relatório deve registrar limites, período, amostragem, não conformidades, observações e ações corretivas.

## 11. Análise crítica e atualização

O material é didático e utiliza versões históricas, especialmente ISO 9001:2008, ISO 9000-3 e ISO/IEC 15504. A nomenclatura e a estrutura de normas evoluíram; antes de aplicar recomendações em produção, contrato ou certificação, deve-se verificar a edição vigente, o organismo reconhecido e os requisitos do setor.

O ponto permanente é a necessidade de processos claros, responsabilidade, rastreabilidade, verificação, validação, auditoria, tratamento de problemas e melhoria. A norma não substitui julgamento de engenharia nem conhecimento do domínio.

## 12. Exercícios autorais e gabaritos

### Exercício 1 — escopo de certificação

Uma empresa possui certificação de gestão para sua fábrica de software. Isso prova que todo produto produzido é correto?

**Gabarito:** não. A certificação abrange o sistema e escopo auditados. Cada produto ainda precisa de requisitos, verificação, validação, testes, segurança e acompanhamento operacional.

### Exercício 2 — 12207

Onde registrar uma alteração de modelo, seus testes e o artefato liberado?

**Gabarito:** documentação e gerência de configuração, apoiadas por verificação, validação e controle de mudança. A operação deve manter evidência do comportamento após a liberação.

### Exercício 3 — SPICE

Um processo é executado, mas não é planejado nem comparado com o realizado. Qual capacidade ainda falta?

**Gabarito:** capacidade de processo gerenciado. Executar tarefas é insuficiente; é necessário planejar, acompanhar, registrar e agir sobre desvios.

### Exercício 4 — verificação e validação

O teste confirma que o código segue o requisito, mas usuários não conseguem completar a tarefa. Qual conclusão?

**Gabarito:** verificação técnica ocorreu, mas a validação de uso falhou ou foi insuficiente. Deve-se revisar necessidade, critérios e ambiente real de operação.

## Referências

[1]: `Qualidade de Software4.pdf`, páginas 5–17, material salvo no projeto Estudos.

[2]: ISO/IEC 12207. *Processos do ciclo de vida do software*. Edição conforme referência histórica da fonte.

[3]: ISO/IEC 15504. *Software Process Improvement and Capability Determination — SPICE*. Referência histórica conforme a fonte.

[4]: PRESSMAN, Roger S. *Engenharia de software*. 6. ed. Porto Alegre: Bookman, 2006.

[5]: SOMMERVILLE, Ian. *Engenharia de software*. 8. ed. Pearson Addison-Wesley, 2007.

[6]: McCALL, J.; RICHARDS, P.; WALTERS, G. *Factors in Software Quality*. NTIS, 1977.
