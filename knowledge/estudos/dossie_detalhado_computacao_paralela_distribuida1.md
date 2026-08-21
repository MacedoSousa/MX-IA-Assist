# Dossiê detalhado — Fundamentos de Computação Paralela e Sistemas Distribuídos

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `ComputacaoParalelaEDistribuida1.pdf` |
| Unidade | Fundamentos Básicos |
| Extensão | 37 páginas físicas; conteúdo técnico concentrado nas páginas 8–33 |
| Fonte bibliográfica principal | Tanenbaum e van Steen, *Sistemas distribuídos: princípios e paradigmas* |
| Tratamento | Síntese técnica original, rastreável por páginas e complementada com aplicação ao MX |

## 1. Ideia central

Um sistema distribuído é formado por componentes de hardware ou software localizados em computadores conectados, que coordenam ações por troca de mensagens. A distribuição permite compartilhamento de recursos, paralelismo, disponibilidade e escala, mas introduz concorrência, latência, heterogeneidade, falhas independentes, segurança e ausência de um relógio global.

A principal conclusão da aula é que um sistema distribuído não deve ser projetado como se fosse um programa local apenas “espalhado” em várias máquinas. A rede é parte do comportamento: mensagens podem atrasar, perder-se, duplicar-se ou chegar fora de ordem; componentes podem continuar executando enquanto outros falham; e uma operação aparentemente simples pode envolver vários processos e políticas de recuperação.

## 2. Definição e propriedades essenciais

| Propriedade | Significado técnico | Consequência para o projeto |
|---|---|---|
| Concorrência | Processos executam simultaneamente e disputam recursos | exige isolamento, sincronização e controle de acesso |
| Cooperação | Processos coordenam ações por mensagens | exige protocolos e contratos explícitos |
| Falhas independentes | Um nó, processo ou canal pode falhar sem parar todos os demais | exige timeouts, retries, circuit breakers e recuperação |
| Heterogeneidade | Redes, hardware, SOs, linguagens e implementações diferem | exige abstração e interoperabilidade |
| Abertura | Serviços podem ser adicionados ou substituídos | exige interfaces estáveis e versionamento |
| Escalabilidade | Sistema deve continuar eficaz com mais usuários, dados ou nós | exige particionamento, cache, filas e controle de gargalos |
| Transparência | Usuário percebe o serviço como um todo, não como nós separados | exige descoberta, balanceamento e tratamento de localização |
| QoS | Confiabilidade, segurança e desempenho percebidos pelo cliente | exige SLOs, métricas e políticas de prioridade |

## 3. Exemplos e tendências

A aula utiliza busca na web, finanças, comércio eletrônico, redes sociais, educação a distância, bibliotecas digitais e ciência baseada em grid como exemplos de sistemas distribuídos. Todos dependem de redes, compartilhamento, servidores, clientes e coordenação.

As tendências discutidas incluem uma internet com dispositivos variados, computação móvel, intranets conectadas, multimídia distribuída e computação como utilidade. A computação móvel amplia o problema porque dispositivos mudam de localização, conectividade e capacidade. A multimídia acrescenta requisitos de largura de banda e latência; a computação como utilidade antecipa a lógica de serviços de nuvem alugados.

Para o MX, essas tendências significam que o assistente pode ser acessado por desktop, celular ou serviços internos, mas deve conservar identidade, autorização, estado de conversa, observabilidade e tolerância a conexões intermitentes.

## 4. Desafios de engenharia

### Heterogeneidade e abertura

Heterogeneidade abrange redes, computadores, sistemas operacionais, linguagens e bibliotecas produzidas por fornecedores distintos. O middleware reduz esse impacto ao oferecer APIs, serialização, descoberta, autenticação, filas e comunicação padronizada. A abertura depende de contratos que permitam incluir ou substituir componentes sem reescrever consumidores.

### Segurança

A fonte organiza segurança em **confidencialidade**, **integridade** e **disponibilidade**. Também menciona negação de serviço e código móvel como ameaças. Em um projeto atual, isso deve ser expandido para identidade forte, autorização por recurso, gestão de segredos, criptografia em trânsito e em repouso, validação de entrada, auditoria, limitação de taxa e isolamento de execução.

### Escalabilidade

Um sistema escalável continua eficaz quando cresce o número de usuários, recursos ou mensagens. Os riscos incluem custo físico, gargalos de desempenho e esgotamento de recursos de software. Escalar horizontalmente não resolve um componente central que preserve todo o estado ou uma fila sem capacidade; é necessário identificar o limite dominante e removê-lo por particionamento, replicação, cache ou desenho assíncrono.

### Falhas

A aula distingue detecção, tolerância, recuperação e redundância. Uma falha pode afetar hardware, software, processo ou canal. A disponibilidade aumenta quando o trabalho de um componente pode ser transferido ou refeito em outro, mas isso só é seguro se operações forem idempotentes ou tiverem deduplicação.

### Concorrência e transparência

Vários clientes podem acessar simultaneamente um recurso compartilhado. O desenho precisa definir ordem, consistência, exclusão mútua, versionamento ou resolução de conflitos. Transparência é esconder a separação dos componentes, mas não significa esconder completamente latência, indisponibilidade ou limites; um MX confiável deve fornecer mensagens de estado e erros acionáveis quando a abstração não puder mascarar a falha.

## 5. Modelos de sistemas

A fonte separa modelos **físicos**, **arquiteturais** e **fundamentais**.

| Modelo | O que descreve | Pergunta que responde |
|---|---|---|
| Físico | computadores, dispositivos e redes de interconexão | onde estão os recursos? |
| Arquitetural | componentes, tarefas e relações | quem faz o quê e como se organiza? |
| Fundamental de interação | estrutura e sequência das comunicações | quais são os limites temporais? |
| Fundamental de falhas | desvios do comportamento correto | como o sistema falha? |
| Fundamental de segurança | ameaças e proteção de processos/canais | contra quem e contra o quê o sistema se protege? |

O modelo físico é uma abstração do hardware. A arquitetura deve atender requisitos atuais e futuros de confiabilidade, gerenciabilidade, adaptabilidade e custo. O modelo fundamental é útil para raciocinar sem ficar preso a uma tecnologia específica.

## 6. Entidades e paradigmas de comunicação

A aula apresenta objetos distribuídos, componentes, serviços web, solicitação-resposta, RPC, RMI, comunicação de eventos, publicação-assinatura, filas de mensagens, tuplas e memória compartilhada distribuída.

| Paradigma | Relação | Uso típico no MX |
|---|---|---|
| Solicitação-resposta | cliente invoca serviço e aguarda resposta | API de consulta ou geração |
| RPC/RMI | chamada remota com abstração semelhante a chamada local | serviços internos fortemente tipados |
| Pub/sub | produtores publicam eventos para vários assinantes | eventos de documento, treinamento ou auditoria |
| Fila | produtor envia; consumidor processa depois | tarefas longas, retries e backpressure |
| Tuplas | processos compartilham itens em espaço indireto | coordenação desacoplada |
| Memória distribuída | abstração de dados compartilhados sem memória física comum | casos específicos que exigem estado distribuído |

A abstração de chamada remota não deve esconder completamente latência, timeout e duplicação. O contrato de uma operação remota precisa declarar timeouts, retry seguro, idempotência, tamanho máximo, autenticação e formato de erro.

## 7. Padrões arquiteturais e camadas

A arquitetura em camadas organiza um sistema complexo em níveis, em que cada camada usa serviços da inferior. O quadro da fonte apresenta hardware de rede, sistema operacional, middleware e aplicações/serviços. Middleware mascara heterogeneidade e oferece modelo de programação mais conveniente.

A arquitetura cliente-servidor pode ter duas, três ou n camadas. Duas camadas podem reduzir interações, mas concentram mais lógica e dificultam algumas separações. Três camadas separam apresentação, lógica e dados, melhorando manutenção, mas acrescentam servidores, tráfego e latência. A decisão não deve ser dogmática: a decomposição precisa refletir limites de negócio, segurança, escala e operação.

No MX, uma decomposição coerente é:

```text
cliente/interface
  -> gateway e autenticação
  -> orquestrador de tarefas
  -> serviços de conhecimento, LLM, memória e ferramentas
  -> filas e workers
  -> bancos, índice, objetos e logs
```

O middleware do MX deve padronizar autenticação, correlação, tracing, serialização, retries, limitação de taxa e contratos de eventos.

## 8. Modelos de interação e falha

Sistemas síncronos possuem limites conhecidos para execução de processo, entrega de mensagens e desvio de relógio. Sistemas assíncronos não oferecem esses limites; esse modelo é mais próximo da internet. A ausência de relógio global impede que um componente conclua simplesmente, pelo silêncio, que outro morreu.

Canais podem corromper mensagens, omitir entregas ou entregar mensagens mais de uma vez. A retransmissão pode mascarar omissões, mas pode também criar duplicatas. Por isso, o MX deve usar identificadores de mensagem, chaves de idempotência, confirmação, dead-letter queue e reconciliação.

Uma política mínima de falha:

| Falha | Resposta recomendada |
|---|---|
| Timeout | interromper espera, registrar contexto e aplicar retry limitado |
| Duplicação | deduplicar por `event_id` ou operação idempotente |
| Mensagem fora de ordem | sequência, watermark ou estado versionado |
| Serviço indisponível | circuit breaker e fallback seguro |
| Perda de conexão | persistir progresso e retomar |
| Corrupção | checksum, validação de schema e rejeição rastreável |

## 9. Segurança e canais seguros

A aula explica que um inimigo pode copiar, alterar ou injetar mensagens. Criptografia protege o conteúdo; autenticação comprova a identidade do principal. Um canal seguro deve garantir identidade do outro processo, privacidade, integridade e proteção contra repetição ou reordenação por meio de registro temporal ou equivalente.

VPN e SSL/TLS são citados como instâncias práticas. Na implementação atual do MX, recomenda-se TLS moderno, autenticação mútua quando apropriado, tokens curtos, rotação de segredos, autorização por escopo, proteção contra replay e logs sem dados sensíveis. Segredos não devem ser colocados no código ou em prompts.

## 10. Redes e internetworking

Sistemas distribuídos usam LAN e WAN. O comportamento é afetado por desempenho, confiabilidade, escala, mobilidade e QoS. A internet integra sub-redes heterogêneas usando protocolos, roteadores, endereçamento e comutação de pacotes.

### Camadas de rede apresentadas

| Camada OSI | Função resumida |
|---:|---|
| 7 Aplicação | serviços de rede usados pelas aplicações |
| 6 Apresentação | formato, compressão e criptografia |
| 5 Sessão | estabelecimento, manutenção e retomada de sessões |
| 4 Transporte | entrega fim a fim, fluxo e correção de erros |
| 3 Rede | endereçamento e escolha de rota |
| 2 Enlace | quadros, entrega local e verificação de integridade |
| 1 Física | sinais, meios e especificações elétricas/mecânicas |

A correspondência com pilhas reais não é sempre um mapeamento perfeito; o modelo é uma ferramenta didática para localizar responsabilidades.

### Roteamento e congestionamento

Roteamento escolhe caminhos entre origem e destino; em redes grandes pode ser adaptativo, considerando tráfego e falhas. Congestionamento ocorre quando filas crescem até os buffers não suportarem mais pacotes. Controle de congestionamento reduz taxa, enfileira ou bloqueia produtores. No MX, filas internas precisam de capacidade, backpressure, limites de concorrência e política explícita de descarte ou prioridade.

### Tipos de rede

A fonte diferencia PAN, LAN, WAN, MAN, WLAN e WWAN. O ponto arquitetural não é decorar siglas, mas reconhecer que distância, meio, mobilidade e capacidade influenciam latência, perda, custo e disponibilidade.

## 11. TCP/IP, IP, IPv6, TCP, UDP e DNS

O sucesso do TCP/IP vem da independência em relação à mídia subjacente, permitindo que redes heterogêneas sejam percebidas como uma rede virtual comum. IP transporta datagramas entre hosts, possivelmente por roteadores. Endereçamento e roteamento são responsabilidades centrais.

IPv6 amplia substancialmente o espaço de endereçamento do IPv4 e foi desenvolvido para atender à evolução da internet. TCP oferece transporte confiável, enquanto UDP não garante entrega e pode ser preferível quando baixa latência e controle na aplicação são mais importantes. TCP e UDP usam portas para entregar dados a processos dentro de um host. DNS converte nomes de domínio em endereços e torna a localização dos serviços mais operável.

No MX, HTTP sobre TCP pode servir APIs tradicionais; protocolos de streaming ou transporte moderno podem exigir outras opções. A escolha deve considerar ordenação, confiabilidade, latência, tamanho, retransmissão, firewall e observabilidade, não apenas familiaridade.

## 12. Integração ao MX

### Contrato de chamada remota

```text
Request
- request_id
- actor_id
- operation
- schema_version
- deadline
- idempotency_key
- payload
- trace_id

Response
- request_id
- status
- result
- error_code
- retryable
- trace_id
- generated_at
```

### Métricas fundamentais

| Área | Métricas |
|---|---|
| Rede | latência p50/p95/p99, perda, retransmissão e bytes |
| Serviço | taxa de sucesso, erros, timeout e saturação |
| Fila | profundidade, idade da mensagem, throughput e dead letters |
| Segurança | falhas de autenticação, rejeições, replay e alterações de permissão |
| Estado | conflitos, versões, checkpoints e recuperação |
| QoS | cumprimento de SLO, disponibilidade e tempo de resposta |

### Arquitetura de referência

```text
cliente
  -> gateway seguro
  -> autenticação/autorização
  -> serviço de orquestração
  -> fila para tarefas demoradas
  -> workers de ingestão, recuperação, LLM e persistência
  -> armazenamento versionado
  -> métricas, logs e traces correlacionados
```

O assistente local pode operar sem exposição pública, mas ainda é distribuído se possuir processos separados, banco, workers ou modelos em serviços distintos. “Local” reduz superfície de rede externa; não elimina concorrência, falhas nem necessidade de contratos.

## 13. Exercícios autorais e gabaritos

### Exercício 1 — falha independente

O serviço de busca fica indisponível, mas o serviço de conversa continua ativo. Como preservar a experiência?

**Gabarito orientativo:** usar timeout curto, circuit breaker, resposta degradada explicitando a limitação, fila para reprocessamento quando possível e registro do evento. Não inventar resultados para ocultar a indisponibilidade.

### Exercício 2 — retry perigoso

Uma operação de gravação demorou e o cliente não sabe se foi concluída. Um retry cego é seguro?

**Gabarito orientativo:** não necessariamente. Pode duplicar a operação. Usar idempotency key, consulta de status ou transação com deduplicação antes de repetir.

### Exercício 3 — TCP ou UDP

Uma telemetria tolera perda ocasional, mas exige baixa latência. Qual decisão deve ser analisada?

**Gabarito orientativo:** UDP ou transporte equivalente pode reduzir custo de retransmissão, mas exige segurança, ordenação ou agregação na aplicação quando necessárias. A decisão deve ser validada por perda, QoS e exposição da rede.

### Exercício 4 — arquitetura em camadas

Quais são os custos de dividir o MX em gateway, orquestrador, workers e armazenamento?

**Gabarito orientativo:** mais deploys, rede, observabilidade, contratos e pontos de falha. Os benefícios são isolamento, escala independente, segurança e manutenção. A divisão deve ser proporcional ao domínio e à carga.

### Exercício 5 — congestão

Uma fila cresce continuamente mesmo com aumento de workers. O que investigar?

**Gabarito orientativo:** produtor acima da capacidade real, dependência externa lenta, partição quente, limite de banco, retries em cascata, mensagens grandes ou consumidores bloqueados. Aumentar workers sem remover o gargalo pode agravar a saturação.

## 14. Checklist de domínio

| Competência | Evidência de domínio |
|---|---|
| Sistemas distribuídos | define concorrência, cooperação e falhas independentes |
| Arquitetura | distingue modelo físico, arquitetural e fundamental |
| Comunicação | compara RPC, pub/sub, filas e solicitação-resposta |
| Middleware | explica abstração, interoperabilidade e limites |
| Falhas | projeta timeout, retry, idempotência e recuperação |
| Segurança | diferencia confidencialidade, integridade e disponibilidade |
| Redes | relaciona camadas, IP, roteamento, congestionamento e QoS |
| MX | traduz os fundamentos em contratos, métricas e componentes |

## 15. Limitações e atualização

O material foi elaborado como introdução acadêmica e contém exemplos, nomenclaturas e referências associados ao período de sua publicação. Alguns serviços e tecnologias citados mudaram; a ideia de “transparência” também deve ser aplicada com cautela porque latência e falhas não podem ser completamente escondidas. Links abreviados e referências externas devem ser validados antes de uso.

Este dossiê não reproduz o texto integral do PDF. Ele reorganiza os conceitos em redação original, adiciona análise crítica, contratos técnicos e aplicação ao MX, preservando a rastreabilidade por páginas e a referência bibliográfica.

## Referências

[1]: `ComputacaoParalelaEDistribuida1.pdf`, páginas 8–33, material salvo no projeto Estudos.

[2]: TANENBAUM, A. S.; STEEN, M. V. *Sistemas distribuídos: princípios e paradigmas*. 2. ed. Pearson, 2007.

[3]: GAGLIARDI, G. *Cliente/servidor*. Makron Books do Brasil, 1996.

[4]: STALLINGS, W. *Arquitetura e organização de computadores: projeto para o desempenho*. 8. ed. Pearson Prentice Hall, 2009.
