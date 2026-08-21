# Inventário dos materiais efetivamente salvos

## Escopo

O inventário abaixo considera somente arquivos presentes no diretório compartilhado do projeto. A existência de um arquivo confirma que ele pode ser analisado dentro do projeto, mas não altera automaticamente os direitos de publicação ou redistribuição do conteúdo.

## PDFs acadêmicos salvos

| Coleção | Arquivos | Tratamento planejado |
|---|---:|---|
| Big Data | 5 PDFs, de `BigData Aula1.pdf` a `BigData Aula5.pdf` | Dossiê detalhado por aula, com conceitos, arquitetura, processamento, exercícios autorais e aplicação ao MX. |
| Computação Paralela e Distribuída | 6 PDFs, de `ComputacaoParalelaEDistribuida1.pdf` a `ComputacaoParalelaEDistribuida6.pdf` | Dossiê por aula, incluindo modelos de paralelismo, distribuição, comunicação, sincronização e análise de desempenho. |
| Qualidade de Software | 4 PDFs, de `Qualidade de Software1.pdf` a `Qualidade de Software4.pdf` | Dossiê por aula, com processos, métricas, testes, garantia da qualidade e integração aos gates de release. |

Ao todo, estão salvos **15 PDFs acadêmicos** nessas três coleções.

## Documentos técnicos e biblioteca própria

Estão salvos 28 módulos derivados do estudo da Alura, documentação de integração do MX, currículo priorizado, mapa de cursos, pacote mestre de aprendizado, taxonomia, manifesto JSONL, exercícios autorais, skills reutilizáveis, bootstrap de integração e modelos de avaliação. Esses arquivos formam a camada de documentação original e não devem ser confundidos com cópias de cursos ou livros.

## Livros

Não foi localizado no diretório um conjunto de arquivos integrais identificados como livros da Alura. Títulos encontrados em buscas permanecem como metadados ou referências até que o arquivo autorizado seja efetivamente salvo no projeto. Quando um livro próprio ou autorizado for disponibilizado, ele será catalogado por título, autor, edição, capítulos, conceitos, referências, exercícios originais e aplicação.

## Primeiro lote recomendado

O primeiro lote detalhado utiliza os 15 PDFs efetivamente salvos, divididos em três frentes independentes. A frente de Big Data foi concluída nesta etapa; Computação Paralela e Distribuída será tratada como desempenho e escalabilidade; Qualidade de Software como engenharia de qualidade. Depois serão expandidos os 28 módulos derivados da Alura, começando por AI Engineer e pelos componentes mais diretamente ligados ao MX.

## Regra de rastreabilidade

Cada dossiê terá `source_file`, hash do arquivo, páginas ou seções examinadas, data de análise, versão do dossiê, limitações e referências. Se a extração de um PDF for incompleta, isso será declarado e o dossiê não preencherá lacunas com suposições.

## Dossiês detalhados produzidos

| Material | Dossiê | Conteúdo coberto |
|---|---|---|
| BigData Aula1.pdf | `dossie_detalhado_bigdata_aula1.md` | Conceito operacional de Big Data, volume, variedade, velocidade, data warehouse, processamento paralelo/distribuído, streaming, capacidade e aplicação ao MX |
| BigData Aula2.pdf | `dossie_detalhado_bigdata_aula2.md` | Veracidade, valor, Vs complementares, fontes de dados, governança, casos de uso, streaming, experimentos de valor e aplicação ao MX |
| BigData Aula3.pdf | `dossie_detalhado_bigdata_aula3.md` | Computação em nuvem, cinco características essenciais, benefícios e limites, TCO, continuidade, IaaS/PaaS/SaaS, modelos de implantação, segurança e arquitetura de nuvem para o MX |
| BigData Aula4.pdf | `dossie_detalhado_bigdata_aula4.md` | IoT, dispositivos, gateways, middleware, arquitetura IoT–Big Data, escala de eventos, Hadoop, HDFS, MapReduce, YARN, Pig, Hive, HBase, ZooKeeper, Cassandra, Chukwa e Ambari |
| BigData Aula5.pdf | `dossie_detalhado_bigdata_aula5.md` | Mahout, Spark, RDD, SQL, Streaming, MLlib, GraphX, instalação didática single-node, configuração, HDFS, operação, segurança e Word Count |
| ComputacaoParalelaEDistribuida1.pdf | `dossie_detalhado_computacao_paralela_distribuida1.md` | Fundamentos de sistemas distribuídos, concorrência, falhas, modelos físicos/arquiteturais/fundamentais, middleware, RPC, pub/sub, filas, camadas, segurança, redes, TCP/IP, IPv6, TCP/UDP, DNS, roteamento, congestionamento e aplicação ao MX |
| ComputacaoParalelaEDistribuida2.pdf | `dossie_detalhado_computacao_paralela_distribuida2.md` | Comunicação entre processos, sockets, comunicação síncrona e assíncrona, UDP, TCP, framing, marshalling, XML, JSON, Protocol Buffers, multicast, overlays, RPC, RMI, pub/sub, filas, DSM, MPI e aplicação ao MX |
| ComputacaoParalelaEDistribuida3.pdf | `dossie_detalhado_computacao_paralela_distribuida3.md` | Suporte do sistema operacional ao middleware, kernel, proteção, processos, threads, espaços de endereço, IPC, desempenho, kernels monolíticos e microkernel, virtualização, objetos e componentes distribuídos, CORBA/RMI, Web Services, SOAP, WSDL, UDDI, XML Security, SOA, Grid, nuvem e aplicação ao MX |
| ComputacaoParalelaEDistribuida4.pdf | `dossie_detalhado_computacao_paralela_distribuida4.md` | Peer-to-peer, Napster e gerações P2P, GUIDs, hashes, sobreposição de roteamento, replicação, ameaças, criptografia simétrica e assimétrica, TLS, certificados, controle de acesso, firewalls, assinaturas digitais, X.509, sistemas de arquivos distribuídos, cache, consistência, NFS e aplicação ao MX |
| ComputacaoParalelaEDistribuida5.pdf | `dossie_detalhado_computacao_paralela_distribuida5.md` | Serviços de nomes, namespaces, DNS, resolução, diretórios, cache, replicação, tempo físico, NTP, relógios lógicos e vetoriais, estados globais, depuração, exclusão mútua, multicast, detectores de falha, consenso, Generais Bizantinos e aplicação ao MX |
| ComputacaoParalelaEDistribuida6.pdf | `dossie_detalhado_computacao_paralela_distribuida6.md` | Transações, propriedades ACID, atomicidade, serialização, bloqueio estrito de duas fases, leitores/escritores, timestamp, controle otimista, multiversão, transações aninhadas e distribuídas, 2PC, deadlocks, logging, recuperação, replicação, consistência, backup primário, replicação ativa, gossip, Bayou e aplicação ao MX |
| Qualidade de Software1.pdf | `dossie_detalhado_qualidade_software1.md` | Motivação da qualidade, qualidade de projeto e conformidade, modelo de McCall, funcionalidade, confiabilidade, eficiência, integridade, usabilidade, manutenibilidade, portabilidade, avaliação, CMM, ISO/SPICE, ISO/IEC 12207, 9000-3, 9126, 12119, 14598, erro/falha/defeito, garantia da qualidade, métricas e aplicação ao MX |
| Qualidade de Software2.pdf | `dossie_detalhado_qualidade_software2.md` | CMM, processo e maturidade, níveis inicial/repetível/definido/gerenciado/otimizado, áreas-chave de processo, interpretação crítica, métricas, SQA, PSP, custo/prazo/qualidade e aplicação ao MX |
| Qualidade de Software3.pdf | `dossie_detalhado_qualidade_software3.md` | CMMI-DEV, CMMI-ACQ, CMMI-SVC, representação contínua, representação por estágios, níveis de capacidade/maturidade, áreas de processo, verificação, validação, riscos, medição e análise causal aplicadas ao MX |
| Qualidade de Software4.pdf | `dossie_detalhado_qualidade_software4.md` | Organismos normativos, ISO/IEC, série ISO 9000, ISO 9001 e PDCA, ISO 9000-3, ISO/IEC 12207, processos fundamentais/de apoio/organizacionais, ISO/IEC 15504-SPICE, capacidade, auditoria, certificação e aplicação ao MX |

### Padrão adotado nos dossiês

Cada dossiê detalhado contém identificação da fonte, objetivos, conceitos reorganizados em redação original, tabelas de comparação, análise crítica, integração arquitetural com o MX, contratos técnicos ou métricas quando aplicável, limitações da fonte, exercícios autorais com gabaritos e referências. O conteúdo dos materiais protegidos não é reproduzido integralmente; a biblioteca preserva o conhecimento por meio de documentação original e rastreável.
