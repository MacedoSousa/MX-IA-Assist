# Dossiê detalhado — Big Data com computação em nuvem

## Identificação da fonte

| Campo | Informação |
|---|---|
| Arquivo analisado | `BigData Aula3.pdf` |
| Tipo | Material teórico acadêmico fornecido ao projeto |
| Extensão observada | 21 páginas físicas, com conteúdo principal nas páginas 1, 3 e 5–16 |
| Unidade | Big Data com Computação em Nuvem |
| Tema central | Relação entre Big Data e nuvem, características, benefícios e modelos |
| Uso neste dossiê | Documentação técnica original, análise crítica e aplicação ao MX |

## 1. Tese central

O material apresenta a computação em nuvem como um conjunto compartilhado de recursos de hardware e software acessíveis por rede, provisionados sob demanda e liberados com baixo esforço operacional. A relação com Big Data é principalmente arquitetural: Big Data exige capacidade variável de armazenamento, processamento, rede e análise, enquanto a nuvem oferece mecanismos para provisionar, medir e escalar esses recursos.

A nuvem não elimina os desafios de Big Data. Ela muda a forma de adquiri-los e operá-los. Um provedor pode fornecer elasticidade, mas a equipe continua responsável por modelagem, qualidade, segurança, custos, contratos, observabilidade, recuperação e adequação do serviço ao problema.

> **Nuvem é um modelo de entrega e operação de recursos; Big Data é um conjunto de desafios de escala, variedade, velocidade, veracidade e valor.**

## 2. Computação em nuvem e Big Data

A analogia do material com o fornecimento de energia ajuda a explicar o consumo sob demanda: o usuário utiliza uma capacidade abstraída sem precisar administrar todos os componentes físicos que a produzem. A analogia, contudo, tem limite. Energia normalmente não permite a mesma variedade de configuração, portabilidade e dependência de APIs que serviços de TI oferecem.

A nuvem pode beneficiar Big Data em quatro frentes. Primeiro, fornece armazenamento e processamento sob demanda para cargas variáveis. Segundo, permite experimentar serviços sem adquirir um datacenter próprio. Terceiro, oferece serviços gerenciados para ingestão, bancos, filas, análise e observabilidade. Quarto, facilita distribuição geográfica e recuperação quando configurada corretamente.

A relação não é automaticamente econômica. Uma carga constante pode ser mais barata em infraestrutura própria ou reserva de capacidade. Dados muito sensíveis podem exigir nuvem privada, controles adicionais ou processamento local. Transferir grandes volumes para fora do ambiente pode gerar custo de rede e latência. A decisão deve ser baseada em requisitos, risco, custo total e capacidade operacional.

## 3. Características essenciais da nuvem

O material apresenta cinco características essenciais: autosserviço sob demanda, acesso amplo pela rede, pool de recursos, elasticidade rápida e serviço medido.

| Característica | Significado | Implicação para Big Data | Controle recomendado |
|---|---|---|---|
| Autosserviço sob demanda | O consumidor provisiona recursos por portal ou API | Criação rápida de clusters, buckets, filas e ambientes | IAM, aprovação, quotas e infraestrutura como código |
| Acesso amplo pela rede | Serviços acessíveis por dispositivos e redes compatíveis | Ingestão distribuída e acesso remoto às plataformas | TLS, redes privadas, endpoints e controle de origem |
| Pool de recursos | Recursos compartilhados e alocados dinamicamente | Multi-tenancy, escalabilidade e melhor utilização | isolamento, criptografia, limites e classificação de dados |
| Elasticidade rápida | Recursos aumentam e diminuem conforme a carga | Processamento de picos, streaming e experimentos | autoscaling, backpressure, limites e testes de carga |
| Serviço medido | Uso monitorado, controlado e reportado | FinOps, custo por pipeline, consulta e documento | tags, budgets, alertas, rate cards e showback |

### 3.1 Autosserviço sob demanda

O autosserviço reduz o tempo entre necessidade e ambiente funcional. Para não transformar agilidade em proliferação descontrolada, o MX deve usar catálogos aprovados, perfis de acesso, quotas, expiração automática e infraestrutura como código. Um ambiente de experimento precisa nascer com finalidade, proprietário, orçamento, data de expiração e política de dados.

### 3.2 Acesso amplo pela rede

A acessibilidade por rede habilita clientes móveis, navegadores, serviços de ingestão e agentes distribuídos. Ela também aumenta a superfície de ataque. Dados acadêmicos, logs e conversas não devem ficar expostos apenas porque o serviço é acessível pela internet. O projeto deve preferir redes privadas, autenticação forte, menor privilégio, TLS e registros de auditoria.

### 3.3 Pool de recursos e multi-tenancy

O provedor reúne recursos físicos e virtuais para atender múltiplos consumidores. A localização exata pode ser abstraída, embora normalmente seja possível definir região, zona ou país. Para o MX, a abstração deve ser confrontada com residência de dados, soberania, latência, disponibilidade e requisitos de continuidade.

### 3.4 Elasticidade rápida

Elasticidade significa ajustar capacidade, não garantir que qualquer aplicação escale automaticamente. Pipelines precisam ser stateless quando possível, usar filas ou particionamento, tolerar reexecução e limitar concorrência. Aumentar trabalhadores sem controlar o downstream pode apenas transferir o gargalo para banco, API ou rede.

### 3.5 Serviço medido

A medição permite cobrança e planejamento. No MX, ela deve ser convertida em observabilidade de custo: custo por documento processado, por milhão de tokens, por consulta vetorial, por hora de worker, por GB armazenado e por evento. Uma conta global não permite localizar desperdício.

## 4. Benefícios e limites

### 4.1 Agilidade

A nuvem reduz o tempo de aquisição e provisionamento, permitindo experimentos e lançamento mais rápidos. O benefício é maior quando a equipe dispõe de automação e padrões prontos. Sem governança, a mesma agilidade cria ambientes órfãos, permissões excessivas e despesas inesperadas.

### 4.2 Custos de TI e TCO

O material relaciona nuvem a pagamento por uso, redução de CAPEX e menor necessidade de datacenter próprio. Porém, **pay-as-you-go não significa custo automaticamente menor**. O TCO precisa incluir computação, armazenamento, rede, egress, serviços gerenciados, observabilidade, suporte, segurança, operação, backup e migração.

Uma estimativa simples é:

```text
custo_mensal = compute + storage + requests + network + managed_services
             + observability + backup + support + engineering_overhead
```

A comparação deve usar a mesma carga e o mesmo nível de disponibilidade. Um serviço barato, mas sem backup, monitoramento ou recuperação adequada, não é equivalente a uma solução operacional completa.

### 4.3 Alta disponibilidade

Redundância de computação, rede, armazenamento e software pode reduzir indisponibilidade. Entretanto, a alta disponibilidade precisa ser contratada, desenhada, testada e medida. Distribuir cópias em regiões diferentes pode elevar custo, complexidade e exigências de proteção de dados.

### 4.4 Continuidade do negócio

Backups e ambientes remotos ajudam a recuperar serviços após desastre, falha técnica ou erro humano. A cópia só é útil se puder ser restaurada. O MX deve definir RPO, RTO, frequência, retenção, criptografia, segregação de contas e testes periódicos de restauração.

```text
RPO = quantidade máxima de dados que pode ser perdida
RTO = tempo máximo aceitável para recuperar o serviço
```

### 4.5 Escalabilidade

A nuvem permite adicionar e liberar recursos para cargas sazonais. A aplicação deve possuir limites, testes de pico, políticas de fila e mecanismos de degradação controlada. Escalar um sistema que não possui idempotência pode gerar duplicatas e inconsistências em maior velocidade.

### 4.6 Flexibilidade de acesso

Aplicações centralizadas podem ser acessadas de múltiplos dispositivos. Isso facilita mobilidade, mas exige autenticação, autorização por recurso, proteção de sessão, gerenciamento de dispositivos e atenção a redes não confiáveis. BYOD não deve ser confundido com acesso irrestrito.

### 4.7 Desenvolvimento e testes

Ambientes isolados de nuvem facilitam testes em múltiplas plataformas e reduzem impacto sobre produção. O benefício só se concretiza com dados sintéticos ou anonimizados, contas separadas, limites financeiros e destruição automática do ambiente.

### 4.8 Gerenciamento simplificado e abstração

Serviços gerenciados deslocam tarefas de hardware, patching e atualização para o provedor. A equipe ainda precisa administrar configuração, identidade, dados, arquitetura, contratos e comportamento do serviço. A abstração reduz complexidade de infraestrutura, mas pode ocultar dependências e dificultar migração.

## 5. Modelos de serviço

O material utiliza a classificação do NIST em IaaS, PaaS e SaaS. A diferença central é a divisão de responsabilidades.

| Modelo | O provedor entrega | A equipe controla principalmente | Exemplo de uso no MX |
|---|---|---|---|
| IaaS | computação, rede, armazenamento e recursos fundamentais | sistema operacional, runtime, aplicativos e parte da rede | worker customizado, cluster próprio e banco autogerenciado |
| PaaS | plataforma de execução, banco, runtime e ferramentas | código, dados e configuração da aplicação | API, pipeline ou serviço sem administrar servidores |
| SaaS | aplicativo pronto acessado por cliente ou API | configuração, usuários e dados de uso | colaboração, CRM, e-mail ou observabilidade SaaS |

Serviços especializados como backup, rede, desktop, teste e recuperação de desastre podem ser classificados como ofertas derivadas. O importante é documentar a fronteira de responsabilidade.

### 5.1 IaaS

IaaS oferece maior controle e flexibilidade, mas transfere à equipe administração do sistema operacional, patching, rede, runtime e parte da disponibilidade. É adequado quando o MX exige binários específicos, processamento customizado, controle de sistema ou migração de uma aplicação existente. O custo operacional pode superar a vantagem se a equipe não automatizar configuração e atualização.

### 5.2 PaaS

PaaS permite concentrar o trabalho na aplicação e nos dados, usando linguagens, bibliotecas e ferramentas suportadas. Reduz tarefas de infraestrutura e acelera entrega. Em contrapartida, pode impor limites de runtime, modelo de dados, observabilidade, escalabilidade, rede ou portabilidade. A equipe deve avaliar lock-in e plano de saída.

### 5.3 SaaS

SaaS oferece uma aplicação pronta, normalmente multi-tenant, acessada por navegador ou API. A equipe não controla a infraestrutura subjacente e depende do fornecedor para disponibilidade, mudanças, exportação e proteção. Contratos devem avaliar localização de dados, retenção, integração, auditoria, SLA, portabilidade e encerramento.

## 6. Modelos de implantação

| Modelo | Característica | Quando pode ser adequado | Cuidado principal |
|---|---|---|---|
| Pública | Infraestrutura disponível para público geral, operada por provedor | elasticidade, serviços gerenciados e experimentação | isolamento, custo, residência e dependência |
| Privada | Uso exclusivo de uma organização | requisitos específicos de controle, segurança ou legado | custo e responsabilidade operacional |
| Comunidade | Uso por organizações com requisitos compartilhados | conformidade, missão ou política comum | governança coletiva e fronteiras de acesso |
| Híbrida | Composição de duas ou mais nuvens distintas conectadas | manter dados ou cargas em ambientes diferentes | portabilidade, rede, consistência e complexidade |

A nuvem híbrida não é apenas “usar dois provedores”. É necessário haver integração padronizada ou proprietária que permita movimentar dados ou aplicações, equilibrar carga ou operar capacidades complementares. A portabilidade real deve ser testada, pois formatos, APIs e serviços gerenciados podem ser incompatíveis.

## 7. Relação dos modelos com Big Data

Big Data na nuvem pode ser implementado com armazenamento de objetos, bancos analíticos, data warehouses, clusters distribuídos, processamento serverless, streams, filas, notebooks e serviços de ML. A escolha depende da combinação de volume, velocidade, variedade, veracidade, valor, latência e custo.

| Necessidade | Opção arquitetural possível | Critério de escolha |
|---|---|---|
| Documentos em grande volume | object storage + catálogo + processamento distribuído | custo, retenção, particionamento e linhagem |
| Eventos contínuos | broker/stream + consumidores escaláveis | ordenação, entrega, lag e reprocessamento |
| Consulta analítica | warehouse ou lakehouse | esquema, concorrência, custo e governança |
| Busca semântica | índice vetorial ou busca híbrida | recall, latência, atualização e explicabilidade |
| Treino de modelos | jobs distribuídos + armazenamento versionado | reprodutibilidade, custo e dados de treino |
| API de aplicação | PaaS ou containers em IaaS | portabilidade, operação e limites do runtime |

A nuvem deve complementar sistemas existentes. PostgreSQL pode continuar sendo o núcleo transacional; um object storage pode servir como repositório de arquivos; uma fila pode desacoplar ingestão; e um índice de busca pode atender recuperação. Não há benefício em migrar tudo para uma arquitetura distribuída sem um problema mensurado.

## 8. Arquitetura de referência para o MX

```text
Cliente web/mobile
      -> API autenticada
      -> serviço de conversação e autorização
      -> PostgreSQL transacional
      -> fila de tarefas e eventos
      -> workers de ingestão, OCR, chunking e embeddings
      -> armazenamento de objetos versionado
      -> índice lexical/vetorial
      -> recuperação e geração com evidências
      -> telemetria, avaliação e controle de custo
```

### 8.1 Autosserviço governado

Ambientes de desenvolvimento, avaliação e produção devem ser separados. Cada recurso precisa de tags de projeto, ambiente, proprietário, finalidade, classe de dados, orçamento e data de expiração. O pipeline de infraestrutura deve criar e destruir recursos de maneira reproduzível.

### 8.2 Elasticidade controlada

O worker deve consumir da fila com concorrência limitada. O autoscaling deve considerar tamanho da fila, idade do evento, utilização de CPU, memória, limite de API e custo máximo. O produtor deve aplicar backpressure quando o downstream não acompanhar.

### 8.3 Medição e FinOps

O MX deve exibir custo por etapa e por unidade de valor. Exemplos: custo por PDF processado, custo por página OCR, custo por embedding, custo por resposta, armazenamento por coleção e egress por período. Alertas devem bloquear ou reduzir cargas de experimento quando o orçamento for excedido.

### 8.4 Continuidade e recuperação

O PostgreSQL deve possuir backup testado, o object storage deve ter versionamento e retenção adequados, e os índices devem ser recriáveis a partir das fontes. O plano de recuperação deve indicar dependências, ordem de restauração, credenciais, migrações e verificação de integridade.

### 8.5 Segurança e responsabilidade compartilhada

O provedor protege partes da infraestrutura, mas a equipe protege identidade, configuração, dados, código, chaves, permissões, endpoints e políticas. A arquitetura deve usar menor privilégio, segregação de contas, criptografia em trânsito e repouso, rotação de segredos, logs de auditoria e validação contra vazamento.

## 9. Método para decidir entre local, nuvem e híbrido

A decisão pode ser estruturada em uma matriz de requisitos:

| Fator | Pergunta | Indicador |
|---|---|---|
| Carga | A demanda é variável ou constante? | curva de utilização e picos |
| Dados | Há restrição de residência ou sensibilidade? | classificação e política |
| Latência | O processamento precisa estar próximo da fonte? | p95 e distância de rede |
| Custo | Qual é o custo recorrente e de migração? | TCO em 12–36 meses |
| Operação | A equipe consegue administrar o ambiente? | competências e horas disponíveis |
| Portabilidade | É possível sair ou alternar de fornecedor? | exportação e reimplantação testadas |
| Continuidade | Qual RPO/RTO é necessário? | testes de restauração |
| Valor | Qual resultado será melhor? | métrica de negócio ou produto |

A assertiva técnica é: **escolha a menor arquitetura que satisfaz requisitos de escala, risco e valor; aumente a complexidade somente quando as medições justificarem**.

## 10. Exercícios autorais e gabaritos

### Exercício 1 — classificação de serviço

O MX precisa executar uma API em Java, gerenciar seus próprios containers e usar um banco gerenciado. O modelo principal é IaaS, PaaS ou SaaS?

**Gabarito orientativo:** depende do nível efetivo de controle. Se a equipe administra máquinas virtuais e containers, o componente de execução se aproxima de IaaS. Se o provedor gerencia runtime e containers por uma plataforma, aproxima-se de PaaS. O banco pode ser um serviço gerenciado separado. A classificação deve ser feita por componente, não pelo sistema inteiro.

### Exercício 2 — estimativa de TCO

Compare um cluster sempre ligado com workers elásticos para uma carga que ocorre em dois picos de quatro horas por dia. Quais dados precisam ser coletados?

**Gabarito orientativo:** medir duração e intensidade dos picos, custo por hora, tempo de inicialização, armazenamento, tráfego, filas, observabilidade, operação, falhas e impacto da latência de provisionamento. O cluster fixo pode vencer se a utilização média for alta; workers elásticos podem vencer se a capacidade permanecer ociosa grande parte do tempo. O resultado deve incluir engenharia e confiabilidade.

### Exercício 3 — RPO e RTO

Defina RPO e RTO para documentos acadêmicos versionados e para mensagens transacionais da conversa.

**Gabarito orientativo:** documentos podem tolerar RPO maior se a fonte original puder ser reprocessada, enquanto mensagens transacionais normalmente exigem RPO menor por afetarem estado e continuidade da conversa. O RTO deve considerar dependências e uma resposta degradada. Os valores precisam ser acordados pelo impacto, não escolhidos apenas pela capacidade técnica.

### Exercício 4 — elasticidade e backpressure

A fila de embeddings cresce continuamente quando o banco vetorial começa a responder mais lentamente. Escalar os workers é sempre correto?

**Gabarito orientativo:** não. A causa pode estar no banco, na rede, em limite de API ou em contenção. Aumentar workers pode piorar o gargalo. Deve-se aplicar backpressure, observar lag, limitar concorrência, revisar particionamento, ajustar lotes e escalar o downstream somente quando houver capacidade comprovada.

### Exercício 5 — implantação híbrida

Dados sensíveis devem permanecer em ambiente controlado, mas cargas exploratórias podem usar serviços públicos. Proponha uma separação.

**Gabarito orientativo:** manter dados identificáveis e chaves no ambiente controlado; publicar somente dados anonimizados, agregados ou devidamente autorizados; usar conectividade segura, catálogo de linhagem, políticas de saída e reprocessamento auditável. A nuvem pública não deve receber dados sensíveis apenas por conveniência.

## 11. Checklist de domínio

| Competência | Critério de domínio |
|---|---|
| Conceito de nuvem | Explica recursos compartilhados, rede, autosserviço e medição |
| Relação com Big Data | Conecta elasticidade e escala sem confundir nuvem com Big Data |
| Benefícios | Analisa agilidade, custo, disponibilidade, continuidade e testes |
| TCO | Inclui rede, observabilidade, operação, backup e migração |
| IaaS/PaaS/SaaS | Diferencia fronteiras de controle e responsabilidade |
| Implantação | Compara pública, privada, comunidade e híbrida |
| Segurança | Aplica responsabilidade compartilhada e menor privilégio |
| MX | Define autoscaling, FinOps, backup, filas e arquitetura híbrida |

## 12. Limitações e atualização

As referências de fornecedores e serviços citadas no material são didáticas e devem ser verificadas antes de decisões de arquitetura, pois nomes de produtos, preços, APIs, regiões e capacidades mudam. A classificação do NIST é uma base conceitual; serviços atuais podem combinar características de mais de um modelo.

Benefícios como redução de custos, alta disponibilidade e continuidade não são garantidos pela simples contratação da nuvem. Eles dependem de desenho, configuração, testes e operação. A nuvem pode ampliar elasticidade e acesso, mas também amplia dependência de rede, superfície de ataque, complexidade de custos e risco de lock-in.

Este dossiê foi escrito originalmente a partir do PDF salvo no projeto. Não reproduz o material integral; reorganiza seus conceitos, acrescenta decisões de engenharia, contratos, métricas, cautelas e exercícios para integração com o MX.

## Referências

[1]: `BigData Aula3.pdf`, páginas 5, 8–16, material teórico salvo no projeto Estudos.

[2]: NIST. *The NIST Definition of Cloud Computing*. Special Publication 800-145. Referenciado no material como base dos modelos e características de nuvem.

[3]: IBM Cloud/Bluemix, Microsoft Azure, Amazon Web Services e Google Cloud, referências de provedores indicadas no PDF nas páginas 16–17, com endereços abreviados no arquivo-fonte.

[4]: Material complementar indicado no PDF: Linux, Dell/EMC, Microsoft, IBM, Azure e Salesforce, páginas 13–17 do arquivo-fonte.
