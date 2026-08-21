# Dossiê detalhado — Concorrência, Replicação e Design em Sistemas Distribuídos

## Identificação e rastreabilidade

| Campo | Informação |
|---|---|
| Arquivo | `ComputacaoParalelaEDistribuida6.pdf` |
| Unidade | Concorrência, Replicação e Design em Sistemas Distribuídos |
| Conteúdo | páginas 8–30; material complementar na página 31; referências na página 32 |
| Responsável pelo conteúdo | Prof. Esp. Allan Piter Pressi |
| Revisão textual | Prof.ª Dr.ª Selma Aparecida Cesarin |
| Objetivo declarado | compreender transações, replicação, desenho e arquitetura de sistemas distribuídos |

## 1. Ideia central

A unidade trata do problema de manter dados consistentes quando vários clientes executam operações simultaneamente, servidores falham e cópias do mesmo objeto vivem em máquinas diferentes. O eixo técnico é combinar **transações**, **controle de concorrência**, **recuperação**, **confirmação distribuída** e **replicação**.

Para o MX, isso significa impedir que dois agentes atualizem simultaneamente o mesmo conhecimento, workflow ou configuração de forma incompatível, garantindo que decisões confirmadas sejam duráveis e que falhas não deixem efeitos parciais.

## 2. Transações

Uma transação é uma sequência de operações tratada como uma unidade lógica. Ela transforma um estado consistente em outro estado consistente, sem expor efeitos intermediários a transações concorrentes.

| Propriedade | Aplicação |
|---|---|
| Atomicidade | tudo é confirmado ou nada produz efeito |
| Consistência | invariantes são preservadas |
| Isolamento | efeitos parciais não são observados por concorrentes |
| Durabilidade | resultado confirmado sobrevive à falha |
| Recuperabilidade | estado pode ser restaurado após reinício |

A fonte enfatiza que atomicidade envolve tanto o comportamento tudo-ou-nada quanto a sobrevivência a falhas. O reconhecimento de commit só deve ocorrer depois que os efeitos necessários foram gravados em armazenamento permanente ou em mecanismo durável equivalente.

## 3. Concorrência em objetos e threads

Servidores com múltiplos threads podem atender vários clientes simultaneamente, mas métodos que acessam estado mutável devem ser sincronizados. Sem coordenação, duas operações de leitura-modificação-escrita podem ler o mesmo valor antigo e perder uma atualização.

Operações atômicas não sofrem interferência observável de operações concorrentes. Mutexes, monitores, locks distribuídos, filas e versões condicionais são mecanismos possíveis; nenhum deles substitui a definição explícita das invariantes do domínio.

Filas compartilhadas ilustram a coordenação produtor-consumidor. Se não há item, o consumidor deve aguardar uma notificação em vez de fazer polling infinito. Em sistemas distribuídos modernos, a mesma ideia aparece em filas duráveis, streams, backpressure e leases.

## 4. Modelo de falhas e recuperação

O modelo estudado considera falhas de processo, atrasos, perda, duplicação e corrupção de mensagens, além de falhas de armazenamento. Não se assume comportamento bizantino no protocolo transacional; falhas arbitrárias exigiriam mecanismos diferentes.

O armazenamento estável pode ser obtido por replicação de blocos. Checksums detectam corrupção; logs e informações persistentes permitem reconstruir objetos após o processo ser substituído. Uma operação de escrita durável deve deixar evidência suficiente para decidir, após reinício, quais efeitos pertencem a transações confirmadas.

A arquitetura deve declarar o que acontece quando ocorre falha durante a recuperação. Recuperação não é apenas reiniciar o processo: é identificar o último estado confirmado, descartar estados incompletos e reconciliar participantes de operações distribuídas.

## 5. Serialização

Executar todas as transações uma por vez é simples, porém reduz o desempenho. O objetivo prático é permitir execução concorrente quando o resultado for equivalente ao de alguma execução serial.

Duas operações conflitam quando o resultado depende da ordem entre elas, normalmente quando pertencem a transações diferentes, acessam o mesmo objeto e pelo menos uma grava. A serialização por conflitos exige que a ordem dos pares conflitantes seja compatível com uma ordem serial global.

Problemas evitados incluem atualização perdida, leitura suja, gravação prematura e recuperação inconsistente.

## 6. Métodos de controle de concorrência

| Método | Estratégia | Vantagens | Riscos ou custos |
|---|---|---|---|
| Bloqueio estrito de duas fases | adquire locks e os mantém até commit/abort | serialização e recuperação previsíveis | deadlock e espera |
| Leitor/escritor | vários leitores ou um escritor | mais concorrência em leituras | promoção de lock e starvation |
| Timestamp | ordena por identificador temporal | evita deadlock | abortos de transações tardias |
| Otimista | executa e valida ao final | bom para conflitos raros | abortos repetidos |
| Multiversão | mantém versões temporárias/históricas | leituras e escritas mais flexíveis | armazenamento e limpeza |
| Resolução de conflitos | aceita alterações e reconcilia | adequada à colaboração | exige semântica de merge |

### Bloqueio estrito de duas fases

Bloqueios de leitura são compartilhados; bloqueios de gravação são exclusivos. Um escritor espera leitores concorrentes terminarem, e leitores ou escritores esperam um escritor liberar o objeto. No regime estrito, locks são mantidos até conclusão ou aborto, evitando leituras sujas e gravações prematuras.

O gerenciador de locks deve ser sincronizado internamente e manter objeto bloqueado, transações proprietárias, tipo de lock, fila de espera, prazo, prioridade e mecanismo de cancelamento. Em ambiente distribuído, deve incluir fencing token para impedir que um proprietário antigo escreva após perder a posse.

## 7. Controle otimista e colaboração

O controle otimista deixa transações avançarem e valida conflitos na fase de confirmação. É apropriado quando conflitos são raros ou quando abortar e tentar novamente é aceitável. Em aplicações colaborativas, rejeitar simplesmente a segunda alteração pode ser pior que registrar versões e permitir merge.

Exemplos discutidos incluem serviços de arquivos com histórico, edição colaborativa e páginas com conflito de edição. O MX pode aplicar controle otimista a documentos de conhecimento e configurações, desde que mantenha autor, versão-base, versão-resultante, diff, validação e histórico de reversão.

## 8. Transações aninhadas

Uma transação de nível superior pode conter subtransações. Subtransações podem executar em paralelo, confirmar provisoriamente ou abortar de forma independente; entretanto, o abortamento do pai deve invalidar seus descendentes.

Esse modelo é útil quando um workflow envolve etapas independentes em servidores diferentes. Uma subtransação pode falhar e ser compensada, enquanto o pai decide se a falha impede o resultado global. A arquitetura deve diferenciar **rollback técnico** de **transação compensatória**, pois efeitos externos nem sempre podem ser apagados.

## 9. Transações distribuídas

Uma transação distribuída acessa objetos em mais de um servidor. Todos os participantes devem confirmar ou todos devem abortar. Um coordenador administra a decisão e cada servidor participante protege os objetos locais.

O identificador da transação precisa ser globalmente exclusivo. Participantes devem manter status, intenções, locks e informações persistentes suficientes para recuperação. O coordenador não deve depender apenas de memória volátil ou de uma única resposta sem evidência durável.

## 10. Confirmação em duas fases — 2PC

O protocolo de duas fases é apresentado como mecanismo comum de confirmação atômica.

### Fase 1 — preparação e voto

O coordenador pergunta se cada participante pode confirmar. Cada participante valida sua parte, garante que consegue recuperar a decisão e grava em armazenamento persistente os objetos alterados e o estado `prepared`. Se qualquer participante votar abortar, a decisão global será abortar.

### Fase 2 — decisão

Se todos votarem positivamente, o coordenador registra e divulga `commit`. Se algum votar negativamente, divulga `abort`. Participantes persistem a decisão e liberam recursos somente quando a regra do protocolo permitir.

O 2PC pode bloquear indefinidamente quando o coordenador falha ou quando participantes não conseguem obter a decisão. Portanto, não deve ser apresentado como solução universal para baixa latência. Em sistemas atuais, vale comparar 2PC com sagas, outbox transacional, filas, compensações, consenso e arquiteturas de consistência eventual.

## 11. Deadlocks distribuídos

Locks em servidores diferentes podem formar um ciclo global de espera. Timeout é simples, mas pode abortar transações saudáveis ou demorar excessivamente. Detecção de deadlock constrói ou aproxima um grafo de espera; ao encontrar ciclo, escolhe uma vítima para abortar.

A seleção deve considerar custo de rollback, idade, prioridade, número de locks, impacto no cliente e número de abortos anteriores. O MX deve registrar a cadeia de espera para permitir diagnóstico, evitando mascarar o problema como simples timeout.

## 12. Logging e recuperação

O arquivo de recuperação funciona como log de transações. Pode conter snapshot recente, valores de objetos, intenções, estados `prepared`, `committed` e `aborted`. Após falha, transações sem status confirmado são abortadas ou submetidas ao procedimento de decisão apropriado.

A escrita do status de commit precisa ser durável antes do reconhecimento ao cliente. Buffers aumentam desempenho, mas devem ser descarregados no ponto de confirmação. Checkpoints reduzem o custo de reconstruir toda a história.

## 13. Replicação

Replicação mantém cópias físicas de um mesmo objeto lógico em computadores diferentes. Ela melhora desempenho, disponibilidade e tolerância a falhas, mas cria o problema de manter uma visão coerente.

Caches de navegadores, proxies e servidores Web também são formas de replicação. O DNS replica mapeamentos de nomes e atributos. Réplicas podem divergir temporariamente, especialmente durante partições ou atualizações assíncronas.

### Arquiteturas

| Arquitetura | Funcionamento | Perfil |
|---|---|---|
| Primário-backup/passiva | um gerente processa; backup assume | simples de raciocinar, failover necessário |
| Replicação ativa | todos processam todas as solicitações | exige ordem determinística e comunicação consistente |
| Front-end + gerentes | camada frontal oculta escolha da réplica | transparência e roteamento |
| Fofoca/gossip | réplicas disseminam atualizações | alta disponibilidade e convergência eventual |
| Bayou-like | atualizações locais e reconciliação posterior | útil sob partição, requer resolução de conflitos |

## 14. Consistência de réplicas

Linearizabilidade exige que cada operação pareça ocorrer instantaneamente em uma ordem compatível com o tempo real. Consistência sequencial exige uma ordem única que preserve a ordem de operações de cada cliente, mas não necessariamente o tempo físico global.

Consistência eventual permite divergência temporária e promete convergência se não surgirem novas atualizações. Ela é apropriada quando disponibilidade e operação desconectada superam a necessidade de leitura imediatamente uniforme.

A escolha deve ser feita por objeto e operação. Um contador de uso pode aceitar convergência; autorização, saldo, lock e decisão de commit exigem garantias mais fortes.

## 15. Comunicação em grupo e gerenciamento de membros

Comunicação em grupo pode disseminar atualizações para réplicas. Visões de grupo representam os membros considerados ativos em determinado momento. Comunicação de visão síncrona procura garantir que as réplicas processem mudanças de associação e mensagens em uma ordem segura.

Em falhas e partições, é necessário definir quorum de leitura, quorum de escrita, membro líder, reentrada, sincronização de réplica atrasada e prevenção de split-brain. A gestão de membros deve ser autenticada e auditável.

## 16. Aplicação ao MX

```text
comando do usuário
   -> idempotency_key
   -> transação local / outbox
   -> coordenador de workflow
   -> participantes de conhecimento, memória, auditoria e execução
   -> commit ou compensação
   -> réplica primária/secundária
   -> log, checkpoint e observabilidade
```

### Contratos essenciais

| Contrato | Campos mínimos |
|---|---|
| Transação | `tx_id`, cliente, estado, timeout, versão-base, participantes |
| Operação | `op_id`, recurso, tipo, pré-condição, efeito, idempotência |
| Lock | recurso, modo, proprietário, expiração, fencing token |
| Commit | voto, estado persistente, coordenador, decisão, evidência |
| Réplica | objeto, versão, origem, sequência, atraso, estado de saúde |
| Conflito | versão-base, alterações concorrentes, resolução, autor e timestamp |

Para executar uma tarefa em múltiplos serviços, o MX deve preferir operações idempotentes, outbox transacional, retries com backoff, deduplicação e compensação explícita. A decisão de usar 2PC deve considerar latência, disponibilidade, partições e possibilidade de manter uma transação aberta durante falhas.

## 17. Exercícios autorais e gabaritos

### Exercício 1 — atualização perdida

Dois agentes leem a mesma versão de uma instrução e gravam alterações diferentes. Como impedir sobrescrita silenciosa?

**Gabarito:** usar controle otimista com `version` ou ETag. A segunda gravação deve falhar com conflito e apresentar diff/merge; alternativa é lock exclusivo, se a colaboração simultânea não for desejada.

### Exercício 2 — 2PC

Um participante votou `prepared`, mas o coordenador caiu antes da decisão. O participante pode confirmar sozinho?

**Gabarito:** não, não sem evidência de decisão válida. Ele deve seguir o protocolo de recuperação, consultar coordenador/participantes ou permanecer preparado conforme o modelo. Esse bloqueio é uma limitação conhecida do 2PC.

### Exercício 3 — réplica atrasada

Uma leitura de autorização veio de uma réplica que ainda não recebeu a revogação. Qual política é necessária?

**Gabarito:** autorização deve usar leitura fortemente consistente, primário/quorum ou token de versão que a réplica precisa alcançar. Consistência eventual é inadequada para essa decisão crítica.

### Exercício 4 — deadlock

A transação A segura o recurso X e espera Y; B segura Y e espera X. Qual é o diagnóstico e a ação?

**Gabarito:** ciclo de espera, portanto deadlock. É necessário abortar uma vítima, liberar seus locks e permitir retry idempotente, registrando a causa.

### Exercício 5 — partição de rede

Duas réplicas aceitam atualizações durante uma partição. O sistema exige disponibilidade. Qual consequência deve ser assumida?

**Gabarito:** divergência temporária e necessidade de reconciliação. O domínio deve definir merge determinístico, resolução manual ou regra de prioridade. Se a operação não admite conflito, deve sacrificar disponibilidade ou restringir writes.

## 18. Análise crítica e atualização

O material fornece fundamentos clássicos e úteis, mas nomes e protocolos apresentados devem ser complementados por práticas contemporâneas. Em produção, é necessário separar transação de banco, workflow distribuído e garantia de entrega; não se deve presumir que um retry seja seguro sem idempotência.

A replicação não é sinônimo de consistência. Mais cópias podem aumentar disponibilidade e leitura, mas também ampliam divergência, custo de sincronização e superfície de falha. A escolha correta nasce dos invariantes do negócio.

## Referências

[1]: `ComputacaoParalelaEDistribuida6.pdf`, páginas 8–32, material salvo no projeto Estudos.

[2]: GAGLIARDI, Gary. *Cliente/servidor*. São Paulo: Makron Books do Brasil, 1996.

[3]: STALLINGS, William. *Arquitetura e organização de computadores: projeto para o desempenho*. 8. ed. Pearson Prentice Hall, 2009.

[4]: TANENBAUM, Andrew S.; STEEN, Maarten van. *Sistemas distribuídos: princípios e paradigmas*. 2. ed. Referência conforme edição indicada na fonte.
