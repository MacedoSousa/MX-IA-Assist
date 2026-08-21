# Dossiê detalhado — Mahout, Spark, Hadoop single-node e execução de jobs

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `BigData Aula5.pdf` |
| Tipo | Material teórico acadêmico fornecido ao projeto |
| Extensão observada | 25 páginas físicas, com conteúdo principal nas páginas 1, 3 e 5–21 |
| Unidade | Outros Projetos Importantes do Hadoop, sua Instalação e Execução |
| Tema central | Mahout, Spark, instalação de Hadoop, HDFS e Word Count |
| Uso neste dossiê | Documentação técnica original, análise crítica e aplicação ao MX |

## 1. Tese central

A aula complementa o ecossistema Hadoop com dois projetos importantes: Mahout, voltado a algoritmos distribuídos de aprendizado de máquina, e Spark, plataforma de processamento que explora memória distribuída e suporta SQL, streaming, grafos e machine learning. Em seguida, apresenta uma instalação didática de Hadoop em um único nó Linux e um experimento de contagem de palavras.

O valor atual do material está nos princípios: escolher o motor conforme o padrão de carga, entender dependências de instalação, distinguir armazenamento de execução e validar um pipeline mínimo ponta a ponta. Os comandos e versões do PDF são históricos e não devem ser executados sem adaptação e verificação de segurança.

## 2. Apache Mahout

Mahout é apresentado como projeto Apache de código aberto com algoritmos de aprendizado de máquina para conjuntos muito grandes, executáveis em clusters Hadoop ou em uma máquina. O material destaca implementação em Java e integração com MapReduce, Spark, H2O e Flink.

### Algoritmos citados

| Categoria | Exemplos apresentados |
|---|---|
| Classificação | Naive Bayes, Hidden Markov Models, regressão logística e Random Forest |
| Clustering | k-Means, Canopy, Fuzzy k-Means, Streaming KMeans e Spectral Clustering |
| Pós-processamento | exportação e visualização de clusters |
| Redução de dimensionalidade | SVD, Lanczos, Stochastic SVD, PCA via Stochastic SVD e decomposição QR |
| Texto e suporte | similaridade de linhas, collocations, vetores esparsos TF-IDF, XML e e-mail |

A escolha do algoritmo não deve partir apenas da disponibilidade no framework. É necessário avaliar rótulos, dimensionalidade, esparsidade, distribuição dos dados, custo de iteração, qualidade do resultado e possibilidade de explicar a decisão.

## 3. Apache Spark

Spark é descrito como plataforma de processamento que pode otimizar cargas MapReduce por meio de execução em memória. A afirmação de ser “até 100 vezes mais rápido” é uma referência didática dependente do cenário; não é garantia universal. O desempenho depende de cache, memória disponível, shuffle, serialização, tamanho dos dados, rede, algoritmo e comparação realizada.

Spark pode trabalhar com HDFS, HBase, Cassandra, MongoDB, S3 e outros armazenamentos. O processamento não exige que todos os dados residam permanentemente na memória: dados podem estar em armazenamento externo e partes do grafo de execução podem ser recomputadas ou persistidas conforme a política escolhida.

### Componentes

| Componente | Função | Aplicação possível no MX |
|---|---|---|
| Spark Core | execução distribuída e base de recursos | jobs de transformação e enriquecimento |
| Spark SQL | consulta e tratamento de dados estruturados/semi-estruturados | validação e agregações de documentos |
| Spark Streaming | processamento de fluxos ou micro-lotes | eventos de ingestão, telemetria e filas |
| MLlib | aprendizado de máquina distribuído | classificação, clustering e features em escala |
| GraphX | processamento distribuído de grafos | relações entre documentos, entidades ou serviços |
| Spark R | uso do Spark a partir do ecossistema R | análise estatística especializada |
| RDD | abstração distribuída resiliente | pipelines que precisam de transformações e recomputação |

## 4. RDD e tolerância a falhas

RDDs são apresentados como conjuntos de dados distribuídos e resilientes, com possibilidade de armazenamento em memória e rastreamento da origem das transformações. Esse rastreamento, conhecido como lineage, permite recomputar partições perdidas em vez de depender exclusivamente de cópias persistentes.

O lineage não elimina todos os riscos. Se a origem externa for alterada, indisponível ou não determinística, a recomputação pode produzir resultado diferente. Para o MX, entradas devem ser versionadas, transformações devem registrar versão de código e os resultados importantes devem possuir checksum ou armazenamento persistente.

## 5. Word Count em Spark

O exemplo conceitual da aula segue o fluxo:

```text
ler arquivo
  -> opcionalmente armazenar em cache
  -> dividir linhas em palavras
  -> mapear cada palavra para (palavra, 1)
  -> reduzir por chave somando contagens
  -> coletar e imprimir resultados
```

A operação representa um padrão clássico de agregação distribuída. Em produção, a implementação deve tratar pontuação, acentuação, caixa, tokenização, idioma, stopwords, linhas vazias e arquivos grandes. `collect()` é adequado para uma saída pequena; coletar um resultado gigantesco no driver pode provocar falta de memória.

## 6. Instalação single-node apresentada no material

A aula descreve uma instalação histórica em Linux Debian com usuário dedicado, Java 8, SSH local, Hadoop 2.7.1, variáveis de ambiente, arquivos XML e diretórios de NameNode e DataNode. O fluxo conceitual é:

```text
atualizar sistema
  -> criar grupo e usuário Hadoop
  -> instalar Java compatível
  -> configurar JAVA_HOME
  -> instalar e configurar SSH
  -> baixar e descompactar Hadoop
  -> ajustar permissões e PATH
  -> configurar core-site, hdfs-site, mapred-site e yarn-site
  -> criar diretórios de dados
  -> formatar NameNode
  -> iniciar serviços
  -> validar interfaces e comandos
```

### Cautela fundamental

Os comandos do PDF usam repositórios, versões, chaves e sintaxe antigos. Não se deve copiar essas instruções diretamente para um servidor atual. Em especial, adicionar repositórios não verificados, usar versões sem suporte, executar como root ou baixar tarballs sem validação de hash pode comprometer o sistema.

Para estudo atual, a instalação deve ocorrer em uma VM ou container isolado, com versão suportada, download HTTPS, assinatura ou checksum verificado, usuário sem privilégios, firewall restritivo e dados não sensíveis. O ambiente single-node é laboratório; não fornece tolerância real a falhas de um cluster.

## 7. Configuração e componentes

Os arquivos citados na aula representam responsabilidades diferentes:

| Arquivo | Propósito geral |
|---|---|
| `core-site.xml` | propriedades centrais, incluindo URI do filesystem |
| `hdfs-site.xml` | diretórios, replicação e parâmetros do HDFS |
| `mapred-site.xml` | configuração do framework de MapReduce |
| `yarn-site.xml` | ResourceManager, NodeManager e recursos YARN |
| `hadoop-env.sh` | variáveis do ambiente Hadoop, incluindo Java |

O processo requer diretórios de NameNode e DataNode e uma formatação inicial do filesystem. Formatar um NameNode é operação destrutiva para o namespace existente; jamais deve ser feita em um ambiente que contenha dados que precisem ser preservados.

## 8. Operação e observabilidade

O material destaca interfaces web para navegar no HDFS, observar logs, datanodes, capacidade e aplicações em execução. Esses recursos são úteis para laboratório, mas uma operação profissional precisa de métricas, alertas, retenção de logs, rastreamento, controle de acesso e auditoria.

Indicadores mínimos:

| Domínio | Métricas |
|---|---|
| HDFS | capacidade, blocos sub-replicados, erros de leitura, espaço livre |
| YARN | filas, jobs ativos, memória, CPU, falhas e tempo de espera |
| MapReduce | duração, map failures, reduce failures, shuffle e skew |
| Spark | executor memory, GC, stages, shuffle, spill e taxa de tarefas |
| Dados | volume, partições, arquivos pequenos, schema e qualidade |
| Operação | disponibilidade, incidentes, restauração e custo |

## 9. Comandos HDFS e semântica

O material cita comandos com funções semelhantes às de Unix/Linux: listar, remover, remover diretórios, exibir, copiar, buscar, alterar permissões, alterar proprietário, mover e anexar. Também cita `copyFromLocal`, `moveFromLocal` e `appendToFile`.

A diferença essencial é que o comando deve ser executado no contexto do filesystem distribuído. Antes de remover ou mover, deve-se verificar URI, usuário, caminho, permissões e política de retenção. Em produção, não se deve confundir shell de laboratório com governança de dados.

## 10. Exemplo de Word Count com Hadoop

O fluxo apresentado é:

```text
arquivo local ola.txt
  -> copyFromLocal para o HDFS
  -> execução do jar de exemplos com wordcount
  -> diretório de saída no HDFS
  -> leitura ou download de part-r-00000
```

A saída contém a palavra e sua frequência. O exemplo também demonstra uma limitação importante: o job não realiza limpeza ou pré-processamento automaticamente. Pontuação, caixa, acentos e tokens compostos podem produzir resultados semanticamente inadequados.

Em uma base de blogs, o mesmo job pode ser aplicado a um autor, a uma pasta de testes ou a um conjunto maior. O tempo cresce conforme o volume, a leitura, o shuffle e a capacidade do cluster. Em single-node, o experimento serve para validar o fluxo, não para medir escalabilidade distribuída.

## 11. Pipeline equivalente para o MX

Um experimento autoral de contagem de termos no MX deve separar ingestão, normalização, tokenização e agregação:

```text
fontes autorizadas
  -> hash e registro de origem
  -> extração de texto
  -> normalização por idioma
  -> tokenização e remoção de ruído
  -> contagem distribuída
  -> armazenamento de métricas
  -> visualização e avaliação
```

Contrato mínimo de uma saída:

```text
TermCount
- term
- normalized_term
- count
- source_collection
- time_window
- tokenizer_version
- job_id
- generated_at
```

O campo `tokenizer_version` é indispensável. Sem ele, uma mudança de tokenização pode alterar a contagem sem que seja possível explicar a diferença.

## 12. Mahout, Spark ou outra abordagem

| Cenário | Alternativa inicial | Justificativa |
|---|---|---|
| Job batch simples em arquivos grandes | engine distribuído de batch | agregação e reprocessamento previsível |
| Muitas transformações e iteração | Spark ou engine equivalente | DAG, cache e execução distribuída |
| Machine learning distribuído histórico | Mahout/Spark ML | algoritmos e integração com cluster |
| Pipeline pequeno de documentos | Python/SQL/worker simples | evita custo de cluster e complexidade desnecessária |
| Streaming contínuo | broker + consumidor stateful | latência, checkpoint e controle de offset |
| Busca e recuperação do MX | índice lexical/vetorial | serving de baixa latência, não batch HDFS |

A regra é não introduzir Hadoop ou Spark por prestígio tecnológico. O motor deve ser escolhido pela carga, latência, volume, qualidade exigida, competência da equipe, custo e manutenção.

## 13. Exercícios autorais e gabaritos

### Exercício 1 — Spark e `collect`

Um job produz 500 milhões de pares de palavras e contagens. O desenvolvedor usa `collect()` para exibir o resultado. Qual é o risco?

**Gabarito orientativo:** o driver precisa receber todo o resultado, podendo esgotar memória e rede. O correto é agregar mais, limitar a amostra, salvar a saída distribuída ou usar uma consulta de top-N com particionamento apropriado.

### Exercício 2 — cache

Quando `cache()` ajuda e quando pode piorar o job?

**Gabarito orientativo:** ajuda quando o mesmo dataset é reutilizado e cabe, total ou parcialmente, na memória disponível. Pode piorar quando o dataset é usado uma única vez, não cabe, provoca eviction, aumenta GC ou compete com outras tarefas. A decisão deve ser validada por métricas.

### Exercício 3 — instalação segura

O material indica baixar um tarball histórico e executar instalação como administrador. Como adaptar para um laboratório atual?

**Gabarito orientativo:** usar VM ou container isolado, release suportada, origem HTTPS, assinatura/checksum, usuário sem privilégio, permissões mínimas, firewall, snapshot e dados sintéticos. Registrar a versão e automatizar a instalação para reproduzir o ambiente.

### Exercício 4 — Word Count correto

Por que o resultado de uma contagem simples pode ser enganoso em português?

**Gabarito orientativo:** caixa, acentos, pontuação, hífen, flexões, stopwords e tokenização alteram o que é considerado palavra. É preciso definir regras e registrar a versão. “Hadoop” e “hadoop” podem ser tratados como tokens diferentes se não houver normalização.

### Exercício 5 — seleção de motor

O MX precisa processar 300 PDFs por dia e gerar embeddings. Deve ser criado um cluster Hadoop?

**Gabarito orientativo:** não há justificativa automática. Deve-se medir tamanho, latência, paralelismo, custo e reprocessamento. Um pool de workers com fila e armazenamento de objetos pode ser mais simples. Spark ou cluster só entra se a carga e os requisitos de processamento distribuído compensarem a complexidade.

## 14. Checklist de domínio

| Competência | Critério de domínio |
|---|---|
| Mahout | Explica objetivo, famílias de algoritmos e limitações históricas |
| Spark | Diferencia Core, SQL, Streaming, MLlib, GraphX e RDD |
| Desempenho | Relaciona cache, memória, shuffle, driver e executors |
| Instalação | Entende usuário, Java, SSH, variáveis, XML e diretórios |
| Segurança | Identifica riscos de comandos históricos e downloads não verificados |
| HDFS | Executa fluxo de copiar, listar, consultar e preservar saída |
| Word Count | Explica map, reduce, tokenização e interpretação do resultado |
| MX | Escolhe motor proporcional ao volume e implementa rastreabilidade |

## 15. Limitações e atualização

O material referencia versões antigas de Hadoop, Java, jars, repositórios e ferramentas. Os nomes e números de desempenho têm finalidade didática e devem ser revalidados. A prática de executar `start-all.sh`, usar interfaces antigas ou operar versões legadas não deve ser transferida sem revisão para produção.

A aula é especialmente útil para compreender o ciclo completo de um job: preparar dados, armazenar, executar, acompanhar e recuperar resultados. Para sistemas atuais, devem ser consideradas alternativas gerenciadas, containers, orquestração, object storage, lakehouse, engines modernas de streaming e plataformas de ML.

Este dossiê foi escrito originalmente a partir do PDF salvo no projeto. Ele não reproduz integralmente o material; reorganiza os conceitos, corrige riscos de interpretação, acrescenta critérios de segurança e propõe aplicações ao MX.

## Referências

[1]: `BigData Aula5.pdf`, páginas 5, 8–21, material teórico salvo no projeto Estudos.

[2]: GIACOMELLI, P. *Apache Mahout Cookbook*. Packt Publishing, 2013, referência indicada no material.

[3]: WHITE, Tom. *Hadoop: The Definitive Guide*, referência do ecossistema Hadoop indicada nas aulas anteriores.

[4]: SCOTT, referência da figura e arquitetura Spark utilizada no material.

[5]: Apache Hadoop. Documentação de comandos HDFS, endereço abreviado indicado no PDF: https://goo.gl/BBdAzK.

[6]: Apache Spark. Documentação e introdução indicadas no material por links abreviados nas páginas 10–11.
