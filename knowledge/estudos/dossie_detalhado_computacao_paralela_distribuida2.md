# Dossiê detalhado — Comunicação entre Processos

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `ComputacaoParalelaEDistribuida2.pdf` |
| Unidade | Comunicação entre Processos |
| Extensão | 35 páginas físicas; conteúdo técnico principal nas páginas 8–31 |
| Temas centrais | UDP, TCP, sockets, empacotamento, multicast, RPC, RMI e comunicação indireta |
| Tratamento | Síntese original, análise crítica, rastreabilidade de páginas e aplicação ao MX |

## 1. Tese central

A comunicação entre processos é a base operacional dos sistemas distribuídos. Processos podem trocar datagramas, fluxos, chamadas remotas, eventos, mensagens em filas ou dados compartilhados por abstrações distribuídas. Cada alternativa oferece um equilíbrio diferente entre latência, confiabilidade, acoplamento, ordenação, escalabilidade e complexidade.

A decisão correta não é simplesmente escolher “TCP porque é confiável” ou “UDP porque é rápido”. TCP fornece um fluxo confiável de bytes enquanto a conexão permanece válida, mas não prova que uma operação de negócio foi concluída. UDP reduz sobrecarga e latência, mas transfere detecção, perda, ordenação e eventual retransmissão para a aplicação quando forem necessárias.

## 2. Datagrama, fluxo e socket

A API associada ao UDP oferece uma abstração de mensagens independentes, os datagramas. A API TCP fornece um fluxo contínuo de bytes, sem preservar limites de mensagens. Essa diferença é decisiva: uma aplicação sobre TCP precisa definir seu próprio enquadramento, por exemplo, tamanho prefixado, delimitador ou protocolo estruturado.

Sockets são pontos finais para comunicação entre processos. Um processo servidor associa seu socket a endereço e porta; o cliente usa uma porta local disponível. O par de endereço e porta permite que a mensagem chegue ao processo correto dentro do host.

| Abstração | Unidade | Limite principal |
|---|---|---|
| UDP | mensagem/datagrama | pode perder, duplicar ou chegar fora de ordem |
| TCP | fluxo de bytes | não preserva mensagens e pode quebrar a conexão |
| Socket | endpoint de processo | exige configuração de endereço, porta e ciclo de vida |

## 3. Comunicação síncrona e assíncrona

Na comunicação síncrona, envio e recebimento podem bloquear até que a operação correspondente seja confirmada. Esse modelo simplifica sincronização, mas pode prender threads e reduzir capacidade quando o receptor está lento.

Na comunicação assíncrona, o envio pode retornar depois de copiar a mensagem para um buffer local, deixando a transmissão prosseguir em paralelo. O ganho potencial de utilização vem acompanhado de maior complexidade: é preciso acompanhar callbacks, futures, estados, erros, cancelamento e pressão de memória.

No MX, tarefas rápidas podem usar solicitação-resposta com timeout; tarefas demoradas devem ser aceitas, persistidas em fila e acompanhadas por status. “Assíncrono” não significa “sem garantia”: o contrato deve definir persistência, confirmação, retries, deduplicação e prazo.

## 4. UDP

UDP transmite datagramas sem confirmação embutida nem tentativas de retransmissão. O material aponta tamanho da mensagem, origem dos pacotes e omissão como problemas. É apropriado quando perda ocasional é aceitável ou quando a aplicação possui estratégia própria.

DNS e VoIP são citados como exemplos. Em comunicação de voz, esperar retransmissão de um pacote atrasado pode ser pior do que perder um pequeno trecho. Em uma telemetria, uma amostra posterior pode substituir uma amostra perdida.

### Sobrecarga evitada

A entrega garantida exige estado nos extremos, mensagens adicionais e latência para confirmação. UDP não elimina custos da rede, mas evita impor esses mecanismos a todas as aplicações. Se o MX usar UDP para um caso específico, deve adicionar autenticação, validação, limite de tamanho, proteção contra spoofing, sequência quando necessária e política contra tempestades.

## 5. TCP

TCP apresenta um fluxo de bytes e oculta tamanhos de mensagens, entrega de pacotes, controle de fluxo e destinos individuais. Usa checksum, números de sequência, timeout e retransmissão para detectar corrupção, duplicação e perda durante uma conexão.

Contudo, TCP não garante entrega diante de uma interrupção definitiva. Quando a conexão quebra, o processo não sabe necessariamente se a última mensagem foi processada pelo servidor antes da falha. Também pode ser impossível distinguir falha do processo remoto de falha de rede. Esse ponto é essencial para operações do MX: a confirmação de transporte não equivale à confirmação de negócio.

### Protocolo de enquadramento sobre TCP

```text
[version][message_type][payload_length][request_id][payload][checksum opcional]
```

Sem um enquadramento, duas mensagens podem ser lidas juntas ou uma mensagem pode chegar fragmentada em várias leituras. HTTP, FTP, Telnet e SMTP são citados como serviços sobre TCP; em sistemas atuais, Telnet não deve ser usado para administração por falta de proteção adequada.

## 6. Representação externa e empacotamento

Estruturas em memória precisam ser convertidas em bytes antes da transmissão e reconstruídas no destino. Esse processo envolve diferenças de endianness, representação de ponto flutuante e codificação de caracteres. A fonte contrasta ASCII e Unicode e define:

| Operação | Função |
|---|---|
| Marshalling | reunir dados estruturados e convertê-los em representação de mensagem |
| Unmarshalling | validar e reconstruir os dados no destino |
| Serialização | representar objetos ou estruturas para transporte/armazenamento |
| Desserialização | restaurar estrutura e estado a partir da representação |

CORBA CDR, serialização Java, XML, buffers de protocolo e JSON são discutidos como alternativas. A escolha deve considerar interoperabilidade, tamanho, velocidade, compatibilidade evolutiva, segurança e capacidade de inspeção.

| Formato | Ponto forte | Risco ou custo |
|---|---|---|
| CORBA CDR | representação tipada para invocações remotas | acoplamento ao ecossistema e complexidade histórica |
| Java serialization | integra-se a objetos Java | risco de desserialização insegura e dependência da JVM |
| XML | textual, extensível e validável por schema | mensagens maiores e processamento mais caro |
| JSON | simples e amplamente interoperável | tipagem e tamanho podem exigir convenções adicionais |
| Protocol Buffers | compacto, tipado e evolutivo | requer schema e tooling |

Para o MX, mensagens devem possuir `schema_version`, limites de tamanho, validação, campos obrigatórios, compatibilidade retroativa e rejeição segura de payloads desconhecidos. Nunca se deve desserializar objetos arbitrários de origem não confiável.

## 7. XML e esquemas

XML representa estrutura lógica por tags, diferentemente do HTML, que se concentra na apresentação. Namespaces e schemas permitem que vários aplicativos compartilhem significado e validem documentos. A legibilidade facilita diagnóstico, mas tags tornam mensagens maiores. XML continua útil em integração legada, configurações e contratos que exigem validação formal; para alta taxa de eventos, um formato binário pode ser mais eficiente.

## 8. Multicast e comunicação em grupo

Multicast envia uma mensagem de um processo para vários membros de um grupo. É útil para replicação, descoberta de serviços, propagação de eventos e melhoria de desempenho por dados replicados. Broadcast alcança todos os processos do domínio; unicast alcança um único processo.

Multicast IP pode perder mensagens e não garantir ordenação. Um roteador ou membro pode falhar, e pacotes podem chegar em ordens diferentes aos participantes. Aplicações replicadas podem exigir multicast confiável e atômico: todos os membros recebem ou nenhum recebe. Podem também exigir ordem causal, total ou por remetente.

| Garantia | Pergunta |
|---|---|
| Integridade | mensagem válida é entregue no máximo uma vez? |
| Validade | mensagem enviada eventualmente chega? |
| Atomicidade | todos recebem ou nenhum recebe? |
| Ordem | membros observam a mesma sequência? |
| Associação | entrada, saída e falha de membros são gerenciadas? |

No MX, a maioria dos eventos pode ser melhor atendida por broker pub/sub persistente do que por multicast IP direto, pois o broker oferece retenção, replay, autenticação e observabilidade.

## 9. Virtualização e redes de sobreposição

Uma rede de sobreposição é uma rede virtual de nós e links construída sobre uma rede subjacente, como IP. Ela permite oferecer serviços especializados sem alterar a arquitetura central da internet. As vantagens são novos serviços e múltiplas redes virtuais; os custos são indireção, latência e complexidade adicional.

O Skype é usado como caso histórico de overlay peer-to-peer com hosts comuns e supernós. A lição arquitetural é que uma aplicação pode criar uma topologia lógica adaptada ao seu domínio. A lição operacional é que descoberta, segurança, escolha de supernós, NAT traversal, monitoramento e falhas passam a ser responsabilidades adicionais.

## 10. RPC e RMI

RPC estende a chamada de procedimento para um processo remoto; RMI aplica ideia semelhante a objetos distribuídos. Ambos usam interfaces, empacotamento, solicitação-resposta e semânticas de chamada. A abstração reduz esforço, mas pode induzir o programador a tratar uma operação remota como local, ignorando latência, falhas e concorrência.

### Elementos de um RPC

```text
interface/schema
  -> stub do cliente
  -> marshalling
  -> transporte
  -> dispatcher do servidor
  -> unmarshalling
  -> implementação do serviço
  -> resposta ou erro remoto
```

Uma interface remota precisa declarar operações, parâmetros, resultados, exceções, versionamento e política de compatibilidade. Sem isso, mudanças podem quebrar consumidores distribuídos.

### Semânticas de chamada

A fonte destaca que implementações podem oferecer semânticas como no máximo uma vez e pelo menos uma vez. Na prática, “no máximo uma vez” pode significar perda de resultado quando há timeout; “pelo menos uma vez” pode repetir a operação. A semântica de negócio mais segura é frequentemente **pelo menos uma entrega com operação idempotente**, deduplicação por chave e consulta de estado.

RMI acrescenta identidade de objetos, referências remotas, herança e métodos. Também introduz questões de ciclo de vida, exceções remotas, garbage collection distribuído, replicação e migração. O MX deve preferir contratos de serviço explícitos e tipos de dados estáveis a referências remotas que ocultem dependências de ciclo de vida.

## 11. Comunicação indireta

Comunicação indireta usa um intermediário e desacopla remetente de receptor no espaço e no tempo. Isso facilita substituição, escalabilidade, absorção de picos e tolerância a falhas. Também torna o sistema mais difícil de observar e raciocinar de ponta a ponta.

### Comunicação em grupo

Uma operação envia a mensagem ao grupo, e os membros recebem cópias conforme garantias de confiabilidade e ordem. A associação é dinâmica: processos entram, saem ou falham. O gerenciamento de membros é parte do protocolo, não um detalhe secundário.

### Publicação-assinatura

Editores publicam eventos em um serviço; assinantes expressam interesse por tópicos, padrões ou conteúdo. É adequado a feeds financeiros, monitoramento, colaboração, infraestrutura e eventos de domínio. O broker precisa controlar autenticação, autorização, filtros, retenção, replay, ordenação, backpressure e entrega.

### Filas de mensagens

Uma fila oferece comunicação ponto a ponto: o produtor coloca a mensagem e um consumidor a remove. Ela desacopla espaço e tempo e pode manter mensagens persistentes em disco até serem consumidas. Filas centralizadas simplificam operação, mas podem formar gargalo ou ponto único de falha; implementação distribuída aumenta resiliência ao custo de complexidade.

| Propriedade | Decisão que o MX deve explicitar |
|---|---|
| Persistência | mensagem sobrevive à queda do consumidor? |
| Entrega | no máximo uma, pelo menos uma ou efetivamente uma vez? |
| Ordem | global, por chave, por partição ou nenhuma? |
| Retenção | por tempo, tamanho ou confirmação? |
| Retry | limite, atraso, backoff e dead-letter |
| Backpressure | bloquear produtor, limitar taxa ou descartar? |

### Memória compartilhada distribuída

DSM oferece aos processos a aparência de uma memória comum, embora a memória física esteja distribuída. O runtime transmite atualizações entre computadores. A abstração reduz a programação explícita de mensagens e é especialmente útil em computação paralela, mas não elimina comunicação nem resolve automaticamente consistência, conflitos e particionamento.

DSM é menos natural para aplicações cliente-servidor que precisam encapsular recursos e proteger estado. No MX, estado compartilhado crítico deve possuir modelo de consistência, dono lógico, versionamento e política de conflito.

## 12. MPI e paralelismo

A introdução da unidade menciona MPI como padrão de API para passagem de mensagens, com variantes síncronas e assíncronas. MPI é especialmente relevante para processos de computação paralela em clusters, onde o programa controla comunicação entre ranks. É diferente de uma fila de tarefas: MPI costuma modelar participantes cooperantes de uma mesma computação, enquanto filas modelam produtores e consumidores desacoplados.

## 13. Aplicação ao MX

### Escolha de canal

| Necessidade do MX | Canal inicial |
|---|---|
| Resposta imediata e contrato de consulta | HTTP/gRPC sobre transporte confiável |
| Tarefa longa de ingestão ou indexação | fila persistente |
| Notificação a vários consumidores | pub/sub com retenção |
| Estado de processamento paralelo | partições, checkpoints e armazenamento versionado |
| Telemetria de baixa latência | UDP ou agente especializado, com autenticação e amostragem |
| Comunicação cooperante de alto desempenho | MPI ou runtime paralelo, quando necessário |

### Evento versionado

```text
KnowledgeEvent
- event_id
- event_type
- schema_version
- aggregate_id
- producer
- occurred_at
- trace_id
- payload
- checksum
```

O consumidor deve registrar `event_id`, validar schema e manter checkpoint. Um reprocessamento deve ser seguro e observável. Eventos contendo documentos ou prompts precisam obedecer às permissões e não expor segredos.

## 14. Exercícios autorais e gabaritos

### Exercício 1 — TCP não confirma negócio

O MX envia uma solicitação de gravação, a conexão cai e o cliente não recebe resposta. O que deve fazer?

**Gabarito orientativo:** não repetir cegamente. Consultar o status por `request_id` ou usar idempotency key; se não houver estado, repetir somente uma operação projetada para deduplicação. Registrar que o resultado é indeterminado.

### Exercício 2 — fluxo TCP

Duas mensagens JSON são enviadas em uma conexão TCP. Uma leitura recebe parte da primeira e parte da segunda. Isso é erro do TCP?

**Gabarito orientativo:** não. TCP entrega fluxo de bytes, não mensagens. A aplicação precisa implementar framing e leitura até completar o tamanho ou delimitador.

### Exercício 3 — pub/sub ou fila

Um evento de documento deve ser processado por indexador, auditoria e métrica. Qual mecanismo é mais adequado?

**Gabarito orientativo:** pub/sub, com um consumidor independente por finalidade. Uma fila única faria apenas um consumidor receber a mensagem, salvo configuração específica de grupos de consumidores.

### Exercício 4 — multicast confiável

Três réplicas precisam aplicar comandos na mesma ordem. Multicast IP básico é suficiente?

**Gabarito orientativo:** não. É necessário protocolo de ordenação, confirmação e associação de grupo, ou um sistema de consenso/log replicado apropriado. O mecanismo deve tratar membro lento e falha.

### Exercício 5 — serialização segura

Um serviço recebe objeto serializado de origem externa e o desserializa automaticamente. Qual é o problema?

**Gabarito orientativo:** desserialização de objetos pode permitir execução ou construção de estados perigosos. Preferir schema explícito, tipos permitidos, validação de tamanho e conteúdo, parser seguro e versão controlada.

## 15. Checklist de domínio

| Competência | Evidência |
|---|---|
| UDP/TCP | explica datagrama, fluxo, perda, framing, timeout e retransmissão |
| Sockets | relaciona processo, endereço, porta e endpoint |
| Empacotamento | diferencia marshalling, schema, encoding e compatibilidade |
| RPC/RMI | descreve interface, stub, dispatcher e semântica de chamada |
| Multicast | distingue entrega, ordem, atomicidade e associação |
| Comunicação indireta | compara grupo, pub/sub, fila e DSM |
| Mensageria | define retenção, retry, DLQ, backpressure e checkpoint |
| MX | escolhe canal por latência, confiabilidade, acoplamento e escala |

## 16. Limitações e atualização

O material usa tecnologias e exemplos históricos, incluindo CORBA, Java RMI, Telnet, WebSphere MQ e Skype em sua forma de referência. A validade pedagógica permanece, mas a seleção para um projeto atual deve considerar gRPC, HTTP moderno, schemas evolutivos, brokers distribuídos, TLS, observabilidade e políticas de segurança atuais.

Este dossiê foi elaborado em redação original a partir do PDF salvo. Não reproduz integralmente o material protegido; reorganiza conceitos, explicita trade-offs e cria contratos, exercícios e aplicações próprias ao MX.

## Referências

[1]: `ComputacaoParalelaEDistribuida2.pdf`, páginas 8–31, material salvo no projeto Estudos.

[2]: GAGLIARDI, Gary. *Cliente/servidor*. São Paulo: Makron Books do Brasil, 1996.

[3]: STALLINGS, William. *Arquitetura e organização de computadores: projeto para o desempenho*. 8. ed. Pearson, 2009.

[4]: TANENBAUM, Andrew S.; STEEN, Maarten van. *Sistemas distribuídos: princípios e paradigmas*. 2. ed. Pearson, 2007.
