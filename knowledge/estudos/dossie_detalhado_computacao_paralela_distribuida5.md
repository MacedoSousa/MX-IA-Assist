# Dossiê detalhado — Serviços de Nome, Tempo Global, Coordenação e Suporte

## Identificação e rastreabilidade

| Campo | Informação |
|---|---|
| Arquivo | `ComputacaoParalelaEDistribuida5.pdf` |
| Unidade | Serviços de Nome, Tempo Global, Coordenação e Suporte de Sistemas Distribuídos |
| Conteúdo principal | páginas 8–29; material complementar na página 30; referências na página 31 |
| Responsável pelo conteúdo | Prof. Esp. Allan Piter Pressi |
| Revisão textual | Prof.ª Dr.ª Selma Aparecida Cesarin |
| Objetivo declarado | compreender DNS, sincronização, uso de relógios e coordenação/comunicação de processos |

## 1. Tese central

A unidade mostra que sistemas distribuídos não podem depender de um endereço físico direto, de um relógio global perfeito ou de uma memória compartilhada central. Por isso, precisam de serviços de nomes, mecanismos de ordenação temporal, coleta de estados, coordenação e acordo entre processos.

Esses assuntos são diretamente aplicáveis ao MX. Um assistente local precisa localizar módulos e serviços por nomes estáveis, correlacionar eventos sem confundir timestamp com causalidade, coordenar tarefas concorrentes, detectar suspeitas sem tratá-las como prova de falha e registrar decisões de modo auditável.

## 2. Serviços de nomes

Um serviço de nomes associa um nome a atributos de uma entidade, permitindo acessar computadores, serviços, objetos, arquivos ou usuários sem conhecer seu endereço de rede. Resolver um nome significa traduzi-lo para atributos utilizáveis em uma operação.

| Conceito | Definição operacional |
|---|---|
| Nome | referência legível ou simbólica a um recurso |
| Identificador | valor interpretado principalmente por programas |
| Endereço | atributo que permite alcançar um recurso, mas também pode exigir resolução |
| Ligação | associação entre nome e objeto/atributos |
| Namespace | conjunto de nomes válidos em um serviço |
| Domínio de nomeação | parte do namespace sob uma autoridade administrativa |
| Resolução | processo de obter atributos a partir do nome |
| Alias | nome alternativo associado ao recurso |

A separação entre nomes e endereços oferece localização transparente. Se o serviço mudar de máquina, o nome lógico pode permanecer, desde que a ligação seja atualizada.

## 3. Namespaces hierárquicos e relativos

Namespaces podem ser planos ou hierárquicos. Um caminho como `/etc/passwd` resolve componentes em contextos sucessivos. URLs também podem conter nomes relativos, cuja interpretação depende do recurso que os incorpora. DNS utiliza uma hierarquia global, com nomes referenciados a partir da raiz.

A hierarquia favorece delegação: cada domínio pode ter autoridade para atribuir e atualizar sua parte. A organização reduz conflitos e evita um banco de nomes único, que seria gargalo e ponto crítico de falha.

## 4. DNS

O DNS é um serviço de nomes global que mapeia nomes de domínio para atributos de hosts e serviços. Uma consulta pode retornar endereço IP, tipo de registro e período de validade. Aplicações como HTTP, FTP e SMTP resolvem nomes antes de estabelecer comunicação.

O DNS substituiu o modelo antigo de arquivo mestre centralizado. Sua escalabilidade depende de particionamento administrativo, servidores autoritativos, replicação, cache e diferentes formas de navegação.

### Navegação

| Estratégia | Funcionamento | Efeito |
|---|---|---|
| Iterativa | cada servidor indica o próximo contexto | cliente participa da navegação |
| Recursiva | servidor consultado resolve em nome do cliente | simplifica o cliente, aumenta responsabilidade do servidor |
| Multicast | consulta pode ser disseminada a múltiplos servidores | útil em contextos específicos |
| Não recursiva controlada | servidor retorna o que conhece ou referência | reduz trabalho local |

A unidade também cita implementações BIND com servidores primários, secundários e apenas de cache. A distinção entre autoridade e cache é essencial: cache acelera, mas pode conservar informação antiga dentro do TTL.

## 5. Serviços de diretório

Serviços de nomes normalmente respondem à pergunta “quais atributos pertencem a este nome?”. Serviços de diretório realizam o inverso: recebem atributos ou critérios e retornam objetos compatíveis.

Active Directory, X.500 e LDAP são exemplos. Essa abordagem é útil para localizar um serviço por capacidade, região, versão, papel ou política, mesmo quando o solicitante não conhece o identificador exato.

No MX, um diretório pode responder: “quais módulos possuem capacidade de leitura de PDF, rodam localmente, estão na versão compatível e têm permissão para acessar este repositório?”.

## 6. Requisitos de um serviço de nomes

Um serviço de nomes de produção deve suportar grande quantidade de nomes, longa vida útil, alta disponibilidade, isolamento de falhas e tolerância a desconfiança. Replicação e cache melhoram disponibilidade e latência, mas introduzem problemas de atualização e consistência.

O desenho deve explicitar:

1. sintaxe e hierarquia do namespace;
2. autoridade de cada domínio;
3. operação de criação, consulta, alteração e remoção;
4. política de cache e TTL;
5. propagação de atualizações;
6. comportamento durante falhas e partições;
7. autenticação de operadores e clientes;
8. auditoria e versionamento das ligações.

## 7. Tempo físico e tempo lógico

Cada computador tem relógio local, mas relógios apresentam desvio e não podem ser perfeitamente sincronizados. Um timestamp físico é útil para auditoria e correlação aproximada, porém não prova sozinho a ordem causal de eventos distribuídos.

O NTP distribui informação temporal e busca sincronização aproximada com UTC, filtrando estatisticamente amostras e considerando a qualidade dos servidores. A precisão obtida atende a muitos usos operacionais, mas não resolve toda questão de ordenação distribuída.

### Regra importante

> **Tempo de parede mede quando um evento parece ter ocorrido; causalidade descreve o que pôde influenciar o quê.**

Para o MX, devem ser registrados pelo menos `wall_time`, `monotonic_time`, `node_id`, `process_id`, `trace_id`, `sequence` e, quando necessário, relógio lógico ou vetor.

## 8. Relógios lógicos

Relógios lógicos representam a relação “aconteceu antes” entre eventos. Um evento local incrementa o contador; o envio inclui o valor; o receptor atualiza seu contador usando o maior valor observado e incrementa. Assim, a comunicação preserva a relação causal.

Relógios vetoriais ampliam essa ideia ao manter um componente por processo ou domínio de observação. Comparando vetores, é possível identificar se um evento precede outro ou se os eventos são concorrentes.

| Instrumento | Melhor uso | Limitação |
|---|---|---|
| Timestamp físico | auditoria e janela operacional | sujeito a drift e ajuste |
| Monotônico local | duração e timeout | não compara diretamente nós |
| Lamport | ordenação causal parcial | não identifica toda concorrência |
| Vetorial | causalidade e concorrência | cresce com o número de participantes |
| Trace distribuído | investigação ponta a ponta | exige propagação de contexto |

## 9. Estados globais

Um estado global é a combinação dos estados dos processos e dos canais de comunicação. Determiná-lo é difícil porque não há instante global perfeito e mensagens podem estar em trânsito.

Um estado global consistente corresponde a um corte consistente: se o corte inclui o recebimento de uma mensagem, deve incluir também o envio correspondente. Essa noção é útil para coleta de lixo distribuída, detecção de deadlock, detecção de terminação e depuração.

Um predicado global mapeia a coleção de estados para verdadeiro ou falso. Exemplos: “existe deadlock?”, “todos os workers terminaram?”, “há referência ativa ao objeto?”, “alguma regra de segurança foi violada?”.

## 10. Depuração e observabilidade distribuída

Um monitor pode receber o estado inicial de cada processo e atualizações posteriores, armazenando filas por origem. Para reduzir tráfego, cada processo envia somente variáveis relevantes e somente quando uma mudança puder alterar o predicado observado.

No MX, isso se traduz em eventos estruturados, correlação de traces, estados de workflow, mensagens em trânsito e snapshots versionados. Um snapshot não deve ser apresentado como prova de uma simultaneidade física que o sistema não consegue observar.

## 11. Coordenação e pressupostos de falha

Coordenação é o acordo entre processos sobre ações ou valores compartilhados. O modelo síncrono assume limites para entrega, execução e desvio de relógio. O assíncrono não oferece esses limites: um processo lento pode ser indistinguível de um processo falho.

Um canal confiável entrega mensagens apesar de falhas subjacentes, por exemplo usando retransmissão. Um processo correto é aquele que não falha durante toda a execução considerada. Um detector de falhas não confiável pode classificar um processo como suspeito ou não suspeito, e diferentes observadores podem receber opiniões diferentes.

O MX deve tratar `suspect` como hipótese operacional, nunca como fato absoluto. Antes de remover um worker, deve considerar heartbeat, latência, partição, carga, pausa de runtime e evidências independentes.

## 12. Exclusão mútua distribuída

Processos que compartilham recurso precisam evitar interferência. Sem memória compartilhada ou um kernel único, a exclusão mútua precisa ser implementada por troca de mensagens, leases, consenso, filas distribuídas ou um coordenador tolerante a falhas.

Uma implementação deve definir proprietário do lock, expiração, renovação, fencing token, comportamento após perda de conexão, idempotência e recuperação. Um lock sem fencing pode permitir que um processo antigo continue escrevendo depois de perder a posse lógica.

## 13. Comunicação em grupo e multicast

Multicast entrega uma mensagem a múltiplos destinatários, duplicando pacotes somente quando a topologia se divide. Unicast é ponto a ponto; broadcast alcança todos os pontos de uma rede.

Multicast IP é eficiente em certos ambientes, mas enfrenta custos de roteamento, escala, gestão de grupos e segurança. O material observa que sua adoção em escala comercial ampla é limitada, enquanto usos locais, redes privadas e cenários específicos permanecem relevantes.

Garantias úteis para comunicação de grupo incluem validade, integridade, concordância e ordenação. Para comandos do MX, um multicast de evento deve especificar se todos os membros precisam receber, se a ordem é total ou causal e como ocorre a recuperação de um membro atrasado.

## 14. Consenso e falhas bizantinas

Consenso exige que processos corretos concordem sobre um valor, apesar de falhas. Em falhas de colisão, um processo para ou perde comunicação. Em falhas bizantinas, ele pode enviar valores diferentes ou falsos a participantes distintos.

O problema dos Generais Bizantinos ilustra a necessidade de concordância e integridade quando participantes podem agir arbitrariamente. Assinaturas digitais limitam a capacidade de um participante mentir sobre mensagens assinadas, mas não resolvem automaticamente disponibilidade, chave comprometida, conluio ou ataques de negação.

Em sistemas síncronos, timeouts ajudam a interpretar ausência de mensagem. Em sistemas assíncronos, um processo lento é indistinguível de um processo falho; não existe detector perfeito baseado apenas em passagem de mensagens. Técnicas práticas incluem persistência, reinício, mascaramento, detectores suspeitos e políticas de exclusão temporária.

## 15. Aplicação arquitetural ao MX

```text
cliente/agente
   -> serviço de nomes e diretório
   -> resolução de módulo/worker
   -> trace_id + relógio lógico
   -> fila ou multicast de evento
   -> coordenação/lease/fencing
   -> execução idempotente
   -> snapshot e auditoria
```

### Contratos recomendados

| Componente | Contrato |
|---|---|
| Registry | nome lógico, versão, capacidade, endpoint, TTL, saúde e autoridade |
| Evento | id, origem, timestamp, causalidade, tipo, payload e assinatura opcional |
| Lease | proprietário, expiração, renovação, fencing token e revogação |
| Worker | heartbeat, estado, carga, capacidades e último evento processado |
| Snapshot | corte, processos incluídos, mensagens consideradas e versão |
| Consenso | proposta, participantes, quorum, decisão e evidências |

## 16. Exercícios autorais e gabaritos

### Exercício 1 — DNS e cache

Um módulo mudou de endereço, mas clientes continuam usando o endereço antigo. Explique a causa mais provável e duas ações.

**Gabarito:** o cache ainda está dentro do TTL ou houve atraso de propagação. Ações: aguardar expiração conforme política, invalidar caches sob controle, atualizar autoridade e usar versionamento/health check para evitar dependência cega do endereço.

### Exercício 2 — timestamp versus causalidade

Dois eventos possuem timestamps diferentes. É correto concluir que o evento com menor timestamp causou o outro?

**Gabarito:** não. Relógios podem estar desalinhados e atrasos podem inverter a aparência temporal. Use relação de mensagens, relógio lógico/vetorial e trace distribuído.

### Exercício 3 — processo lento

Um worker não respondeu dentro do timeout. Deve ser removido imediatamente?

**Gabarito:** não. O detector fornece suspeita, não prova. Deve-se considerar retries, quorum, heartbeat, carga, partição, fencing e idempotência. A remoção pode ser temporária e auditada.

### Exercício 4 — snapshot consistente

O snapshot registrou o recebimento de uma mensagem, mas não o envio. O que isso indica?

**Gabarito:** o corte é inconsistente, pois o recebimento não pode aparecer sem o envio correspondente. O coletor deve incluir a causalidade dos canais.

### Exercício 5 — consenso do MX

Dois agentes decidem políticas incompatíveis para o mesmo recurso. Que mecanismo deve ser introduzido?

**Gabarito:** uma autoridade de coordenação com proposta, quorum/consenso, versão monotônica, fencing e log de decisão. A escolha exata depende do modelo de falha e do requisito de disponibilidade.

## 17. Limitações e atualização crítica

A unidade oferece fundamentos sólidos, mas alguns exemplos de DNS, NTP, multicast e referências de implementação devem ser atualizados antes de uso produtivo. Em sistemas atuais, é recomendável combinar DNS com service discovery, health checks, mTLS, observabilidade, consenso comprovado, filas duráveis, relógios monotônicos, tracing distribuído e políticas de segurança.

Consenso e tolerância bizantina não devem ser adicionados apenas por prestígio técnico. Eles aumentam comunicação, latência, complexidade operacional e exigências de gestão de identidade. A arquitetura deve começar pelo modelo de falha e pelo requisito de consistência.

## Referências

[1]: `ComputacaoParalelaEDistribuida5.pdf`, páginas 8–31, material salvo no projeto Estudos.

[2]: GAGLIARDI, Gary. *Cliente/servidor*. São Paulo: Makron Books do Brasil, 1996.

[3]: LAMPORT, L.; SHOSTAK, R.; PEASE, M. “The Byzantine generals problem”. *ACM Transactions on Programming Languages and Systems*, v. 4, p. 382–401, 1982.

[4]: MILLS, D. *Simple Network Time Protocol*. RFC 1769. Disponível na referência indicada pela fonte.

[5]: STALLINGS, William. *Arquitetura e organização de computadores: projeto para o desempenho*. 8. ed. Pearson, 2009.

[6]: TANENBAUM, Andrew S.; STEEN, Maarten van. *Sistemas distribuídos: princípios e paradigmas*. 2. ed. Pearson, 2007.
