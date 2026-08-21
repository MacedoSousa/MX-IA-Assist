# Biblioteca multidisciplinar — sínteses e exercícios autorais v2

> Material original para estudo interno da squad. Não reproduz conteúdo protegido de plataformas externas.

## 1. Bancos de dados e persistência

### Síntese
A escolha de persistência deve partir do modelo de acesso, consistência, volume, retenção, disponibilidade, observabilidade e custo. PostgreSQL é adequado quando transações, integridade referencial e consultas relacionais são centrais. Redis é adequado para cache, sessões, locks e estruturas de baixa latência, desde que a perda controlada de dados seja aceitável. MongoDB pode atender documentos flexíveis, mas não elimina a necessidade de modelar consultas, índices e consistência.

### Exercício autoral
Modele a persistência de conversas do assistente com histórico, mensagens, ferramentas utilizadas, versões de prompt e referências documentais. Indique as entidades, índices e política de retenção.

### Gabarito orientativo
Uma solução pode separar `conversation`, `message`, `tool_execution`, `prompt_version` e `citation`. Índices devem refletir consultas por usuário, conversa e timestamp. Mensagens podem ser particionadas ou arquivadas por período. O texto completo e os logs operacionais devem possuir políticas de retenção diferentes, e dados sensíveis devem ser protegidos ou pseudonimizados.

## 2. Testes e qualidade

### Síntese
Qualidade é uma propriedade observável por diferentes níveis de teste. Testes unitários verificam regras isoladas; integração verifica componentes reais; contrato verifica compatibilidade; carga verifica comportamento sob volume; segurança verifica abuso e exposição; avaliação de IA verifica relevância, fundamentação, segurança e custo.

### Exercício autoral
Defina uma pirâmide de testes para uma API Spring que chama um modelo de linguagem, consulta um índice vetorial e executa ferramentas autorizadas.

### Gabarito orientativo
A base deve conter testes unitários de regras e políticas. Em seguida, testes de integração para banco, índice e adaptador de modelo falso. Testes de contrato devem validar schemas. Testes de avaliação devem usar casos versionados e respostas esperadas por critérios. Testes de carga e segurança devem ocorrer antes de uma release crítica, sem chamar serviços reais de forma descontrolada.

## 3. Kubernetes, Helm e releases

### Síntese
Kubernetes orquestra workloads, mas não substitui arquitetura, segurança ou observabilidade. Helm deve parametrizar configurações sem esconder dependências críticas. Releases precisam de estratégia de promoção, healthchecks, limites de recursos, migrações reversíveis quando possível, monitoramento e rollback.

### Exercício autoral
Projete os componentes mínimos para executar o gateway do assistente, um worker de mensageria e um serviço de inferência em Kubernetes.

### Gabarito orientativo
Os componentes podem ser Deployments separados, Services internos, ConfigMaps para configuração não sensível, Secrets ou um gerenciador externo de segredos, probes de liveness/readiness, requests/limits, autoscaling condicionado a métricas e uma política de rollout. O worker deve ter idempotência e o serviço de inferência deve possuir limite de concorrência e timeout.

## 4. Redes, DNS e servidores

### Síntese
Diagnóstico de infraestrutura deve seguir camadas: processo, porta, firewall local, rota, DNS, proxy, TLS, serviço remoto e aplicação. DNS resolve nomes, mas não garante conectividade. Uma rota correta não garante que o firewall permita o tráfego. Windows e Linux possuem ferramentas diferentes, mas o raciocínio de isolamento de falhas é comum.

### Exercício autoral
Uma aplicação resolve o domínio, mas não consegue acessar a API em HTTPS. Liste uma sequência de diagnóstico sem alterar produção de forma arriscada.

### Gabarito orientativo
Verificar resolução com `nslookup` ou `dig`, rota com ferramentas do sistema, conectividade TCP, firewall local, proxy, negociação TLS, certificado, porta exposta e logs do serviço. A investigação deve registrar timestamps, origem, destino, porta, erro observado e comando executado. Alterações devem ocorrer primeiro em ambiente controlado.

## 5. Frontend, mobile e UX/UI

### Síntese
Interfaces de IA precisam comunicar estado, incerteza, progresso, erro, fonte e possibilidade de correção. Design responsivo e acessível exige semântica, contraste, foco de teclado, leitores de tela, feedback não visual e tratamento de estados offline. Mobile deve considerar permissões, sincronização, cache, conectividade intermitente e diferenças entre plataformas.

### Exercício autoral
Desenhe os estados de uma tela de chat com RAG: inicial, carregando, resposta parcial, resposta concluída com citações, erro, ausência de conexão e confirmação de ferramenta.

### Gabarito orientativo
Cada estado deve ter feedback claro e ação possível. A resposta parcial deve indicar que está em progresso; a resposta concluída deve exibir referências; o erro deve explicar recuperação; a ferramenta deve solicitar confirmação quando houver efeito externo; e o modo offline deve informar o que pode ser feito localmente sem prometer sincronização inexistente.

## 6. Kanban e gestão técnica

### Síntese
Kanban limita trabalho em progresso, torna o fluxo visível e usa métricas como lead time, throughput, idade dos itens e taxa de bloqueio. O objetivo não é ocupar todas as pessoas, mas melhorar previsibilidade e reduzir gargalos. Finanças técnicas devem relacionar custo, risco, valor e capacidade.

### Exercício autoral
Uma equipe possui 20 itens iniciados, poucos concluídos e muitos bloqueios. Proponha três ações e duas métricas para verificar melhoria.

### Gabarito orientativo
Reduzir o limite de trabalho em progresso, explicitar políticas de entrada e saída, atacar o gargalo e separar bloqueios visíveis são ações adequadas. Lead time e throughput devem ser acompanhados junto da idade dos itens bloqueados. A análise não deve usar velocidade individual como mecanismo de punição.

## 7. Programação e análise crítica estrutural

### Síntese
A matriz de linguagens deve distinguir paradigma, tipos, gerenciamento de memória, concorrência, compilação ou interpretação, ecossistema e interoperabilidade. Clipper, Java, Python, JavaScript, C, C++, Go, Rust, Kotlin, Swift, Dart, SQL, Bash, PowerShell e outras linguagens têm contextos distintos. A melhor linguagem depende do problema, do legado, da equipe, do risco e do custo de operação.

### Exercício autoral
Avalie uma proposta para reescrever um sistema legado Clipper diretamente em microserviços Java. Liste riscos e uma estratégia de migração gradual.

### Gabarito orientativo
Riscos incluem perda de regras implícitas, divergência de cálculos, migração de dados, incompatibilidade de relatórios, observabilidade insuficiente e aumento operacional. Uma estratégia melhor é inventariar regras, criar testes de caracterização, encapsular o legado, definir contratos, migrar um domínio de baixo risco, comparar resultados e só então ampliar o escopo.

## Critérios comuns de avaliação

Toda resposta deve apresentar premissas, alternativas, riscos, decisão, evidência e plano de reversão. Quando houver cálculo, registrar fórmula, unidade, dados de entrada, arredondamento e sensibilidade. Quando houver segurança, registrar ameaça, controle, evidência e impacto residual.
