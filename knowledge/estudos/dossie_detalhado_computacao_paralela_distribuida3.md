# Dossiê detalhado — Sistemas Operacionais, Componentes e Serviços

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `ComputacaoParalelaEDistribuida3.pdf` |
| Unidade | Sistemas Operacionais, Componentes e Serviços |
| Extensão | 35 páginas físicas; conteúdo técnico nas páginas 8–31 |
| Temas centrais | Kernel, processos, threads, memória, proteção, middleware, virtualização, objetos, componentes e Web Services |
| Tratamento | Síntese original, análise crítica, rastreabilidade e aplicação ao MX |

## 1. Tese central

O sistema operacional fornece as abstrações e mecanismos sobre os quais o middleware implementa serviços distribuídos. Ele encapsula processadores, memória, armazenamento e rede; protege recursos; agenda execução; oferece comunicação; e cria os domínios de execução nos quais processos e threads operam.

A aula mostra que **mecanismo** e **política** devem ser distinguidos. O kernel fornece mecanismos básicos, enquanto políticas de escalonamento, alocação, prioridade, isolamento e recuperação podem ser implementadas em níveis apropriados. Essa separação favorece evolução, mas exige contratos claros entre kernel, runtime, middleware e aplicação.

## 2. Sistemas operacionais de rede e distribuídos

Um sistema operacional de rede, como UNIX/Linux, macOS ou Windows, mantém autonomia por nó e fornece acesso a recursos remotos por mecanismos de rede. Cada máquina mantém sua própria imagem de sistema e administra seus próprios processos.

Um sistema operacional distribuído pretende oferecer uma imagem única, controlando todos os nós e escolhendo de forma transparente onde processos e recursos serão executados. O material observa que sistemas operacionais distribuídos de uso geral enfrentam barreiras de compatibilidade, investimento em software existente e preferência dos usuários por autonomia local.

A combinação prática de **sistema operacional de rede + middleware** equilibra autonomia e compartilhamento. Essa combinação é análoga ao MX: cada processo ou servidor conserva seu ambiente, enquanto APIs, filas, descoberta, autenticação e observabilidade integram os componentes.

## 3. Camada do sistema operacional

| Componente | Responsabilidade |
|---|---|
| Gerente de processos | criação, término e operações sobre processos |
| Gerenciador de threads | criação, sincronização e escalonamento |
| Gestor de comunicação | comunicação entre threads e processos locais |
| Gerenciador de memória | memória física, virtual e compartilhada |
| Supervisor | interrupções, traps, exceções, caches e registros |
| Kernel | mecanismos privilegiados, proteção e interface com hardware |
| Bibliotecas/runtime | abstrações utilizáveis por middleware e aplicações |

O middleware usa esses recursos para implementar invocação remota, eventos, filas e objetos distribuídos. O desenho deve considerar custo da chamada de sistema, cópia de dados, troca de contexto, bloqueio, alocação e sincronização.

## 4. Proteção, kernel e chamadas de sistema

O kernel permanece carregado desde a inicialização e executa com privilégios que permitem controlar hardware e memória. Processos de usuário executam em modo menos privilegiado. Uma chamada de sistema, trap ou interrupção transfere o controle para o kernel, que valida a solicitação e retorna ao processo.

Espaços de endereço separam regiões de memória e associam permissões de leitura, escrita e execução. A proteção não serve apenas contra código malicioso; também impede que bugs de um processo corrompam outros processos ou recursos do sistema.

Aplicando ao MX, o isolamento deve existir em várias camadas:

```text
hardware e kernel
  -> usuário/processo/container/VM
  -> runtime e dependências
  -> serviço com identidade própria
  -> recurso autorizado por escopo
```

A autorização da aplicação não substitui o isolamento do sistema operacional. Da mesma forma, container não deve ser considerado equivalente automático a uma fronteira de segurança forte em qualquer cenário.

## 5. Processos, threads e espaços de endereço

Um processo é um ambiente de execução com um ou mais encadeamentos. O ambiente inclui espaço de endereço, recursos de comunicação, sincronização e outros recursos. A thread é a abstração de uma atividade que executa dentro desse ambiente.

Threads no mesmo processo compartilham memória e recursos, o que reduz custo de criação e facilita comunicação. Essa vantagem cria riscos de corrida, deadlock, starvation, corrupção de estado e efeitos de memória compartilhada. Processos separados isolam melhor, mas comunicam-se com custo maior.

| Critério | Threads | Processos |
|---|---|---|
| Memória | compartilhada por padrão | isolada por padrão |
| Criação/troca | geralmente mais barata | geralmente mais cara |
| Comunicação | variáveis, locks, filas locais | IPC, sockets, memória compartilhada |
| Falha | pode comprometer todo o processo | pode ser contida no processo |
| Paralelismo | possível em múltiplos núcleos | possível em múltiplos núcleos |
| Uso no MX | pool de requisições, I/O e tarefas curtas | serviços, workers e isolamento de plugins |

Mais threads não significam necessariamente mais desempenho. O limite pode ser CPU, I/O, locks, banco, memória ou dependência remota. O pool deve ter tamanho controlado e métricas de fila, utilização e latência.

### Espaço de endereço

O espaço de endereço é dividido em regiões não sobrepostas, com extensão, permissões e possibilidade de crescimento. Regiões compartilhadas podem suportar bibliotecas, comunicação e dados comuns, mantendo não compartilhadas protegidas.

A criação de processos distribuídos separa a escolha do host da criação do ambiente de execução. Essa separação é diretamente relacionada a schedulers, orquestradores e sistemas de jobs.

## 6. Comunicação e desempenho de invocação

A comunicação pode ser uma chamada remota, notificação de evento ou operação em recurso localizado em espaço de endereço diferente. O sistema operacional deve oferecer primitivas, protocolos, eficiência e suporte a latência, desconexão e reconexão.

Sockets são frequentemente usados pelo middleware por portabilidade e interoperabilidade. A maioria dos sistemas fornece APIs semelhantes para TCP e UDP, permitindo que o middleware funcione em Linux, Windows e outros ambientes.

Custos de invocação incluem latência fixa, troca de contexto, cópia/serialização, transmissão, processamento no destino e espera. O custo aumenta com o tamanho de argumentos e resultados. Para reduzir impacto de latência, aplicações podem fazer invocações simultâneas ou assíncronas.

No MX, uma consulta que exige várias fontes pode executar recuperações independentes em paralelo, mas deve impor deadline global, limite de concorrência, cancelamento e tratamento parcial de falhas. Paralelismo sem orçamento pode gerar tempestade de chamadas.

## 7. Kernel monolítico e microkernel

A arquitetura monolítica coloca grande parte dos serviços no kernel, favorecendo eficiência por chamadas diretas e menos fronteiras de proteção, mas aumentando superfície, acoplamento e impacto potencial de bugs.

O microkernel mantém no núcleo mecanismos básicos, como espaços de endereço, threads e comunicação local, deixando serviços em processos servidores carregados conforme necessário. Isso favorece extensibilidade, modularidade e isolamento, mas pode acrescentar custo de comunicação e troca de contexto.

| Aspecto | Monolítico | Microkernel |
|---|---|---|
| Eficiência de chamada | tende a ser maior | pode sofrer custo de IPC |
| Tamanho do núcleo | maior | menor |
| Isolamento de serviços | menor | maior |
| Extensibilidade | módulos e integração interna | servidores substituíveis |
| Impacto de falha | potencialmente amplo | pode ser contido |
| Aplicação conceitual ao MX | runtime integrado de alto desempenho | serviços isolados e substituíveis |

A escolha real depende da plataforma, requisitos e maturidade operacional; “microkernel” não garante automaticamente maior segurança ou desempenho.

## 8. Virtualização no nível do sistema operacional

Virtualização fornece várias máquinas virtuais sobre uma arquitetura física, cada uma com uma instância de sistema operacional. O monitor aloca CPU, memória, armazenamento e rede entre as VMs. Os casos de uso incluem consolidação de servidores, nuvem, ambientes isolados, criação e destruição rápida de ambientes e execução de sistemas distintos no mesmo hardware.

O material usa Xen como referência histórica. Em infraestrutura moderna, é importante distinguir:

| Tecnologia | Isolamento | Kernel compartilhado? | Uso típico |
|---|---|---:|---|
| Processo | baixo a médio | sim | aplicação no mesmo host |
| Container | médio, depende da configuração | sim | serviços empacotados |
| VM | forte em relação a processos | não | hosts e workloads isolados |
| Bare metal | máximo controle físico | não aplicável | cargas dedicadas |

Para o MX, containers facilitam repetibilidade e implantação; VMs podem separar tenants, kernels ou ambientes com maior isolamento. O desenho deve avaliar custo, performance, superfície de ataque, atualizações e recuperação.

## 9. Objetos distribuídos

Objetos distribuídos aplicam encapsulamento, abstração de dados e interfaces ao desenvolvimento distribuído. CORBA busca interoperabilidade entre linguagens, hardware, sistemas operacionais e redes por meio de IDL e ORB. Java RMI oferece invocação remota de métodos em objetos Java.

A abstração é útil porque separa especificação e implementação e permite substituir objetos compatíveis. Porém, a chamada remota continua sujeita a latência, falhas, concorrência, ciclo de vida, persistência e localização. O programador precisa saber que está cruzando uma fronteira distribuída.

Funcionalidades adicionais de middleware de objetos incluem comunicação entre objetos, ciclo de vida, ativação/desativação, persistência e serviços auxiliares. A tendência histórica evolui de cliente-servidor para objetos e depois para componentes com dependências explícitas.

## 10. Componentes distribuídos

Um componente é uma unidade de composição encapsulada com interfaces fornecidas e interfaces necessárias. Cada dependência necessária deve ser vinculada a uma interface fornecida por outro componente. A arquitetura passa a ser representada por componentes, interfaces e conexões.

Essa abordagem separa lógica de aplicação de gerenciamento distribuído e facilita composição de funcionalidades prontas. Também explicita dependências, segurança, implantação e propriedades não funcionais.

```text
Componente A
  fornece: KnowledgeQuery
  requer: Index, Authorization

Componente B
  fornece: Index
  requer: Storage

Componente C
  fornece: Authorization
```

No MX, contratos de componentes devem incluir versão, SLO, limites, dependências, dados tratados, permissões, eventos produzidos e estratégia de falha. Um componente “plugável” sem contrato de segurança e observabilidade não é realmente reutilizável em produção.

## 11. Web Services

Web Service fornece uma interface para clientes específicos interagirem com operações remotas, normalmente por mensagens estruturadas sobre HTTP. O modelo expande a Web para além do navegador e favorece interoperabilidade entre organizações, integração B2B, mashups, Grid e nuvem.

A fonte apresenta XML como representação textual e SOAP como protocolo para envelopes de solicitação e resposta. WSDL descreve interfaces, mensagens, tipos, bindings e localização. UDDI fornece conceito de diretório e descoberta por nome ou atributo. Segurança XML permite assinar ou criptografar partes de documentos.

| Elemento | Função |
|---|---|
| HTTP | transporte comum de mensagens |
| XML | representação estruturada e interoperável |
| SOAP | envelope e regras de processamento |
| WSDL | descrição formal do serviço |
| UDDI | diretório/descoberta de serviços |
| TLS | proteção do canal |
| XML Signature/Encryption | integridade, autenticidade e confidencialidade de partes do documento |

TLS protege o canal, mas não resolve todos os requisitos de documentos que serão armazenados, retransmitidos, modificados por papéis diferentes ou encaminhados a vários destinos. Para o MX, a seleção entre SOAP/XML e REST/JSON/gRPC deve considerar integração legada, contrato, tamanho, tooling, governança e necessidade de assinatura no nível da mensagem.

## 12. Coordenação e SOA

Uma interação simples de solicitação-resposta não coordena automaticamente uma sequência de operações. Processos de negócio precisam definir ordem, condições, compensações, consistência e falhas parciais. A descrição de coordenação informa como participantes devem interagir.

SOA organiza sistemas em serviços fracamente acoplados, potencialmente descobertos e coordenados. Um serviço pode ser cliente de outros serviços e produzir um serviço composto. Mashup é uma composição de dois ou mais serviços por um terceiro desenvolvedor.

Atenção: baixo acoplamento não elimina acoplamento; ele o desloca para schemas, contratos, identidade, políticas, disponibilidade, semântica de erros e observabilidade.

## 13. Grid e nuvem

Grid compartilha arquivos, dados, software, computadores e sensores entre organizações, frequentemente para problemas de alto custo computacional ou grande volume de dados. A heterogeneidade exige gestão, segurança, coordenação e políticas de uso.

Nuvem oferece computação, armazenamento e serviços como recursos sob demanda, geralmente por uso. O modelo de negócio e a virtualização diferenciam nuvem de Grid, embora ambos compartilhem a ideia de recursos remotos como serviço. Web Services podem servir como caminho de implementação e integração.

No MX, o padrão “tudo como serviço” deve ser aplicado com cuidado: cada serviço adicional aumenta custo, latência, governança e pontos de falha. Uma arquitetura local modular pode ser preferível a uma decomposição excessiva em serviços remotos.

## 14. Aplicação ao MX

### Contrato de componente

```text
ComponentContract
- name
- version
- provided_interfaces
- required_interfaces
- permissions
- resource_limits
- timeout_ms
- retry_policy
- events_emitted
- health_checks
- metrics
- deployment_requirements
```

### Ciclo de uma chamada

```text
cliente
  -> API/gateway
  -> autenticação e autorização
  -> proxy/cliente do componente
  -> serialização e transporte
  -> serviço/worker
  -> recurso protegido pelo SO
  -> resposta, evento ou job persistido
```

### Políticas operacionais

O MX deve separar processos por responsabilidade quando houver necessidade de isolamento, usar threads para concorrência controlada dentro de um serviço, limitar chamadas paralelas, registrar `trace_id`, manter deadlines e expor health/readiness checks. Componentes de execução de código, plugins ou ferramentas externas devem ter sandbox, permissões mínimas e limites de CPU, memória, disco e rede.

## 15. Exercícios autorais e gabaritos

### Exercício 1 — processo ou thread

Um serviço precisa processar requisições I/O-bound e executar um plugin não confiável. A mesma estratégia de threads é adequada para ambos?

**Gabarito orientativo:** não. Threads são eficientes para I/O dentro de um processo confiável, mas plugin não confiável deve ser isolado em processo, container ou VM conforme risco. O limite deve incluir recursos e permissões.

### Exercício 2 — microkernel

Qual é o benefício de manter um serviço de arquivos fora do kernel?

**Gabarito orientativo:** reduzir a superfície privilegiada e permitir reinício/substituição mais isolados. O custo pode ser maior latência por IPC e complexidade de coordenação.

### Exercício 3 — chamada assíncrona

Uma consulta do MX busca cinco fontes independentes. Como reduzir latência sem perder controle?

**Gabarito orientativo:** iniciar chamadas concorrentes com limite, deadline, cancelamento após o prazo, fallback parcial e métricas por fonte. Não criar uma thread ilimitada por requisição.

### Exercício 4 — SOAP ou REST

Uma integração exige assinatura de partes de um documento que será armazenado e encaminhado. TLS sozinho resolve?

**Gabarito orientativo:** não. TLS protege o canal entre dois pontos; assinatura e criptografia no nível da mensagem podem preservar integridade e confidencialidade de partes após armazenamento ou encaminhamento.

### Exercício 5 — composição de serviços

Um serviço agregado depende de três serviços remotos e um deles falha depois de os outros dois concluírem. Qual estratégia é necessária?

**Gabarito orientativo:** definir compensação, estado intermediário, idempotência, retry limitado, timeout e resposta parcial ou falha explícita. Não presumir transação distribuída automática.

## 16. Checklist de domínio

| Competência | Evidência |
|---|---|
| Sistema operacional | explica kernel, chamadas, proteção, memória e escalonamento |
| Processos/threads | escolhe unidade de isolamento e concorrência com justificativa |
| Virtualização | diferencia processo, container e VM |
| Middleware | relaciona SO, sockets, RPC, eventos e componentes |
| Objetos | explica encapsulamento, ciclo de vida e referências remotas |
| Componentes | modela interfaces fornecidas e necessárias |
| Web Services | diferencia SOAP, WSDL, UDDI, XML e TLS |
| SOA/Grid/nuvem | avalia baixo acoplamento, composição, heterogeneidade e custo |
| MX | cria contratos, limites, permissões, métricas e falhas |

## 17. Limitações e atualização

O material apresenta CORBA, Java RMI, SOAP, WSDL, UDDI, Xen e XML em contexto acadêmico e histórico. Algumas dessas tecnologias continuam em nichos, mas não são escolhas universais para sistemas novos. A implementação atual deve considerar REST/JSON, gRPC, OpenAPI, containers, VMs, service mesh, identidade moderna, tracing e segurança da cadeia de software.

Este dossiê é uma documentação original baseada no PDF salvo. Ele não reproduz o texto integral e acrescenta análise crítica, contratos e exercícios autorais para integração ao MX.

## Referências

[1]: `ComputacaoParalelaEDistribuida3.pdf`, páginas 8–31, material salvo no projeto Estudos.

[2]: GAGLIARDI, Gary. *Cliente/servidor*. São Paulo: Makron Books do Brasil, 1996.

[3]: STALLINGS, William. *Arquitetura e organização de computadores: projeto para o desempenho*. 8. ed. Pearson, 2009.

[4]: TANENBAUM, Andrew S.; STEEN, Maarten van. *Sistemas distribuídos: princípios e paradigmas*. 2. ed. Pearson, 2008.
