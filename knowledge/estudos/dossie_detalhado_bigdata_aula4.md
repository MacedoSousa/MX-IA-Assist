# Dossiê detalhado — IoT, Big Data, Hadoop e subprojetos

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `BigData Aula4.pdf` |
| Tipo | Material teórico acadêmico fornecido ao projeto |
| Extensão observada | 25 páginas físicas, com conteúdo principal nas páginas 1, 3 e 5–22 |
| Unidade | Big e IoT, Projeto Hadoop e Subprojetos |
| Tema central | Internet das Coisas, arquitetura IoT–Big Data e ecossistema Hadoop |
| Uso neste dossiê | Documentação técnica original, análise crítica e aplicação ao MX |

## 1. Tese central

O material conecta três camadas tecnológicas: o mundo físico instrumentado por dispositivos, a infraestrutura de comunicação e processamento, e as aplicações capazes de produzir decisões ou atuar novamente no ambiente. A Internet das Coisas amplia enormemente a quantidade de pontos de captura; Big Data fornece técnicas para armazenar e analisar os fluxos; Hadoop é apresentado como uma família de projetos para processamento distribuído em larga escala.

A conclusão arquitetural é que IoT não é apenas conectar sensores à internet. É necessário definir identidade, comunicação, gateway, middleware, armazenamento, processamento, segurança, atuação e governança. Do mesmo modo, Hadoop não é apenas um programa de processamento: é um ecossistema que combina armazenamento distribuído, gerenciamento de recursos, execução paralela, consulta, coordenação e operação.

## 2. Computação ubíqua e Internet das Coisas

O material parte da visão de computação ubíqua e pervasiva: a computação tende a desaparecer como objeto explícito e tornar-se integrada às atividades humanas. Dispositivos pequenos, sensores e capacidade de comunicação passam a participar de ambientes, veículos, sistemas industriais, casas, serviços públicos e aplicações móveis.

Uma definição operacional adequada é:

> **IoT é um ecossistema de objetos físicos e virtuais identificáveis, capazes de capturar, comunicar, processar ou receber comandos, conectados por redes e integrados a serviços que transformam dados em ações.**

O conceito de “coisa” é amplo. Pode ser um produto, uma etiqueta, uma máquina, uma câmera, um veículo, um medidor, um dispositivo vestível, uma infraestrutura urbana ou qualquer objeto que tenha identidade, estado, sensores, atuadores ou conectividade relevantes para uma finalidade.

## 3. Ecossistema de IoT

O material apresenta papéis de negócio associados ao ecossistema ITU-T.

| Papel | Responsabilidade | Risco de integração |
|---|---|---|
| Provedor de dispositivo | Fornece dispositivos, sensores, identificadores e dados brutos | calibração, firmware, formatos e propriedade dos dados |
| Provedor de rede | Oferece conectividade, integração e capacidades de transporte | indisponibilidade, latência, cobertura e segurança |
| Provedor de plataforma | Fornece interfaces, armazenamento, processamento e gerenciamento de dispositivos | lock-in, isolamento e evolução de APIs |
| Provedor de aplicação | Combina recursos para entregar serviço ao cliente | interpretação, autorização e responsabilidade pelo resultado |
| Cliente da aplicação | Usa a aplicação ou representa usuários finais | consentimento, experiência, acessibilidade e impacto |

A separação é útil para contratos e governança. Em uma solução pequena, os papéis podem pertencer à mesma organização; em uma solução ampla, podem ser fornecedores diferentes. A arquitetura deve registrar quem pode acessar o dado, quem responde por falhas, quem atualiza dispositivos e quem pode emitir comandos.

## 4. Tipos de dispositivos

O material apresenta três classes conceituais:

| Classe | Característica | Exemplo arquitetural |
|---|---|---|
| Passiva | Identificação e dados fixos, sem processamento significativo | etiqueta RFID ou identificador de produto |
| Contextual | Poder moderado, sensores e dados que variam com tempo e lugar | medidor ou dispositivo vestível |
| Conectada e autônoma | Comunicação em rede sem intervenção humana e possível capacidade de decisão | veículo, máquina industrial ou controlador inteligente |

A classificação não é absoluta. Um dispositivo pode mudar de classe à medida que recebe gateway, firmware, conectividade ou capacidade de processamento. Para fins de engenharia, é mais importante documentar energia, memória, CPU, conectividade, frequência de leitura, tolerância a atraso, segurança e possibilidade de atualização.

## 5. Captura, processamento e atuação

O fluxo básico descrito pelo material pode ser reorganizado da seguinte forma:

```text
objeto físico
  -> sensor ou identificador
  -> gateway/interrogador
  -> rede de comunicação
  -> plataforma de gerenciamento
  -> enriquecimento com contexto
  -> processamento analítico ou preditivo
  -> aplicação/serviço
  -> comando ou atuação no mundo físico
```

O gateway reduz a dependência de dispositivos pequenos. Ele pode agregar leituras, normalizar unidades, filtrar ruído, armazenar temporariamente, autenticar o dispositivo, aplicar regras locais e encaminhar somente eventos relevantes. Essa capacidade é importante quando a conectividade é intermitente ou quando transmitir todos os dados é caro.

O processamento pode ocorrer em três níveis:

| Nível | Objetivo | Quando usar |
|---|---|---|
| Dispositivo | captura, filtragem e controle imediato | baixa latência, baixa banda ou operação offline |
| Edge/gateway | agregação, normalização e decisões locais | conectividade limitada e muitos dispositivos |
| Nuvem/cluster | armazenamento histórico, correlação e ML | análises amplas, treinamento e visão global |

## 6. Middleware para IoT

O material destaca a necessidade de middleware para suportar escala. Um sensor simples não deve responder individualmente a milhares de aplicações móveis. O middleware oferece abstração e serviços compartilhados, como descoberta de dispositivos, autenticação, roteamento, armazenamento, normalização, processamento, cache, controle de comandos, telemetria e gerenciamento de falhas.

Arquitetura em camadas:

```text
Aplicação e interface do usuário
        -> APIs, regras, visualização e comandos
Middleware IoT
        -> identidade, registro, eventos, cache, integração e escala
Sensores e atuadores
        -> captura, medição e ação
Gateway e conectividade
        -> protocolo, agregação, segurança e store-and-forward
```

Um middleware deve controlar taxa de requisições, aplicar backpressure e isolar falhas. Também deve suportar versões de protocolo e permitir que dispositivos antigos continuem operando durante a migração.

## 7. Escala de dados em IoT

O exemplo do material usa uma cadeia de suprimentos com centenas de milhares de produtos e leituras frequentes para mostrar que poucos bytes por objeto podem gerar terabytes em períodos curtos. A lição é mais importante que o número histórico: **taxa de leitura, número de objetos, tamanho do evento e duração produzem uma multiplicação que deve ser calculada antes da implantação**.

```text
bytes_por_periodo = dispositivos × leituras_por_segundo × bytes_por_evento × duração
throughput = dispositivos × leituras_por_segundo × bytes_por_evento
```

O cálculo real deve incluir timestamp, identificador, localização, versão de schema, assinatura, cabeçalhos, transporte, replicação, índices e compressão. Filtrar, agregar e resumir no edge pode reduzir custos, mas não deve destruir dados necessários para auditoria ou investigação.

## 8. Arquitetura IoT e Big Data

A integração proposta reúne plataformas IoT, computação em nuvem, ingestão, armazenamento, processamento, machine learning, visualização e comunicação de retorno. Um desenho de referência é:

```text
Sensores/atuadores
  -> gateway seguro
  -> broker de eventos
  -> processamento edge
  -> armazenamento bruto versionado
  -> lake/warehouse/NoSQL
  -> batch e streaming analytics
  -> modelos e regras
  -> dashboards, alertas e comandos
```

O retorno ao mundo físico merece controles adicionais. Uma previsão ou classificação não deve acionar automaticamente um atuador crítico sem validação, limites, fallback e registro de decisão. Em ambientes médicos, industriais ou de segurança, a supervisão humana e os procedimentos de emergência são requisitos, não funcionalidades opcionais.

## 9. Hadoop: visão geral

O material apresenta Hadoop como uma família de projetos para computação distribuída e processamento de grandes conjuntos de dados. Sua ideia central é executar em clusters que podem crescer de um servidor para milhares de máquinas, usando recursos locais e tolerando falhas de nós por mecanismos no software.

O Hadoop não torna qualquer algoritmo distribuível. O ganho depende de particionamento, proximidade dos dados, custo de comunicação, tolerância a falhas e adequação do padrão de acesso. Uma análise moderna também deve considerar alternativas mais recentes e serviços gerenciados, pois o ecossistema evolui e componentes históricos podem estar descontinuados ou substituídos.

### Componentes centrais

| Componente | Função |
|---|---|
| Hadoop Common | bibliotecas, utilitários, abstrações e scripts compartilhados |
| HDFS | sistema de arquivos distribuído com alto débito e tolerância a falhas |
| YARN | agendamento de trabalhos e gerenciamento de recursos do cluster |
| MapReduce | modelo e API para processamento paralelo em fases de map e reduce |

## 10. MapReduce

MapReduce divide um processamento em tarefas que produzem pares de chave e valor. A fase de map lê registros e gera resultados intermediários; uma etapa de agrupamento organiza valores por chave; a fase de reduce agrega ou transforma cada grupo. O framework distribui tarefas, movimenta resultados, reexecuta partes com falha e produz uma saída consolidada.

Exemplo conceitual de contagem de eventos:

```text
map(evento):
    emitir(evento.tipo, 1)

reduce(tipo, contagens):
    emitir(tipo, soma(contagens))
```

O modelo funciona bem para agregações independentes e processamento em lote. Pode ser inadequado para baixa latência, algoritmos altamente iterativos, comunicação frequente entre tarefas ou cargas pequenas em que o custo de coordenação domina.

Aspectos a verificar:

| Aspecto | Pergunta |
|---|---|
| Particionamento | A chave distribui o trabalho sem criar um hot spot? |
| Localidade | O processamento ocorre próximo aos dados? |
| Shuffle | O volume intermediário cabe na rede e no tempo disponível? |
| Reexecução | A tarefa é idempotente e pode ser repetida? |
| Skew | Uma chave concentra dados demais? |
| Saída | O formato permite consumo posterior e auditoria? |

## 11. HDFS

HDFS é apresentado como um sistema de arquivos para arquivos muito grandes, acesso em fluxo e execução sobre hardware comum. Seu padrão favorece **escrever uma vez e ler muitas vezes**, com alto débito em vez de baixa latência inicial. A distribuição dos blocos e a tolerância a falhas permitem operar mesmo quando nós individuais falham.

### Forças

HDFS é adequado para grandes arquivos, processamento sequencial, replicação, análise em lote e clusters de hardware commodity. A disponibilidade não depende de todos os nós serem perfeitos; a plataforma detecta falhas e mantém cópias conforme a política.

### Limitações

| Limitação | Consequência |
|---|---|
| Baixa latência | Não é ideal para consultas na faixa de dezenas de milissegundos |
| Muitos arquivos pequenos | Metadados pressionam a memória do NameNode |
| Múltiplos escritores | O padrão não favorece escrita concorrente arbitrária |
| Alteração em posições internas | O padrão é anexar ou reescrever, não editar aleatoriamente |
| Baixa escala de carga | O custo de cluster e coordenação pode não compensar |

Para muitos arquivos pequenos, o pipeline deve compactar, agrupar ou usar armazenamento mais adequado. Para acesso aleatório de baixa latência, uma base distribuída orientada a colunas, como HBase no contexto didático da aula, pode ser mais apropriada.

## 12. Subprojetos Hadoop

### Apache Pig

Pig oferece uma linguagem de alto nível, Pig Latin, para expressar fluxos de análise de dados. O ambiente pode executar localmente em uma JVM ou de modo distribuído em um cluster. A abstração ajuda quem não quer escrever diretamente todas as etapas de execução, particionamento e otimização.

### Hive

Hive foi criado para permitir que analistas com forte conhecimento de SQL consultassem grandes volumes armazenados no HDFS. A lição arquitetural é separar armazenamento distribuído de uma interface de consulta mais acessível. Hive não deve ser confundido com um banco relacional transacional; seus padrões, latência, esquema e consistência precisam ser avaliados.

### HBase

HBase é apresentado como banco NoSQL distribuído, orientado a colunas e construído sobre HDFS, adequado para leitura e gravação aleatórias em grandes conjuntos de dados. Suas operações incluem criação de tabelas, `Put`, `Get`, `Scan` e `Delete`, com APIs Java, shell, Python, REST e outras.

### ZooKeeper

ZooKeeper oferece coordenação distribuída, configuração, nomeação, sincronização e serviços de grupo. Sua finalidade é ajudar componentes a descobrir e coordenar-se sem depender de conhecimento direto sobre topologia. Deve ser operado com atenção a quorum, latência, persistência e ausência de ponto único de falha.

### Cassandra

Cassandra é descrita como uma base NoSQL de família de colunas associada a conceitos de BigTable e Dynamo. O material destaca escalabilidade, disponibilidade, ausência de ponto único de falha, alto rendimento de gravação, consistência ajustável, replicação e esquema flexível. A escolha exige modelagem orientada às consultas; flexibilidade de esquema não significa consultas arbitrárias eficientes.

### Chukwa

Chukwa é apresentado como subprojeto para carregamento massivo de arquivos de texto em um cluster Hadoop, usando HDFS e MapReduce, com recursos de visualização e análise. Sua função ilustra a necessidade de ingestão e ETL, embora soluções atuais possam utilizar coletores e pipelines diferentes.

### Ambari

Ambari oferece interface e APIs para provisionar, configurar, gerenciar e monitorar clusters Hadoop. A ideia central é tratar operação do cluster como atividade automatizada, com instalação reproduzível, ciclo de vida de serviços, alertas de saúde e integração com ferramentas corporativas.

## 13. Integração com o MX

O MX não precisa executar Hadoop para aprender os princípios da aula. As ideias relevantes são: gateways, middleware, eventos, processamento em camadas, armazenamento bruto, batch, streaming, particionamento, idempotência, tolerância a falhas e escolha do banco conforme o padrão de acesso.

Arquitetura aplicada:

```text
Documento, conversa ou evento
  -> ingresso autenticado
  -> envelope com schema, origem e trace_id
  -> fila/broker
  -> worker de validação e normalização
  -> armazenamento bruto e versionado
  -> processamento paralelo de chunks e embeddings
  -> índice de recuperação
  -> resposta com evidência
  -> avaliação, telemetria e reprocessamento
```

Os conceitos de IoT também podem ser usados para operações do MX: métricas de máquina, sensores de infraestrutura, temperatura, consumo, logs e sinais de disponibilidade formam um fluxo de observabilidade. O sistema deve filtrar dados no agente ou gateway, agregar quando possível e preservar evidência suficiente para diagnóstico.

### Contratos técnicos recomendados

```text
DeviceEvent
- device_id
- event_id
- event_time
- ingestion_time
- schema_version
- metric_name
- value
- unit
- location_or_scope
- quality_flag
- signature_or_integrity_hash
```

```text
BatchJob
- job_id
- input_partition
- algorithm_version
- retry_count
- output_uri
- status
- started_at
- completed_at
- metrics
```

## 14. Exercícios autorais e gabaritos

### Exercício 1 — cálculo de ingestão IoT

Uma rede possui 20.000 sensores, cada um enviando 100 bytes a cada 5 segundos. Calcule a taxa média de dados antes de overhead, replicação e metadados.

**Gabarito orientativo:** cada sensor envia 20 bytes por segundo. A taxa média é `20.000 × 20 = 400.000 bytes/s`, aproximadamente 400 kB/s em unidade decimal. O projeto real deve acrescentar cabeçalhos, retries, timestamps, criptografia, replicação, picos e retenção.

### Exercício 2 — escolha de camada

Um dispositivo envia temperatura a cada segundo, mas a aplicação precisa detectar superaquecimento em até 200 ms mesmo quando a internet estiver indisponível. Onde deve ocorrer a primeira decisão?

**Gabarito orientativo:** no dispositivo ou gateway, com uma regra local e fallback seguro. A nuvem pode receber o evento, correlacionar histórico e recalibrar o modelo, mas não deve ser o único ponto para uma decisão de baixa latência e conectividade incerta.

### Exercício 3 — MapReduce

Projete map e reduce para obter o maior valor de temperatura por sensor e dia.

**Gabarito orientativo:** o map emite `((sensor_id, data), temperatura)`; o reduce recebe todos os valores da chave e emite o máximo. É preciso normalizar fuso, lidar com leituras inválidas, definir o que acontece com dados atrasados e registrar a versão da regra.

### Exercício 4 — HDFS ou banco de baixa latência

Um sistema precisa armazenar arquivos históricos de 2 TB e também responder leituras aleatórias em menos de 50 ms. Uma única tecnologia atende igualmente bem aos dois padrões?

**Gabarito orientativo:** não necessariamente. HDFS favorece arquivos grandes e alto débito em lote; acesso aleatório de baixa latência requer outro mecanismo, como uma base orientada a colunas ou serviço específico. Uma arquitetura híbrida pode armazenar histórico no filesystem e dados de consulta em uma camada de serving.

### Exercício 5 — middleware

Milhares de clientes consultam o mesmo sensor simples. Quais funções o middleware deve assumir?

**Gabarito orientativo:** cache com TTL, controle de taxa, autenticação, agregação, descoberta, observabilidade, normalização e resposta desacoplada. O sensor não deve atender cada cliente individualmente. O middleware deve também lidar com dados desatualizados e indicar o timestamp da última leitura.

## 15. Checklist de domínio

| Competência | Critério de domínio |
|---|---|
| IoT | Define coisa, dispositivo, gateway, atuador e ecossistema |
| Arquitetura | Separa edge, middleware, nuvem, análise e atuação |
| Escala | Calcula taxa, volume, picos e retenção |
| Hadoop | Explica Common, HDFS, YARN e MapReduce |
| HDFS | Diferencia alto débito de baixa latência e pequenos arquivos |
| MapReduce | Modela map, shuffle, reduce, partições e reexecução |
| Subprojetos | Distingue Pig, Hive, HBase, ZooKeeper, Cassandra, Chukwa e Ambari |
| MX | Aplica eventos, contratos, filas, processamento paralelo e evidências |

## 16. Limitações e cautelas

O material apresenta um ecossistema Hadoop e plataformas IoT com forte valor histórico e didático. Projetos, serviços e práticas de mercado podem ter evoluído, sido substituídos ou descontinuados. Antes de uma implementação, deve-se verificar manutenção, segurança, compatibilidade, custo e suporte atual.

A ideia de trilhões de dispositivos e os cálculos de volume são ilustrações de escala. Uma estimativa de produção deve usar telemetria real, distribuição de leituras, picos, perda de conexão, retries, compressão e políticas de retenção.

Este dossiê foi escrito originalmente a partir do PDF salvo no projeto. Ele não reproduz integralmente o material; reorganiza os conceitos e acrescenta contratos, decisões de arquitetura, exercícios e aplicações ao MX.

## Referências

[1]: `BigData Aula4.pdf`, páginas 5, 8–22, material teórico salvo no projeto Estudos.

[2]: BAHGA, A.; MADISETTI, V. *Internet of Things: A Hands-On Approach*. 2014.

[3]: CASAGRAS. *CASAGRAS Final Report: RFID and the Inclusive Model for the Internet of Things*. 2009.

[4]: ITU. *Internet of Things 2005: Executive Summary*. Disponível em: https://www.itu.int/osg/spu/publications/internetofthings/InternetofThings_summary.pdf

[5]: WHITE, Tom. *Hadoop: The Definitive Guide*. Referenciado no material como base para Hadoop, HDFS e ecossistema.

[6]: SHENOY, Aravind. *Hadoop Explained*. Packt Publishing, 2014. Disponível conforme referência do material em: https://www.packtpub.com/packt/free-ebook/hadoop-explained

[7]: SOUZA, Alberto Messias da Costa. *Uma nova arquitetura para Internet das Coisas com análise e reconhecimento de padrões e processamento com Big Data*. Tese de Doutorado, USP, 2015.

[8]: QUINTERO, D. et al. *IBM Data Engine for Hadoop and Spark*. IBM Redbooks, 2016. Disponível conforme referência do material em: http://www.redbooks.ibm.com/redbooks/pdfs/sg248359.pdf
