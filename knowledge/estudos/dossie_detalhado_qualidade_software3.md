# Dossiê detalhado — CMMI-DEV, Representações e Áreas de Processo

## Identificação e rastreabilidade

| Campo | Informação |
|---|---|
| Arquivo | `Qualidade de Software3.pdf` |
| Unidade | Capability Maturity Model Integration — CMMI |
| Conteúdo | páginas 5–13; material complementar na página 14; referências na página 15 |
| Responsável pelo conteúdo | Prof. Ms. Douglas Almendro |
| Revisão textual | Prof. Ms. Luciano Vieira Francisco |
| Objetivo | compreender o CMMI-DEV, suas representações e áreas de processo |

## 1. Finalidade do CMMI

O CMMI integra práticas de engenharia, gestão, aquisição e serviços para orientar melhoria de processos. A unidade destaca que seus princípios podem ser adaptados a outros negócios porque tratam de planejamento, controle, risco, medição, treinamento, integração e melhoria.

O foco é reduzir imprevisibilidade, custos, defeitos e riscos, elevando a satisfação de clientes e equipes. O modelo não garante automaticamente o sucesso: ele oferece uma estrutura para tornar o processo mais observável, repetível e melhorável.

## 2. Constelações do modelo

A fonte apresenta três modelos:

| Modelo | Foco |
|---|---|
| CMMI-DEV | desenvolvimento e manutenção de produtos e serviços |
| CMMI-ACQ | aquisição, fornecedores e gestão de produtos/serviços contratados |
| CMMI-SVC | prestação e gestão de serviços |

O CMMI-DEV reúne práticas de engenharia de sistemas, engenharia de software e gestão de projetos. A separação é útil porque o risco de construir, adquirir e operar um serviço não é idêntico, embora compartilhe fundamentos.

## 3. Representação contínua

A representação contínua permite escolher processos prioritários e avaliá-los individualmente em níveis de capacidade. É adequada quando a organização quer melhorar alguns processos críticos sem assumir uma transformação uniforme de todo o portfólio.

| Nível | Nome | Interpretação |
|---|---|---|
| 0 | Incompleto | processo não executado ou parcialmente executado |
| 1 | Realizado | tarefas necessárias são executadas |
| 2 | Gerenciado | execução é planejada e comparada com o realizado |
| 3 | Definido | processo é documentado e sustentado por uma descrição padrão |
| 4 | Gerenciado quantitativamente | uso de dados e estudos estatísticos |
| 5 | Otimizado | processo é adaptado para atender necessidades e melhorar continuamente |

Uma vantagem da representação contínua é priorizar, por exemplo, gerenciamento de requisitos, segurança ou configuração antes de amadurecer todos os demais processos. O risco é produzir ilhas maduras sem integração sistêmica.

## 4. Representação por estágios

A representação por estágios estabelece uma sequência organizacional de cinco níveis de maturidade. A organização progride do inicial ao gerenciado, definido, quantitativamente gerenciado e otimizado.

| Nível | Característica |
|---|---|
| 1 — Inicial | processos caóticos, pouca previsibilidade e dependência de esforço individual |
| 2 — Gerenciado | planejamento, requisitos, custos, aquisições, prazos e marcos controlados |
| 3 — Definido | processos documentados, padronizados, integrados e adaptados aos projetos |
| 4 — Quantitativamente gerenciado | processo e produto avaliados e controlados por métricas quantitativas |
| 5 — Otimizado | melhoria contínua, inovação e prevenção baseada em causas |

A fonte observa que versões históricas apresentavam níveis e nomenclaturas que podem confundir, especialmente referências a nível 4 e nível 5/6 removidos ou reorganizados em versões posteriores. Portanto, o ano e a versão do modelo devem sempre ser registrados.

## 5. Diferença entre representações

| Critério | Contínua | Por estágios |
|---|---|---|
| Unidade de avaliação | processo individual ou grupo selecionado | conjunto organizacional de áreas |
| Flexibilidade | alta | menor, devido à sequência |
| Uso típico | melhoria direcionada por risco | roteiro e comparação de maturidade |
| Resultado | níveis de capacidade por processo | nível de maturidade organizacional |
| Risco | otimização local | burocracia ou falsa uniformidade |

Na representação por estágios, não basta que a maioria dos processos esteja em nível alto. Se a organização possui 22 processos e um permanece em nível inferior, ela não deve declarar o nível superior global conforme o critério apresentado pela fonte.

## 6. Áreas de processo por estágio

### Nível 2 — Gerenciado

| Área | Aplicação |
|---|---|
| Gerenciamento de requisitos | identificar inconsistências e manter requisitos e produtos coerentes |
| Planejamento de projeto | definir e acompanhar planos específicos |
| Acompanhamento e controle | produzir informação suficiente para controlar o projeto |
| Acordos com fornecedores | negociar, adquirir e gerir produtos/serviços externos |
| Medição e análise | estudar desvios e variações com base em medidas |
| Garantia da qualidade | verificar processo e produto contra requisitos |
| Gerência de configuração | preservar integridade de itens e versões |

### Nível 3 — Definido

Inclui desenvolvimento de requisitos, solução técnica, integração de produto, verificação, validação, foco e definição de processo organizacional, treinamento, gerenciamento integrado, gerenciamento de riscos e análise de decisão e resolução.

**Verificação** pergunta se o produto foi construído corretamente. **Validação** pergunta se o produto correto foi construído para o ambiente e necessidade definidos. Confundir as duas enfraquece a qualidade.

### Nível 4 — Quantitativamente gerenciado

As áreas citadas são desempenho de processo organizacional e gerenciamento quantitativo de projeto. A organização estabelece linhas de base e modelos, entende a variação normal, compara desempenho e intervém com dados.

### Nível 5 — Em otimização

As áreas são gestão de processo organizacional e análise causal e resolução. A organização procura causas sistêmicas de problemas e defeitos, executa ações preventivas e verifica se a mudança produziu melhoria.

## 7. Aplicação ao MX

O MX pode combinar as representações: usar a representação contínua para amadurecer primeiro memória, auditoria, segurança e CI/CD; depois usar um roteiro por estágios para institucionalizar práticas em todos os módulos.

```text
risco do módulo
  -> processo crítico
  -> capacidade atual
  -> evidências e métricas
  -> ação corretiva
  -> revisão de eficácia
  -> padronização ou otimização
```

### Áreas prioritárias

| Processo do MX | Área CMMI relacionada | Evidência |
|---|---|---|
| ingestão e memória | requisitos, configuração e validação | origem, versão, teste e auditoria |
| execução de ferramentas | gestão de riscos e verificação | sandbox, permissões, logs e testes negativos |
| releases | planejamento e configuração | changelog, artefato, aprovação e rollback |
| integrações | acordos com fornecedores e integração | contrato, compatibilidade e monitoramento |
| avaliação do assistente | medição, análise causal e validação | conjunto de testes, métricas e regressões |

## 8. Análise crítica

O CMMI é uma estrutura de melhoria, não uma prescrição de arquitetura ou tecnologia. Uma organização pode atingir maturidade documental e ainda entregar um produto inadequado se não compreender o usuário, o domínio ou os riscos técnicos.

A certificação ou avaliação deve ser contextualizada. O resultado de um modelo de maturidade não substitui segurança independente, testes de desempenho, auditoria de dados, acessibilidade, confiabilidade operacional ou validação com usuários.

Também é necessário evitar a aplicação mecânica de processos de grandes fornecedores governamentais a equipes pequenas. A prática deve ser proporcional ao risco e automatizada sempre que possível.

## 9. Exercícios autorais e gabaritos

### Exercício 1 — escolha da representação

Uma equipe quer melhorar apenas gestão de configuração e testes de regressão porque sofre com releases quebrados. Qual representação é inicialmente mais adequada?

**Gabarito:** a contínua, pois permite priorizar processos específicos conforme risco e necessidade, sem esperar que toda a organização avance uniformemente.

### Exercício 2 — verificação e validação

Um módulo implementa corretamente uma regra, mas a regra não atende à necessidade do usuário. Isso é falha de verificação ou validação?

**Gabarito:** falha de validação. A implementação pode estar conforme a especificação, mas o produto não é adequado ao uso ou necessidade real.

### Exercício 3 — maturidade global

Vinte e um processos estão no nível 4 e um está no nível 3. A organização pode declarar nível 4 por estágios?

**Gabarito:** não segundo o critério apresentado; o nível global exige que o conjunto de processos alcance o nível correspondente.

### Exercício 4 — ação causal

A equipe corrige repetidamente o mesmo tipo de defeito em releases. Qual prática de nível otimizado é indicada?

**Gabarito:** análise causal e resolução, procurando a causa sistêmica — requisito ambíguo, ausência de teste, etapa de revisão ou defeito de processo — e verificando a eficácia da prevenção.

## 10. Referências

[1]: `Qualidade de Software3.pdf`, páginas 5–15, material salvo no projeto Estudos.

[2]: SEI. *CMMI para Desenvolvimento (CMMI-DEV), versão 1.2*. Carnegie Mellon Software Engineering Institute, 2006.

[3]: FALBO, Ricardo de Almeida. *Qualidade de processo de software CMMI*. DI/UFES, 2008.

[4]: SOMMERVILLE, Ian. *Engenharia de software*. 8. ed. Pearson Addison-Wesley, 2007.

[5]: GAMMA, Erich et al. *Padrões de projeto*. Porto Alegre: Bookman, 2000.
