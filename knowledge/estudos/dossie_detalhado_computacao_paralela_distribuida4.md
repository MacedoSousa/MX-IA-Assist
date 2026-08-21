# Dossiê detalhado — Sistemas de Compartilhamento e Segurança em Sistemas Distribuídos

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `ComputacaoParalelaEDistribuida4.pdf` |
| Unidade | Sistemas de Compartilhamento e Segurança em Sistemas Distribuídos |
| Conteúdo técnico | páginas 8–27; referências na página 29 |
| Temas centrais | Peer-to-peer, segurança, criptografia, autenticação, controle de acesso e sistemas de arquivos distribuídos |
| Tratamento | Documentação original, análise crítica, rastreabilidade e aplicação ao MX |

## 1. Visão geral

A unidade analisa três formas complementares de compartilhamento distribuído: o paradigma **peer-to-peer**, os mecanismos de **segurança** e os **sistemas de arquivos distribuídos**. A tese comum é que retirar a dependência de um servidor central aumenta escala e aproveitamento de recursos, mas transfere complexidade para descoberta, roteamento, replicação, consistência, autenticação, auditoria e recuperação de falhas.

A aplicação ao MX deve ser seletiva. Peer-to-peer é adequado para artefatos grandes, imutáveis e verificáveis; não é automaticamente adequado para memória de conversação mutável ou dados que exigem governança central. Segurança precisa ser projetada a partir de ameaças, políticas e mecanismos, não reduzida à instalação de criptografia ou firewall.

## 2. Sistemas peer-to-peer

Peer-to-peer é um paradigma no qual muitos nós contribuem com dados, armazenamento, processamento ou presença para oferecer um serviço uniforme. Diferentemente do modelo cliente-servidor tradicional, os recursos ficam distribuídos entre computadores participantes.

Características recorrentes:

| Característica | Implicação arquitetural |
|---|---|
| Contribuição de recursos | cada participante fornece capacidade ao sistema |
| Simetria funcional | nós podem atuar como clientes e provedores |
| Descentralização | não depende de um único sistema administrado |
| Auto-organização | entrada e saída de nós são eventos normais |
| Balanceamento | armazenamento, rede e processamento precisam ser distribuídos |
| Disponibilidade | exige réplicas, descoberta e detecção de nós ativos |
| Anonimato potencial | pode ocultar origem, destino ou provedor, com limitações |

O problema fundamental é localizar objetos distribuídos com baixa sobrecarga, equilibrando carga e disponibilidade. A escolha do algoritmo de posicionamento é tão importante quanto o transporte de dados.

## 3. Evolução histórica e lições de projeto

A unidade distingue três gerações. A primeira ficou associada ao Napster, que usava índice centralizado enquanto os arquivos eram fornecidos pelos usuários. A segunda incluiu soluções como Freenet, Gnutella, Kazaa e BitTorrent, buscando mais escala, anonimato e tolerância. A terceira introduziu middleware para gerenciar posicionamento, localização, roteamento e replicação de forma independente da aplicação.

A principal lição do Napster não é reproduzir seu desenho, mas observar a combinação de índice, recursos nas bordas e localização de rede. O encerramento do serviço também evidencia que escalabilidade técnica não elimina direitos autorais, governança e responsabilidade legal.

## 4. Middleware peer-to-peer e sobreposição de roteamento

O middleware peer-to-peer automatiza a colocação e a localização de objetos. O cliente não precisa manter a posição de cada recurso; envia uma operação acompanhada de um identificador global, e a sobreposição encaminha a solicitação.

Em sistemas estruturados, objetos recebem GUIDs, geralmente derivados de hashes, e são mapeados a nós por uma função. A sobreposição mantém tabelas de roteamento e encaminha a requisição segundo uma noção de distância. Em sistemas não estruturados, nós formam uma rede ad hoc e pesquisas são propagadas entre vizinhos.

| Abordagem | Localização | Vantagem | Custo/risco |
|---|---|---|---|
| Estruturada | GUID e função de mapeamento | busca previsível e escalável | manutenção de tabelas e churn |
| Não estruturada | vizinhos e propagação | flexibilidade e simplicidade inicial | busca imprevisível e maior tráfego |
| Índice central | diretório único ou pequeno conjunto | consulta simples | ponto de falha e gargalo |
| Híbrida | diretório + dados distribuídos | equilíbrio operacional | dependência parcial do diretório |

Um hash seguro pode tornar um recurso auto-certificável: o cliente verifica se o conteúdo recebido corresponde ao identificador esperado. Isso funciona especialmente bem para dados imutáveis; se o conteúdo muda, o hash também muda.

## 5. Replicação, disponibilidade e imutabilidade

Réplicas reduzem impacto da indisponibilidade de nós, mas não garantem disponibilidade por si sós. É necessário distribuir réplicas de modo que não compartilhem simultaneamente a mesma falha de região, rede, host ou operador.

Para artefatos imutáveis, o MX pode armazenar módulos, modelos, índices, documentos processados e pacotes por conteúdo. O identificador pode ser um digest criptográfico do arquivo. Para dados mutáveis, deve haver versionamento, controle de concorrência, autoridade de escrita e política de resolução de conflitos.

```text
artefato -> hash de conteúdo -> réplicas independentes -> verificação no download
```

## 6. Segurança: política, mecanismo e ameaça

A unidade diferencia **política de segurança** de **mecanismo de segurança**. A política define quem pode acessar qual recurso, em que condição e com qual operação. O mecanismo implementa autenticação, autorização, criptografia, assinatura, firewall, log e isolamento.

A segurança deve começar com uma lista de ameaças e uma hipótese de pior caso. Não existe mecanismo único que prove segurança completa. Além de controles preventivos, sistemas sensíveis precisam de auditoria e registros seguros das ações, com identidade, autoridade, horário, resultado e contexto.

### Propriedades protegidas

| Propriedade | Pergunta de controle |
|---|---|
| Confidencialidade | somente destinatários autorizados conseguem ler? |
| Integridade | alterações não autorizadas são detectadas? |
| Disponibilidade | o serviço continua acessível sob falhas e abuso? |
| Autenticidade | a identidade do participante foi verificada? |
| Não repúdio | há evidência verificável da autoria? |
| Rastreabilidade | a ação pode ser auditada depois? |

## 7. Ameaças e ataques

As três classes apresentadas são vazamento, adulteração e vandalismo. Ataques em canais distribuídos incluem escuta, mascaramento, modificação, repetição e negação de serviço.

| Ataque | Descrição | Defesa típica |
|---|---|---|
| Eavesdropping | cópia não autorizada de mensagens | criptografia de transporte e/ou mensagem |
| Mascaramento | uso indevido da identidade de outro principal | autenticação forte, certificados, tokens |
| Man-in-the-middle | interceptação e substituição na negociação | autenticação de chaves e validação de cadeia |
| Replay | repetição de mensagem válida | nonce, timestamp, sequência e idempotência |
| Adulteração | modificação antes da entrega | MAC, assinatura e verificação de integridade |
| DoS | saturação de canal ou recurso | rate limit, quotas, isolamento e mitigação |

No MX, uma mensagem de comando deve possuir identificador único, timestamp ou janela de validade, identidade do emissor, escopo de autorização e proteção contra reexecução.

## 8. Criptografia, TLS e certificados

A criptografia simétrica usa uma chave secreta compartilhada e é apropriada para grandes volumes de dados. A criptografia assimétrica usa par público/privado, facilitando distribuição e autenticação, mas com custo maior para processamento em massa.

TLS combina as duas abordagens: usa criptografia assimétrica para estabelecer ou proteger a negociação de chaves e depois utiliza chave simétrica para o fluxo de dados. O certificado digital associa uma identidade a uma chave pública por uma autoridade certificadora.

A criptografia pode prover sigilo, integridade, autenticação de comunicação e assinatura digital. Não substitui controle de acesso: um conteúdo pode estar criptografado e ainda assim ser entregue a um principal autorizado incorreto.

## 9. Controle de acesso

O servidor deve autenticar o principal e depois aplicar autorização para verificar se ele pode executar a operação sobre o recurso. O domínio de proteção associa processos a recursos e operações permitidas.

Credenciais são evidências apresentadas durante a solicitação de acesso. Credenciais baseadas em papéis permitem expressar permissões organizacionais ou funcionais, mas devem ser combinadas com escopo, contexto, expiração e princípio do menor privilégio.

### Modelo aplicado ao MX

```text
principal autenticado
  -> identidade e contexto
  -> papel/atributos
  -> recurso e operação
  -> política
  -> decisão permit/deny
  -> auditoria imutável
```

Para plugins, ferramentas e agentes, o MX deve usar identidades separadas, permissões mínimas, diretórios restritos, limites de rede e de processo, além de aprovação para ações de alto impacto.

## 10. Firewalls e assinaturas digitais

Firewalls filtram comunicações de entrada e saída. São úteis para reduzir exposição, mas não protegem contra ameaças internas, falhas de aplicação ou abuso por credenciais válidas. Devem ser parte de defesa em profundidade.

Assinaturas digitais fornecem evidências de autenticidade, integridade e intenção de assinatura. O material também apresenta não repúdio como requisito de transações, embora a força jurídica dessa propriedade dependa de contexto, legislação, gestão de chaves e procedimentos.

O padrão X.509 vincula chave pública a uma entidade por assinatura de uma autoridade certificadora e inclui período de validade. Em produção, revogação, rotação, armazenamento de chaves e proteção de autoridades são tão importantes quanto o formato.

## 11. Sistemas de arquivos distribuídos

Um sistema de arquivos distribuído permite acessar arquivos remotos como se fossem locais. Seus objetivos são transparência de acesso, desempenho, escalabilidade, persistência, tolerância a falhas, segurança e compartilhamento.

O sistema gerencia dados e metadados. Metadados incluem tamanho, timestamps, tipo, proprietário, diretórios e listas de controle de acesso. Diretórios mapeiam nomes para identificadores internos. Em uma implementação distribuída, verificações de autorização devem ocorrer no servidor, pois a interface RPC é um ponto exposto.

### Requisitos

| Requisito | Questão técnica |
|---|---|
| Transparência | o cliente conhece ou não a localização? |
| Localização | nomes permanecem válidos após migração? |
| Desempenho | cache reduz latência sem invalidar dados? |
| Escala | metadados e tráfego suportam crescimento? |
| Concorrência | como leituras e escritas se ordenam? |
| Replicação | quantas cópias e onde ficam? |
| Falhas | como detectar, recuperar e reintegrar? |
| Heterogeneidade | clientes usam plataformas diferentes? |
| Segurança | identidade, ACL, criptografia e auditoria |
| Consistência | qual garantia após uma atualização? |

O cache melhora desempenho, mas cria o problema de consistência. A Web usa verificações explícitas e tolera cópias desatualizadas em muitos cenários, enquanto aplicações colaborativas exigem garantias mais fortes.

## 12. Serviço de arquivos, diretórios e NFS

O serviço simples de arquivos executa operações de leitura e escrita usando identificadores de arquivo. O serviço de diretório converte nomes textuais em identificadores e administra hierarquia de diretórios. Leitura e escrita precisam indicar posição e quantidade de dados.

NFS é citado como exemplo de protocolo simples e stateless que permaneceu relevante por melhorias de protocolo, implementação e hardware. O desenho stateless facilita recuperação, mas exige estratégias cuidadosas para locks, cache, consistência e identificação de operações.

## 13. Aplicação ao MX

O MX pode usar um repositório distribuído por conteúdo para artefatos imutáveis:

```text
conteúdo -> digest -> manifesto assinado -> armazenamento replicado
         -> validação de integridade -> cache local -> uso pelo componente
```

Para memória mutável, recomenda-se armazenamento autoritativo com versionamento e logs de alteração. O peer-to-peer pode distribuir cópias de leitura, mas a escrita deve passar por política de autoridade.

Um serviço de arquivos do MX deve separar dados, metadados e autorização. O caminho lógico não deve depender diretamente do host físico. A recuperação deve verificar checksum, versão, assinatura, disponibilidade das réplicas e permissão do solicitante.

### Checklist de segurança do MX

| Controle | Implementação esperada |
|---|---|
| Identidade | principal distinto para usuário, agente e serviço |
| Autorização | RBAC/ABAC com menor privilégio |
| Canal | TLS com validação de certificados |
| Mensagem | nonce, idempotência e assinatura quando necessário |
| Segredos | cofre, rotação e nunca em logs |
| Artefatos | hash, assinatura, versão e manifesto |
| Rede | firewall, segmentação e egress controlado |
| Processo | sandbox, quotas e limites |
| Auditoria | log append-only com timestamp e trace_id |
| Recuperação | backup, réplica, teste de restauração e revogação |

## 14. Exercícios autorais e gabaritos

### Exercício 1 — imutabilidade

Por que um sistema peer-to-peer baseado em hash favorece arquivos imutáveis?

**Gabarito orientativo:** o hash identifica o conteúdo. Se o arquivo mudar, o identificador muda; portanto, atualizações exigem nova versão e novo endereço lógico. Dados mutáveis exigem autoridade, versionamento e consistência adicionais.

### Exercício 2 — ataque replay

Um comando assinado continua válido depois de ser capturado. Como impedir sua repetição?

**Gabarito orientativo:** incluir nonce ou identificador único, janela temporal, estado de comandos processados, expiração e operação idempotente quando possível. Assinatura sozinha prova autoria do conteúdo, não sua atualidade.

### Exercício 3 — firewall

Um firewall elimina a necessidade de autorização no serviço?

**Gabarito orientativo:** não. O firewall controla tráfego, mas não decide se uma identidade autenticada pode executar determinada operação. A autorização precisa ser aplicada no servidor e no recurso.

### Exercício 4 — cache

Dois clientes leem cópias diferentes de um arquivo após uma escrita. Quais decisões precisam estar explícitas?

**Gabarito orientativo:** modelo de consistência, invalidação, leases, versionamento, ordem de escrita e comportamento durante partição ou reconexão.

### Exercício 5 — artefato distribuído

Como distribuir um modelo de IA entre nós não confiáveis sem aceitar corrupção silenciosa?

**Gabarito orientativo:** publicar digest e manifesto assinado, validar assinatura e checksum no recebimento, usar réplicas independentes, restringir origem e registrar versão, identidade e resultado da validação.

## 15. Limitações e atualização

O material apresenta exemplos históricos e fundamentos duradouros. Napster, Freenet, Gnutella, Kazaa, BitTorrent, X.509, NFS e TLS devem ser interpretados como referências conceituais e tecnológicas de épocas distintas. Em uma implementação atual, também devem ser avaliados content-addressable storage, object storage, mTLS, OAuth/OIDC, KMS/HSM, políticas como código, SBOM, supply-chain security, observabilidade e zero trust.

A unidade não deve ser lida como autorização para armazenar conteúdo protegido ou operar compartilhamento anônimo sem governança. O uso real precisa respeitar direitos autorais, privacidade, políticas de segurança e requisitos legais aplicáveis.

## Referências

[1]: `ComputacaoParalelaEDistribuida4.pdf`, páginas 8–27 e 29, material salvo no projeto Estudos.

[2]: GAGLIARDI, Gary. *Cliente/servidor*. São Paulo: Makron Books do Brasil, 1996.

[3]: TANENBAUM, Andrew S.; STEEN, Maarten van. *Sistemas distribuídos: princípios e paradigmas*. 2. ed. Pearson, 2008.

[4]: STALLINGS, William. *Arquitetura e organização de computadores: projeto para o desempenho*. 8. ed. Pearson, 2009.
