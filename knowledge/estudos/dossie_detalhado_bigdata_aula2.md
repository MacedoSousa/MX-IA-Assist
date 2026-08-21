# Dossiê detalhado — Veracidade, valor, fontes e aplicações de Big Data

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `BigData Aula2.pdf` |
| Tipo | Material teórico acadêmico fornecido ao projeto |
| Extensão observada | 19 páginas físicas, com conteúdo principal nas páginas 1, 3 e 5–16 |
| Unidade | Definições, Fontes de Dados e Exemplos de Big Data |
| Responsável indicado no material | Prof. Dr. Alberto Messias |
| Tema central | Veracidade, valor, importância, fontes e aplicações de Big Data |
| Uso neste dossiê | Documentação técnica original, análise crítica e integração com o MX |

## 1. Objetivos e tese central

O material amplia os três Vs introdutórios — volume, variedade e velocidade — com **veracidade** e **valor**. A tese prática é direta: uma plataforma capaz de processar muitos dados não produz automaticamente boas decisões. Os dados precisam ser confiáveis o bastante para a finalidade pretendida, e o processamento precisa gerar resultado útil para uma organização, produto, operação ou usuário.

Essa tese evita uma visão puramente tecnológica. Big Data deve ser avaliado como uma cadeia completa: origem, coleta, qualidade, linhagem, proteção, processamento, interpretação, decisão e retorno. Um pipeline rápido sobre dados incorretos apenas automatiza erro; um repositório bem governado que não melhora nenhuma decisão pode ser tecnicamente interessante, mas não necessariamente valioso.

## 2. Os cinco Vs

| V | Definição operacional | Pergunta de controle |
|---|---|---|
| Volume | Quantidade total e crescimento dos dados | Conseguimos armazenar, processar e recuperar dentro do custo e prazo? |
| Velocidade | Ritmo de geração, transmissão, ingestão e análise | Qual latência é necessária e qual pico precisa ser suportado? |
| Variedade | Diversidade de formatos, estruturas, fontes e semânticas | Como integrar texto, tabela, imagem, áudio, eventos e registros? |
| Veracidade | Grau de confiança, qualidade, consistência, acurácia e rastreabilidade | Podemos confiar neste dado para esta decisão específica? |
| Valor | Benefício econômico, operacional, científico ou social obtido | Qual decisão melhora, qual risco diminui ou qual custo é evitado? |

Os Vs não são independentes. Aumentar fontes pode elevar variedade e volume, mas também reduzir veracidade. Exigir baixa latência pode limitar etapas de validação. Preservar tudo indefinidamente pode aumentar volume, vulnerabilidade e custo sem aumentar valor. Uma arquitetura madura explicita esses conflitos.

O material também menciona extensões para até dez Vs: **variabilidade**, **validade**, **vulnerabilidade**, **volatilidade** e **visualização**. Essas categorias não substituem automaticamente os cinco Vs centrais; funcionam como lentes complementares para situações em que inconsistência, correção, segurança, prazo de utilidade e interpretação visual são decisivos.

## 3. Veracidade: confiabilidade orientada à finalidade

Veracidade é mais ampla que “o dado parece correto”. O material destaca três componentes: qualidade ou limpeza, origem e linhagem ao longo do tempo, e adequação do nível de confiança ao uso planejado. Portanto, um dado pode ser suficientemente confiável para exploração preliminar e insuficiente para uma decisão regulatória ou financeira.

A análise de veracidade deve começar pela procedência. É necessário saber de onde o dado veio, se a origem é interna ou externa, qual sistema o produziu, se houve transformação, se o registro foi auditado e quais versões do esquema foram utilizadas. Em fontes públicas, redes sociais e agregadores, também importa distinguir fato observado, opinião, estimativa, conteúdo fabricado e dado manipulado intencionalmente.

### 3.1 Dimensões de qualidade

| Dimensão | Pergunta | Exemplo de indicador |
|---|---|---|
| Completude | Campos essenciais estão presentes? | percentual de registros sem `source_id` |
| Consistência | O mesmo conceito possui valores compatíveis entre fontes? | divergência de unidade, moeda ou fuso |
| Acurácia | O valor representa adequadamente o fenômeno? | comparação com fonte de referência |
| Atualidade | O dado ainda é válido para o uso? | idade média e atraso de ingestão |
| Unicidade | Há duplicatas ou reprocessamentos indevidos? | duplicatas por chave idempotente |
| Rastreabilidade | É possível localizar origem e transformações? | cobertura de linhagem por campo |
| Validade | O valor satisfaz formato e domínio? | percentual de datas e códigos válidos |
| Integridade | O dado foi alterado ou corrompido? | hash, assinatura ou verificação de checksum |

A qualidade deve ser medida na entrada e novamente após transformações importantes. Uma limpeza pode remover outliers úteis, destruir contexto ou introduzir viés. Por isso, o dado bruto deve ser preservado quando houver justificativa, e a transformação deve produzir registro de regras, versão e motivo.

### 3.2 Proveniência e linhagem

Para o MX, cada evidência recuperada deve carregar metadados suficientes para explicar sua origem: identificador do documento, versão, nome do arquivo, página, seção, data de coleta, hash, método de extração, política de acesso e status de revisão. Em dados gerados por ferramentas, a linhagem deve incluir a chamada, a versão do contrato, a identidade do agente e o resultado validado.

Um registro mínimo de proveniência pode ser representado assim:

```text
EvidenceRecord
- evidence_id
- source_id
- source_version
- content_hash
- locator            # página, seção, timestamp ou offset
- collected_at
- transformed_at
- extraction_method
- quality_status
- access_policy
- reviewer_status
```

A linhagem não é apenas uma funcionalidade de auditoria. Ela permite corrigir respostas, reprocessar somente versões afetadas, comparar mudanças entre materiais e impedir que conhecimento revogado continue sendo recuperado.

### 3.3 Classificação de confiabilidade

Uma classificação simples pode combinar regras determinísticas e revisão humana:

| Nível | Característica | Uso recomendado |
|---|---|---|
| A | Fonte identificada, versão conhecida, conteúdo íntegro e revisão concluída | Respostas fundamentadas e decisões operacionais autorizadas |
| B | Fonte identificada e parcialmente validada, com pequenas lacunas | Exploração e resposta com ressalva |
| C | Fonte externa ou transformada com incertezas relevantes | Hipótese, nunca afirmação definitiva |
| D | Origem desconhecida, conteúdo conflitante ou suspeita de manipulação | Bloquear ou solicitar verificação |

A classificação não deve ser confundida com verdade absoluta. Ela representa confiança documentada para uma finalidade, data e política específicas.

## 4. Valor: transformar dados em resultado

O V de valor pergunta se o investimento em coleta, armazenamento, processamento, segurança e análise produz retorno. Esse retorno pode ser aumento de receita, redução de custo, prevenção de fraude, manutenção preditiva, melhoria de atendimento, redução de risco, descoberta científica ou aumento de produtividade.

O material recomenda experimentação associada a um caso de teste que impulsione o negócio. Isso significa começar por uma hipótese mensurável, e não por uma plataforma genérica. A sequência recomendada é:

```text
problema -> hipótese -> dados necessários -> experimento -> métrica de resultado
         -> decisão de continuidade -> produto de dados ou descarte
```

Para o MX, um caso de valor pode ser reduzir o tempo de localização de evidências acadêmicas. A hipótese seria: “a recuperação com metadados, busca híbrida e reranking reduz o tempo e a taxa de respostas sem fonte”. As métricas devem incluir latência, precisão de recuperação, cobertura de citações, taxa de abstenção correta e satisfação do usuário. O número de documentos ingeridos é uma métrica de operação, não prova de valor.

### 4.1 Indicadores de retorno

| Objetivo | Métrica de resultado | Cuidado de interpretação |
|---|---|---|
| Reduzir custo | custo por consulta, documento ou decisão | Não ignorar custo de armazenamento, observabilidade e operação |
| Melhorar atendimento | tempo de resposta e resolução | Não sacrificar correção e segurança por baixa latência |
| Reduzir erros | taxa de falhas e de correções | Medir gravidade, não apenas quantidade |
| Aumentar receita | conversão, retenção ou valor por cliente | Controlar sazonalidade e fatores externos |
| Aumentar produtividade | tempo economizado com qualidade preservada | Verificar se o trabalho foi deslocado para revisão humana |
| Melhorar decisões | precisão, cobertura e impacto da decisão | Separar correlação de causalidade |

A decisão de investir deve considerar valor líquido, risco e tempo de realização. Um projeto com alto potencial, mas sem dados confiáveis ou sem mecanismo de decisão, deve primeiro financiar uma etapa de validação, não uma implantação irreversível.

## 5. Vs complementares

O material lista cinco extensões que ajudam a tornar a análise mais crítica.

| V complementar | Interpretação prática para o MX |
|---|---|
| Variabilidade | O mesmo campo ou evento muda de significado, distribuição ou formato ao longo do tempo |
| Validade | O dado satisfaz regras formais e semânticas para o uso pretendido |
| Vulnerabilidade | O dado pode ser exposto, roubado, envenenado, alterado ou usado fora da finalidade |
| Volatilidade | O dado perde relevância após determinado período e deve ser retido ou descartado conforme política |
| Visualização | O resultado precisa ser representado de forma compreensível diante de alta dimensão e escala |

Esses Vs introduzem responsabilidades de governança. Variabilidade requer versionamento e detecção de mudança de distribuição. Validade requer regras de domínio. Vulnerabilidade requer controle de acesso, criptografia, isolamento, auditoria e proteção contra prompt injection ou envenenamento de base. Volatilidade requer retenção diferenciada. Visualização requer agregação, amostragem, clusters, árvores, diagramas ou outras formas que não ocultem incerteza.

## 6. Por que Big Data é importante

Segundo o material, Big Data é especialmente útil quando a análise precisa combinar dados estruturados, semiestruturados e não estruturados; quando uma amostra não representa bem o fenômeno; quando a exploração é iterativa; ou quando as perguntas comerciais ainda não estão completamente predeterminadas.

Essa importância não significa que sempre se deve analisar “todos os dados”. Analisar o conjunto completo pode ser necessário em alguns problemas, mas pode ser caro, redundante ou estatisticamente inadequado em outros. A decisão deve considerar representatividade, viés, custo, latência e risco. Big Data amplia o espaço de investigação; não elimina a necessidade de método científico, estatística, governança e conhecimento de domínio.

O material destaca desafios como uso de formatos não relacionais, integração de comentários e call centers, decisão sensível ao tempo, análise de logs e sensores, associação entre fontes novas e tradicionais e redução do custo total de propriedade. A proposta é complementar soluções existentes e adotar sinergia arquitetural, não substituir sistemas relacionais de maneira indiscriminada.

## 7. Cinco casos de uso principais

### 7.1 Grande exploração de dados

A exploração reúne dados crus, estruturados e não estruturados para descoberta, pesquisa, indexação, análise textual, autoatendimento de fontes, modelagem preditiva e análise profunda. O fluxo deve preservar o dado original e separar exploração de produção, pois uma hipótese exploratória não deve alterar automaticamente um processo operacional.

No MX, essa capacidade corresponde à ingestão de PDFs, livros autorizados, logs técnicos e registros de estudo, seguida de extração, indexação, busca híbrida e avaliação de evidências. Sandboxes devem ter limites de acesso, retenção e custo.

### 7.2 Visão 360 graus do cliente ou usuário

A visão 360 combina transações, interações, preferências, histórico de atendimento e sinais comportamentais. A principal dificuldade é resolver identidade sem criar associações incorretas. No MX, a visão do usuário deve ser restrita ao necessário, com consentimento, separação de perfis e possibilidade de exclusão.

### 7.3 Segurança e inteligência

Logs, eventos, rede, identidade e fontes externas podem ser correlacionados para detecção de anomalias, fraude e ameaças. A velocidade pode ser fundamental, mas falsos positivos também têm custo. Alertas devem conter evidência, severidade, confiança, contexto e procedimento de revisão.

### 7.4 Análise de operações

Sensores, logs, métricas e traces permitem identificar gargalos, prever indisponibilidade e verificar contratos de nível de serviço. A qualidade da análise depende de relógios sincronizados, correlação de eventos, retenção adequada e instrumentação consistente.

### 7.5 Aumento do data warehouse

Dados de sensores, comportamento, preços e transações podem ser combinados para ampliar a análise histórica. Uma arquitetura híbrida permite que o warehouse mantenha consultas estruturadas enquanto fontes novas são processadas em uma camada complementar. O resultado deve ser reconciliado com definições de negócio, evitando métricas duplicadas ou conflitantes.

## 8. Fontes de dados

O material organiza as fontes em tradicionais, novas, estruturadas, semiestruturadas e não estruturadas. A origem determina formato, frequência, qualidade, direitos de uso e risco.

| Fonte | Natureza | Uso típico | Riscos e controles |
|---|---|---|---|
| Redes sociais | Texto, mídia, metadados e eventos | sentimento, tendências e comportamento | termos de uso, viés, autenticidade e privacidade |
| Web logs | Semiestruturados | diagnóstico, comportamento e segurança | dados pessoais, alta cardinalidade e retenção |
| Máquinas e sensores | Séries temporais e eventos | manutenção, ambiente e monitoramento | calibração, ruído, sincronização e volume |
| GPS/geolocalização | Dados espaciais e temporais | logística, navegação e emergência | rastreamento indevido, consentimento e precisão |
| Streaming | Fluxo contínuo de qualquer tipo | fraude, segurança, tráfego e saúde | atraso, duplicidade, ordem e backpressure |
| Conversas de clientes | Texto e áudio, frequentemente não estruturados | atendimento e intenção | conteúdo sensível, transcrição e anonimização |
| Fontes públicas | Documentos, APIs e bases abertas | enriquecimento e pesquisa | licença, atualização, integridade e origem |
| Sistemas tradicionais | Tabelas e registros transacionais | referência, reconciliação e histórico | semântica, integração e conflitos de chave |

### 8.1 Redes sociais

Blogs, publicações, fóruns, feeds e plataformas sociais geralmente oferecem APIs e metadados próprios. A coleta deve respeitar autorização e finalidade. O conteúdo pode ser opinião, ironia, duplicação, campanha coordenada ou material manipulado; logo, não deve ser tratado como medição direta de comportamento sem validação.

### 8.2 Web logs

Web logs registram ambiente, requisições, origem, início e fim de conexões, erros e atividade de servidores. Historicamente usados para diagnóstico, eles também podem ser combinados com outras fontes para estudar comportamento e ameaças. O MX deve aplicar redaction de tokens, IPs e dados sensíveis antes de indexar logs em sistemas de consulta geral.

### 8.3 Dados gerados por máquinas

A categoria inclui RFID, sensores ópticos, de áudio, sísmicos, térmicos, químicos, médicos, climáticos, rodoviários, televisores, câmeras e dispositivos vestíveis. Para serem úteis, os valores precisam de unidade, escala, frequência, timestamp, localização e identificação do sensor. Uma mesma temperatura textual ou numérica deve ser normalizada para um padrão explícito.

### 8.4 GPS e geolocalização

GPS pode apoiar navegação, rastreamento logístico, segurança, emergência e análise de circulação. É uma fonte sensível: localização revela rotina, relações e hábitos. O MX deve coletar apenas o necessário, aplicar retenção curta quando a localização não for indispensável e separar dados identificáveis de estatísticas agregadas.

### 8.5 Streaming

Streaming é um modo de processamento contínuo, não um formato de dado. Uma aplicação recebe eventos, processa-os e os encaminha a outras aplicações, normalmente com baixa latência. Os contratos precisam definir entrega, duplicidade, ordem, reprocessamento, janela temporal, tolerância a atraso e tratamento de eventos inválidos.

## 9. Exemplos e setores

O material apresenta recomendação de filmes e notícias, monitoramento de segurança física, classificação de clientes, análise de sentimento, análise de aprendizagem, detecção de anomalias em redes, web analytics, séries temporais de sensores, fraude financeira, jogos massivos e dados médicos.

| Setor | Aplicações exemplificativas | Requisito crítico |
|---|---|---|
| Automotivo | sensores de veículos e localização de problemas | telemetria confiável e baixa latência |
| Serviços financeiros | risco, fraude, carteiras e novos produtos | auditoria, explicabilidade e segurança |
| Manufatura | qualidade e garantia | rastreabilidade e séries temporais |
| Saúde | sensores, prontuários e qualidade do cuidado | privacidade, integridade e supervisão profissional |
| Óleo e gás | análise de sensores de exploração | volume, ambiente adverso e confiabilidade |
| Varejo | sentimento, marketing, cesta, previsão e estoque | identidade, consentimento e integração |
| Utilities | medidores inteligentes e capacidade de rede | continuidade, séries temporais e segurança |
| Segurança pública | ameaças, tráfego, imagem e redes sociais | legalidade, viés e governança |
| Publicidade | segmentação, localização, retargeting e churn | finalidade, transparência e proteção de dados |

Esses exemplos devem ser tratados como padrões de aplicação, não como autorização automática para coletar qualquer dado. O caso de uso precisa ser avaliado pela finalidade, base legal ou autorização aplicável, risco de discriminação, impacto sobre pessoas e necessidade de revisão humana.

## 10. Arquitetura aplicada ao MX

A arquitetura recomendada para o MX deve conectar o V de valor ao V de veracidade. O fluxo abaixo separa aquisição, controle, transformação, recuperação e decisão:

```text
Fonte autorizada
  -> registro de origem e finalidade
  -> validação de esquema, integridade e sensibilidade
  -> armazenamento bruto versionado
  -> limpeza e normalização reproduzíveis
  -> enriquecimento com metadados e linhagem
  -> indexação lexical/vetorial
  -> recuperação e reranking
  -> resposta ou alerta com evidência e confiança
  -> avaliação de qualidade, custo, latência e impacto
```

Um conjunto mínimo de contratos pode incluir `SourceRegistration`, `DataQualityReport`, `EvidenceRecord`, `RetentionPolicy`, `StreamingContract` e `ValueExperiment`. O `ValueExperiment` deve registrar hipótese, população, período, métrica principal, métricas de segurança, baseline, resultado e decisão. O `DataQualityReport` deve registrar contagens de entrada e saída, nulos, duplicatas, falhas de parsing, outliers, versão do pipeline e amostra para revisão.

### 10.1 Regras de decisão

O MX deve aceitar uma fonte para respostas fundamentadas quando a origem estiver identificada, a versão for conhecida, a transformação for reproduzível e a qualidade for compatível com o uso. Deve responder com ressalva quando houver conflito ou lacuna, e deve se abster quando a evidência for insuficiente, revogada, sem origem ou incompatível com a pergunta.

O sistema também deve distinguir **valor de produto** de **volume de conhecimento**. Adicionar materiais pode aumentar cobertura, mas também gerar conflitos, conteúdo desatualizado e custo de recuperação. A incorporação deve passar por avaliação de duplicidade, autoridade, direitos de uso, qualidade e impacto na resposta.

## 11. Exercícios autorais e gabaritos

### Exercício 1 — veracidade por finalidade

Um documento possui fonte identificada, mas foi convertido por OCR com 8% de erros em tabelas. Pode ser usado para responder conceitos gerais e para calcular indicadores financeiros?

**Gabarito orientativo:** pode ser aceitável para conceitos gerais depois de revisão amostral e com citação da página; não deve ser usado diretamente para cálculo financeiro sem validação das tabelas, conferência com fonte original e registro de incerteza. Veracidade é relativa ao uso, e erro de OCR em números pode alterar a decisão.

### Exercício 2 — experimento de valor

Proponha um experimento para verificar se a busca híbrida do MX é superior à busca vetorial isolada.

**Gabarito orientativo:** construir um conjunto de perguntas representativas com respostas e evidências esperadas; comparar os dois métodos com mesmo corpus e limite de latência; medir recall de evidência, precisão do primeiro resultado, taxa de citação correta, latência p95, custo por consulta e taxa de abstenção adequada. O experimento só deve prosseguir para produção se o ganho de qualidade superar custo e complexidade.

### Exercício 3 — fonte de streaming

Uma aplicação de segurança recebe eventos fora de ordem e duplicados. Quais campos e mecanismos devem existir?

**Gabarito orientativo:** o evento deve conter `event_id`, `event_time`, `ingestion_time`, `source_id`, `sequence_number`, `partition_key`, `schema_version` e `trace_id`. O consumidor deve deduplicar por `event_id`, usar janelas de tolerância a atraso, definir política para eventos tardios, persistir offsets e enviar mensagens inválidas para DLQ. A análise deve distinguir hora do acontecimento e hora da chegada.

### Exercício 4 — retenção e volatilidade

Um log contém informações úteis para diagnóstico por 30 dias, mas inclui identificadores pessoais. Como desenhar a retenção?

**Gabarito orientativo:** aplicar minimização e mascaramento na entrada; manter dados detalhados pelo menor período necessário ao diagnóstico; produzir métricas agregadas sem identificadores para retenção maior; controlar acesso, registrar auditoria e executar deleção verificável. A política deve ser aprovada para a finalidade e não baseada apenas na conveniência técnica.

### Exercício 5 — caso de uso híbrido

Relacione dados de sensores, vendas e estoque para prever ruptura de produto. Indique onde o data warehouse e a plataforma de Big Data podem cooperar.

**Gabarito orientativo:** a camada distribuída pode ingerir e processar sensores e eventos de alta velocidade; o warehouse pode consolidar vendas, estoque e dimensões de negócio; uma camada analítica pode unir os dados por produto, loja e tempo. A previsão precisa de validação contra histórico, controle de vazamento temporal, tratamento de valores ausentes e monitoramento de degradação.

## 12. Checklist de domínio

| Competência | Critério de domínio |
|---|---|
| Veracidade | Explica qualidade, origem, linhagem e adequação ao uso |
| Valor | Converte hipótese de negócio em experimento e métrica |
| Fontes | Classifica fontes estruturadas, semiestruturadas e não estruturadas |
| Streaming | Diferencia fluxo de formato e define garantias operacionais |
| Governança | Considera origem, direitos, retenção, acesso e auditoria |
| Casos de uso | Relaciona exploração, cliente, segurança, operações e warehouse |
| Arquitetura | Combina sistemas existentes sem adotar Big Data por moda |
| MX | Implementa evidência, confiança, experimentos e abstenção |

## 13. Limitações e cautelas

As estatísticas empresariais citadas na aula, incluindo percentuais atribuídos a uma pesquisa da KPMG, devem ser entendidas como dados históricos apresentados pelo material. Não foram tratados como indicadores atuais do mercado. Da mesma forma, os exemplos de plataformas, redes sociais e setores são referências didáticas; uma implementação real exigiria verificar APIs, termos de uso, legislação, segurança, custos e disponibilidade atuais.

A expansão de três para cinco ou dez Vs é útil pedagogicamente, mas não constitui uma taxonomia universal. Organizações podem usar nomes diferentes ou decompor as dimensões de outra forma. O ponto essencial é tornar explícitos os riscos e decisões que ficam escondidos sob o rótulo Big Data.

Este dossiê foi redigido originalmente a partir do PDF salvo no projeto. Ele não reproduz o texto integral do material; reorganiza os conceitos em uma estrutura de estudo, acrescenta critérios de engenharia, contratos, exemplos próprios e exercícios para integração com o MX.

## Referências

[1]: `BigData Aula2.pdf`, páginas 5, 8–16, material teórico salvo no projeto Estudos.

[2]: BALLARD, Chuck et al. *Information Governance Principles and Practices for a Big Data Landscape*. IBM Redbooks, 2014. Disponível em: http://www.redbooks.ibm.com/abstracts/sg248165.html

[3]: LEPLANTE, Alice. *The Big Data Transformation: Understanding Why Change Is Actually Good for Your Business*. O’Reilly, 2016. Disponível conforme referência do material em: https://saas.hpe.com/en-us/asset/big-data-software/big-data-transformation-understanding-why-change-actually-good-your-business

[4]: Material complementar indicado no PDF: TDWI, Exame, DataFloq e IBM, com os endereços abreviados registrados nas páginas 15–16 do arquivo-fonte.
