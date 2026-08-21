# Dossiê detalhado — Motivação da Qualidade de Software

## Identificação e rastreabilidade

| Campo | Informação |
|---|---|
| Arquivo | `Qualidade de Software1.pdf` |
| Unidade | Motivação de Qualidade |
| Conteúdo principal | páginas 5–14; material complementar na página 15; referências nas páginas 16–17 |
| Responsável pelo conteúdo | Prof. Ms. Douglas Almendro |
| Revisão textual | Profa. Esp. Márcia Ota |
| Objetivo | introduzir qualidade, relação com o tipo de software, avaliação do produto e certificações/processos |

## 1. Conceito de qualidade

Qualidade de software não é apenas ausência de defeitos. É o grau em que o produto atende requisitos funcionais e de desempenho, segue padrões documentados e possui características implícitas esperadas de um software profissional.

A fonte diferencia **qualidade de projeto**, relacionada ao que foi especificado, de **qualidade de conformidade**, relacionada ao quanto a implementação segue o projeto. Um produto pode ter requisitos mal definidos e, ainda assim, conformidade de implementação; por isso, qualidade exige avaliar tanto o produto quanto o processo e a adequação dos requisitos.

## 2. Risco social e econômico

A dependência de sistemas computacionais torna falhas de software potencialmente graves. O material utiliza exemplos históricos de navegação, sincronização, radiação e datas para mostrar que um defeito pode produzir danos operacionais, financeiros e humanos.

A consequência prática é que qualidade deve ser construída desde a concepção, não apenas verificada no final. Requisitos críticos, riscos, modos de falha, testes, rastreabilidade e controle de mudanças precisam existir antes de o sistema atingir produção.

## 3. Modelo de McCall

O modelo de McCall organiza características em três perspectivas: operação do produto, capacidade de alteração e capacidade de adaptação.

| Perspectiva | Características |
|---|---|
| Uso/operação | correção, confiabilidade, eficiência, integridade e usabilidade |
| Alteração | manutenibilidade, flexibilidade e testabilidade |
| Transição/adaptação | portabilidade, reusabilidade e interoperabilidade |

### Características operacionais

**Correção** mede quanto o programa satisfaz a especificação e os objetivos do cliente. **Confiabilidade** considera a execução da função pretendida com precisão durante um período. **Eficiência** relaciona resultado a uso de recursos computacionais e código. **Integridade** trata do controle de acesso ao software e aos dados. **Usabilidade** mede o esforço de aprender, preparar entradas e interpretar saídas.

### Características de alteração

**Manutenibilidade** é o esforço para localizar e eliminar erros. **Flexibilidade** é o esforço para modificar o programa. **Testabilidade** é o esforço para verificar que a alteração continua funcionando.

### Características de transição

**Portabilidade** é o esforço de transferir o software entre plataformas. **Reusabilidade** é a possibilidade de usar componentes em outros programas. **Interoperabilidade** é o esforço necessário para integrar o software com outros sistemas.

## 4. Subcaracterísticas detalhadas

A unidade relaciona funcionalidade a adequação, acurácia, interoperabilidade, conformidade e segurança de acesso. Confiabilidade inclui maturidade, tolerância a falhas e recuperabilidade. Usabilidade envolve inteligibilidade, apreensibilidade e operacionalidade.

Eficiência é observada por comportamento temporal e uso de recursos. Manutenibilidade inclui analisabilidade, modificabilidade, estabilidade e testabilidade. Portabilidade abrange adaptabilidade, capacidade de instalar, capacidade de substituir e conformidade.

Essas categorias devem ser transformadas em critérios mensuráveis. Por exemplo, “eficiente” pode significar p95 de resposta abaixo de um limite sob carga definida; “recuperável” pode significar RTO e RPO testados; “seguro” pode significar autorização negada por padrão e ausência de vulnerabilidades críticas conhecidas.

## 5. Qualidade depende do tipo de software

Não há uma distribuição universal de pesos. Um sistema médico prioriza segurança, confiabilidade e recuperabilidade; uma interface de consumo pode priorizar usabilidade e desempenho percebido; um compilador pode priorizar correção, portabilidade e desempenho; um pipeline de Big Data precisa considerar throughput, tolerância a falhas, observabilidade e custo.

Para o MX, os pesos variam por componente. Memória e conhecimento exigem integridade, rastreabilidade e recuperabilidade; execução de código exige segurança e isolamento; interface exige usabilidade; ingestão exige eficiência e observabilidade; integração distribuída exige interoperabilidade e tolerância a falhas.

## 6. Objetivos da qualidade

A unidade apresenta três objetivos principais:

1. aprimorar o processo para melhorar o produto;
2. avaliar o produto e sua conformidade com norma ou padrão;
3. apoiar a aquisição, selecionando o produto mais adequado entre alternativas.

A qualidade não é uma etapa isolada do desenvolvimento. É um sistema de decisões que liga requisitos, arquitetura, implementação, testes, operação, manutenção e evolução.

## 7. Processos e referências normativas

A fonte cita CMM/SEI como modelo de orientação para melhoria de processo; ISO/SPICE para avaliação e melhoria de processos; ISO/IEC 12207 para ciclo de vida; ISO/IEC 9000-3 para diretrizes de aplicação da ISO 9001 ao desenvolvimento e manutenção de software; ISO/IEC 9126 para características de qualidade; ISO/IEC 12119 para pacotes de software e instruções de teste; e ISO/IEC 14598-5 para avaliação de produto.

Essas referências devem ser lidas historicamente e comparadas às revisões e normas atuais antes de uma auditoria. O dossiê preserva o conteúdo da fonte, mas não assume que todas as edições citadas estejam vigentes.

## 8. Avaliação do produto

A avaliação pode ocorrer por organismo de certificação, por equipe interna multidisciplinar ou por empresa externa. Uma avaliação interna eficaz combina especialistas técnicos e especialistas do domínio do produto, pois qualidade técnica sem adequação ao negócio pode produzir um sistema conforme e inútil.

Certificação oficial e avaliação independente não são equivalentes. Uma empresa pode realizar avaliação técnica sem possuir competência para emitir certificado reconhecido por determinado organismo. O relatório deve declarar escopo, critérios, evidências, limitações e responsável pela conclusão.

## 9. Erro, falha, defeito e failure

A terminologia da fonte, alinhada ao IEEE 610.12, diferencia causas e manifestações.

| Termo | Sentido |
|---|---|
| Engano/erro humano (`mistake`) | ação humana que produz resultado incorreto |
| Falha/defeito de implementação (`fault`, bug) | manifestação no software de um engano |
| Erro em execução (`error`) | estado interno ou resultado diferente do esperado |
| Defeito de serviço (`failure`) | incapacidade observável de fornecer o serviço conforme especificado |

A cadeia típica é `mistake -> fault -> error -> failure`, mas um defeito pode permanecer latente e nunca produzir falha sob determinadas entradas. Testes devem buscar ativar condições relevantes e observabilidade deve detectar manifestações em produção.

## 10. Causas de degradação

A unidade destaca três fatores: alterações, tempo e complexidade. Mudanças podem degradar a estrutura e aumentar o custo de manutenção. Com o tempo, o custo de implementar mudanças tende a aumentar e a capacidade de prestar o serviço pode diminuir. Complexidade dificulta desenvolvimento, entendimento, uso e documentação.

A conclusão arquitetural é que refatoração, documentação viva, testes automatizados, modularidade, revisão de código, gestão de dependências, observabilidade e controle de configuração são mecanismos de preservação de qualidade, não adornos.

## 11. Garantia da Qualidade de Software

Garantia da qualidade é um arcabouço sistemático e planejado de ações para assegurar qualidade. Deve responder se o software possui as características desejadas, se o desenvolvimento seguiu padrões e se as disciplinas técnicas cumpriram seus papéis.

Atividades citadas incluem aplicação de métodos e ferramentas, revisões técnicas, testes, padrões de documentos e código, controle de alterações, medição e manutenção de registros de auditorias e revisões.

### Ciclo recomendado

```text
requisitos mensuráveis
  -> arquitetura e riscos
  -> padrões e critérios de aceitação
  -> implementação e revisão
  -> testes e métricas
  -> auditoria e registro
  -> operação monitorada
  -> feedback e melhoria
```

## 12. Métricas para o MX

| Dimensão | Indicadores possíveis |
|---|---|
| Correção | taxa de casos aprovados, violações de invariantes, defeitos escapados |
| Confiabilidade | MTBF, taxa de falha, sucesso de recuperação |
| Eficiência | latência p50/p95/p99, throughput, CPU, memória e custo |
| Integridade | falhas de autorização, cobertura de auditoria, incidentes |
| Usabilidade | tempo para concluir tarefa, erros do usuário, acessibilidade |
| Manutenibilidade | tempo para corrigir, complexidade, acoplamento, change failure rate |
| Testabilidade | cobertura significativa, tempo de execução, mutantes detectados |
| Portabilidade | plataformas suportadas e esforço de adaptação |
| Interoperabilidade | contratos compatíveis, taxa de integração e erros de schema |

Métrica sem contexto pode induzir comportamento incorreto. Cobertura de testes, por exemplo, não prova que os testes são bons; deve ser relacionada a riscos e defeitos detectados.

## 13. Aplicação ao MX

O MX deve possuir uma matriz que associe cada requisito a risco, característica de qualidade, métrica, teste, evidência e responsável. Antes de integrar um novo módulo, deve verificar contrato de entrada/saída, versão, permissões, observabilidade, rollback e compatibilidade.

### Matriz mínima

| Requisito | Característica | Evidência | Critério |
|---|---|---|---|
| resposta rápida | eficiência | teste de carga e traces | p95 dentro do limite |
| não expor memória privada | integridade | testes negativos e auditoria | zero acesso indevido |
| recuperar após falha | confiabilidade | teste de restauração | RTO/RPO atendidos |
| trocar modelo | portabilidade/interoperabilidade | teste de adapter | contrato preservado |
| alterar regra | manutenibilidade | revisão e teste de regressão | mudança localizada |
| explicar decisão | funcionalidade/usabilidade | registro de evidência | rastreabilidade completa |

## 14. Exercícios autorais e gabaritos

### Exercício 1 — qualidade por domínio

Escolha três características prioritárias para memória de conhecimento do MX e justifique.

**Gabarito:** integridade, recuperabilidade e manutenibilidade. Integridade evita corrupção ou acesso indevido; recuperabilidade preserva o conhecimento; manutenibilidade permite corrigir e atualizar módulos sem degradar o sistema.

### Exercício 2 — cadeia de defeito

Uma regra de validação foi implementada com operador incorreto, mas nenhum teste usa o caso limítrofe. Classifique os estágios.

**Gabarito:** o engano foi humano; o código incorreto é um fault; quando o caso limítrofe executar, o estado ou resultado incorreto será um error; se o serviço entregar resultado incompatível ao cliente, ocorrerá failure.

### Exercício 3 — certificação

Uma avaliação interna concluiu que o produto é adequado. Isso equivale a uma certificação oficial?

**Gabarito:** não necessariamente. Avaliação interna fornece evidência para decisão e melhoria; certificação depende de escopo, norma, organismo competente e regras formais aplicáveis.

### Exercício 4 — métrica inadequada

A equipe quer declarar qualidade porque atingiu 95% de cobertura de linhas. A conclusão é válida?

**Gabarito:** não. Cobertura de linhas não mede adequação, casos críticos, mutações, confiabilidade, segurança ou comportamento sob carga. Deve ser combinada com critérios de risco, cobertura de requisitos, testes negativos e defeitos escapados.

### Exercício 5 — alteração arquitetural

Uma nova integração reduz latência, mas aumenta acoplamento e dificulta testes. Como avaliar?

**Gabarito:** analisar o conjunto de características: eficiência melhorou, mas manutenibilidade, testabilidade e possivelmente interoperabilidade pioraram. A decisão deve usar pesos do domínio, métricas antes/depois e custo de ciclo de vida.

## 15. Análise crítica e atualização

A unidade é adequada como introdução, mas alguns nomes de normas e modelos pertencem a edições históricas. Para aplicação contemporânea, devem ser confrontados com normas vigentes, práticas de engenharia de confiabilidade, segurança, acessibilidade, DevOps e gestão de riscos.

A principal conclusão permanece atual: qualidade é uma propriedade construída por processo, arquitetura, código, testes, operação e melhoria contínua. Não deve ser reduzida a certificação ou inspeção final.

## Referências

[1]: `Qualidade de Software1.pdf`, páginas 5–17, material salvo no projeto Estudos.

[2]: McCALL, J.; RICHARDS, P.; WALTERS, G. *Factors in Software Quality*. NTIS, 1977.

[3]: PRESSMAN, Roger S. *Engenharia de software*. 6. ed. Porto Alegre: Bookman, 2006.

[4]: RAKITIN, Steven R. *Software Verification and Validation: a Practitioner’s Guide*. Artech House, 1997.

[5]: GAMMA, Erich et al. *Padrões de projeto*. Porto Alegre: Bookman, 2000.

[6]: SOMMERVILLE, Ian. *Engenharia de software*. 8. ed. Pearson Addison-Wesley, 2007.

[7]: RESENDE, Denis Alcides. *Engenharia de software e sistemas de informação*. 3. ed. Brasport, 2005.
