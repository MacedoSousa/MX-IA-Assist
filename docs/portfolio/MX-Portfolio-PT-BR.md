# MX — Universo Local de Inteligência, Cuidado e Produtividade

**Documentação técnica e portfólio profissional — Português (Brasil)**
**Autor:** Macedo Sousa
**Projeto:** `MacedoSousa/MX-IA-Assist`
**Data da evidência principal:** 20 de agosto de 2026
**Licença e acesso:** repositório privado, ambiente executado localmente no Windows

> O MX é um assistente pessoal local, inspirado no conceito de “Jarvis”, cujo princípio arquitetural é manter o usuário conectado a um único núcleo inteligente. O MX Core interpreta a intenção, seleciona uma skill especialista, aplica políticas de segurança, coordena tools autorizadas e entrega uma resposta pelo canal central.

## 1. Resumo executivo

O MX foi concebido para resolver um problema comum em ambientes de desenvolvimento e produtividade: a existência de muitas ferramentas, agentes, interfaces e fontes de conhecimento sem uma camada central de decisão, segurança e observabilidade. Em vez de expor diversos agentes diretamente ao usuário, o projeto estabelece o **MX Core como ponto único de comunicação**. As skills são especialistas internas, substituíveis e governadas por contratos explícitos.

A solução combina **Java 21, Spring Boot 4, Clean Architecture, TDD, SOLID, PostgreSQL, Redis, Ollama, Expo Web/React Native e Docker Compose**. O modelo local atualmente validado é `qwen3:8b`. O processamento de linguagem permanece na máquina do usuário, reduzindo a dependência de serviços externos e permitindo que o MX atue sobre assuntos técnicos, qualidade de software, desenvolvimento e conversação geral.

O conceito do projeto pode ser apresentado como um pequeno universo operacional:

| Dimensão | Implementação no MX | Benefício demonstrado |
|---|---|---|
| Inteligência | MX Core, Ollama e skills especialistas | Interpretação centralizada e execução multiassunto |
| Cuidado | Segurança, aprovação humana, sessões revogáveis e sandbox | Ações sensíveis não são executadas silenciosamente |
| Produtividade | Cliente universal, tools internas, streaming SSE e automação Docker | Um único ponto de interação para tarefas técnicas |
| Confiabilidade | TDD, healthchecks, métricas Micrometer e runbook | Sistema verificável, operável e documentado |
| Autonomia responsável | Níveis de autonomia e PolicyEngine | Read-only automático; ações sensíveis aguardam aprovação |

## 2. Problema e visão

O problema original não era simplesmente “ter um chatbot”. O objetivo era construir uma camada pessoal de inteligência capaz de auxiliar programação, estudos, assuntos pessoais e tarefas operacionais, preservando privacidade e mantendo o controle humano sobre ações de risco. A visão do MX é evoluir de uma coleção de ferramentas para um **sistema integrado de inteligência, cuidado e produtividade**.

A decisão de tornar o MX Core o único ponto de comunicação evita que cada agente desenvolva sua própria API, sua própria autenticação ou sua própria política de segurança. A especialização acontece internamente: GeneralSkill trata conversação ampla, DevelopmentSkill lida com desenvolvimento e tools estruturadas, e QualitySkill incorpora a matéria de Qualidade de Software em uma base de conhecimento versionada.

## 3. Arquitetura

![Diagrama arquitetural do universo MX](../architecture/mx-universe.png)

A arquitetura segue uma separação explícita entre domínio, casos de uso e adapters. O cliente Expo não executa regras de negócio; ele autentica, envia mensagens, recebe streaming e consulta aprovações. O MX Core coordena o ciclo de execução e delega a especialização à skill adequada. O backend conversa com PostgreSQL, Redis e Ollama por ports/adapters, sem permitir que o modelo seja o ponto de autoridade para autorização.

| Camada | Responsabilidade | Exemplos no projeto |
|---|---|---|
| Interface | Transportar comandos e eventos | Controllers REST, SSE e Expo Web/Mobile |
| Aplicação | Orquestrar casos de uso | `MxCoreService`, `SendMessageUseCase`, runs e aprovação |
| Domínio | Regras e contratos estáveis | Skills, autonomia, PolicyEngine, tools e ExecutionRun |
| Infraestrutura | Implementar integrações | JPA/PostgreSQL, Redis, Ollama HTTP, métricas |
| Operação | Executar, observar e recuperar | Compose, healthchecks, launcher e tarefa Always-On |

### 3.1 Fluxo principal

1. O usuário envia uma mensagem pelo cliente universal.
2. O endpoint autenticado cria ou recupera a conversa e encaminha o pedido ao MX Core.
3. O roteador seleciona a skill especialista com base em intenção e contrato.
4. A skill consulta o gateway de modelo local e, quando necessário, produz uma chamada estruturada de tool.
5. O `ToolCallParser` aceita somente o marcador explícito `[MX_TOOL_CALL]` com JSON válido.
6. O `ToolRegistry` e o `PolicyEngine` validam nome, argumentos, allowlist e autonomia.
7. Tools read-only permitidas podem executar automaticamente; operações sensíveis geram `AWAITING_APPROVAL` com nonce hash, expiração e idempotência.
8. A resposta retorna pelo canal único REST ou SSE, sem expor segredos de infraestrutura.

### 3.2 Skills especialistas

| Skill | Papel | Conhecimento e autonomia |
|---|---|---|
| GeneralSkill | Conversação geral e encaminhamento | Responde assuntos amplos, detecta domínio e recusa instruções de prompt injection |
| DevelopmentSkill | Apoio ao desenvolvimento | Pode interpretar chamadas estruturadas e executar tools permitidas, priorizando read-only |
| QualitySkill | Qualidade de Software | ISO/IEC 25010, CMMI, métricas, testes, requisitos e base EAD versionada |

A QualitySkill não representa um fine-tuning irreversível. Ela utiliza uma base controlada em `docs/knowledge/qualidade-software.md`, que pode ser revisada, testada e versionada sem reconstruir o modelo. Essa escolha reduz custo operacional e aumenta a rastreabilidade do conhecimento utilizado.

## 4. Segurança por padrão

O projeto foi desenvolvido para não delegar segurança ao texto gerado pelo modelo. O modelo pode sugerir uma chamada, mas a execução depende de validações determinísticas no código.

| Controle | Evidência no projeto |
|---|---|
| JWT e refresh rotativo | Sessão persistida, refresh revogável e logout invalidante |
| Aprovação humana | `AWAITING_APPROVAL`, nonce SHA-256, expiração e idempotência |
| Allowlist de tools | `ToolRegistry`, `ToolDefinition` e `PolicyEngine` |
| Sandbox de workspace | Limite de bytes, bloqueio de symlink e escopo de diretório |
| Proteção contra prompt injection | Testes adversariais na GeneralSkill e no ToolExecutor |
| Segredo fora das evidências | Scripts E2E não gravam token, senha ou payload sensível |
| Canal único | Skills são internas e não expõem interfaces independentes ao usuário |

Quando uma operação sensível exige autorização, o MX não trata uma resposta textual como confirmação. O caso de uso cria um run persistente e devolve metadados mínimos para que o cliente possa sincronizar e aprovar. A continuação pós-aprovação permanece um ponto evolutivo separado, permitindo acrescentar reexecução idempotente sem enfraquecer o controle já implementado.

## 5. Stack Docker organizada

O ambiente principal foi organizado com nomes estáveis, portas documentadas, healthchecks e dados persistentes. O Compose usa volumes/bind mounts para que a recriação de containers não apague PostgreSQL, Redis, Ollama ou Open WebUI. Volumes Docker são projetados para armazenar dados persistentes independentemente do ciclo de vida do container [1] [2].

| Container | Imagem | Porta | Estado esperado | Dados |
|---|---|---:|---|---|
| `mx-core` | `mx-core:latest` | 8080 | Healthy | Backend, migrations e API |
| `mx-web` | `mx-web:latest` | 8082 | Up | Bundle Expo servido por Nginx |
| `mx-postgres` | `postgres:16` | 5432 | Healthy | Banco do MX |
| `mx-redis` | `redis:7` | 6379 | Healthy | Cache/sessões auxiliares com AOF |
| `mx-ollama` | `ollama/ollama:latest` | 11434 | Up | Modelos locais, incluindo `qwen3:8b` |
| `mx-open-webui` | `ghcr.io/open-webui/open-webui:main` | 3000 | Healthy | Interface auxiliar de modelos locais |

O serviço `medsync`, o servidor `deceasedCraft`, os containers de Minecraft, TeamSpeak, `carpg-server`, `core-service` e os artefatos de Compose solicitados foram tratados como recursos preservados. O cleanup aplicado removeu somente containers de teste parados com nomes automáticos, imagens sem uso e cache de build. **Nenhum volume ou rede foi removido.**

### 5.1 Evidência da limpeza segura

A execução registrou `Total reclaimed space: 14.56GB`. Depois da limpeza, o inventário registrou 15 imagens em uso, 15 containers catalogados, 19 volumes locais e os serviços MX, MedSync, Minecraft, TeamSpeak, carpg, deceasedCraft e core-service preservados. A prova completa está em:

- `docs/evidence/docker-inventory/containers-after-cleanup.txt`
- `docs/evidence/docker-inventory/images-after-cleanup.txt`
- `docs/evidence/docker-inventory/volumes-after-cleanup.txt`
- `docs/evidence/docker-inventory/disk-usage-after-cleanup.txt`
- `scripts/cleanup-docker.ps1`

O script possui modo simulação por padrão. A limpeza só ocorre com `-Apply`, e a política explicitamente não executa `docker volume prune` nem `docker network prune`.

## 6. Operação Always-On e persistência

No Windows, a tarefa `MX-AI-Assistant-AlwaysOn` executa o launcher de forma idempotente. O launcher aguarda o Docker Desktop, inicia ou reconcilia o Compose sem rebuild desnecessário e não interrompe os demais servidores. A tarefa foi executada com sucesso, com `LastTaskResult = 0` e zero execuções perdidas.

“Always-on” não significa que a memória RAM física nunca possa ser liberada pelo sistema operacional. Significa que o MX é iniciado automaticamente e que seus **dados, modelos, sessões, conversas e configurações** permanecem em armazenamento persistente. Para evitar perda de memória lógica, o Compose usa volumes persistentes, Redis com AOF e PostgreSQL com dados fora do ciclo efêmero do container.

| Mecanismo | Objetivo |
|---|---|
| `restart: unless-stopped` | Recuperar containers após falha ou reinício do daemon |
| Tarefa do Agendador | Iniciar o MX após logon/inicialização do Windows |
| Bind mounts persistentes | Manter banco, cache, modelos e configurações |
| Healthchecks | Evitar que o web suba antes do Core estar saudável |
| AOF do Redis | Reduzir perda de estado em reinícios |
| Runbook | Permitir recuperação manual previsível |

A instalação e remoção da tarefa são controladas por `scripts/install-mx-always-on.ps1`. A instalação é idempotente e não modifica os serviços preservados.

## 7. Provas e execuções reproduzíveis

### 7.1 Healthcheck do stack

A execução de `scripts/verify-mx-stack.ps1` retornou `Overall result: PASS` em 20/08/2026. Os endpoints responderam com HTTP 200:

| Probe | Resultado |
|---|---:|
| `http://localhost:8080/actuator/health` | 200 / PASS |
| `http://localhost:8082/health` | 200 / PASS |
| `http://localhost:11434/api/tags` | 200 / PASS |
| `http://localhost:3000/health` | 200 / PASS |

O relatório está em `docs/evidence/executions/mx-stack-verification.md` e seu JSON correspondente permite auditoria automatizada.

### 7.2 TDD backend

A suíte executada em container Maven/JDK finalizou com código 0. O relatório sumarizado registrou:

| Métrica | Resultado |
|---|---:|
| Arquivos de relatório | 30 |
| Testes | 79 |
| Falhas | 0 |
| Erros | 0 |
| Ignorados | 0 |
| Resultado | PASS |

A execução bruta está em `docs/evidence/executions/maven-test-run.txt`; o resumo está em `docs/evidence/executions/tdd-summary.md` e `tdd-summary.json`. O comando utilizado foi:

```powershell
docker run --rm `
  -v D:\MX\core-service:/app `
  -v D:\MX\maven-test-target:/app/target `
  -v C:\Windows\Temp\mx-m2:/root/.m2/repository `
  -w /app `
  maven:3.9-eclipse-temurin-21 mvn test
```

### 7.3 Fluxo E2E

O fluxo versionado `scripts/e2e-auth-chat.ps1` registra usuário descartável, executa login, confirma sessão JWT e envia uma mensagem pelo endpoint central. O script mascara tokens e não grava credenciais. O resultado esperado é `chatStatus=COMPLETED`, com resposta processada pelo `qwen3:8b` local.

### 7.4 Diagrama e registro visual

O diagrama arquitetural em `docs/architecture/mx-universe.png` é um artefato visual renderizado a partir de `mx-universe.mmd`. Ele mostra o cliente único, o MX Core, skills, tools, persistência e serviços preservados. A documentação também incorpora logs de terminal, inventários, relatórios JSON e a execução E2E como provas rastreáveis.

## 8. Uso do sistema

No computador Windows, execute:

```powershell
cd D:\MX
.\start.bat
```

A interface principal fica disponível em `http://localhost:8082`. Para acessar de um celular na mesma rede, utilize `http://<IP-LAN-DO-COMPUTADOR>:8082`. O launcher injeta a URL LAN da API no bundle web e habilita CORS no perfil de desenvolvimento. O firewall do Windows deve permitir a porta 8082 na rede privada.

O usuário conversa com o MX Core, e não diretamente com GeneralSkill, DevelopmentSkill ou QualitySkill. A aprovação pode ser consultada pelo painel/sincronização de runs e os eventos SSE normais incluem `started`, `token`, `completed` e `approval_required`.

## 9. Como apresentar o projeto em uma candidatura

O MX demonstra mais do que uma integração com LLM. Ele demonstra pensamento de produto, arquitetura limpa, governança de agentes, TDD, segurança operacional e preocupação com experiência multiplataforma. Uma narrativa profissional possível é:

> “Construí um assistente pessoal local com Java 21 e Spring Boot, organizado em Clean Architecture, em que um núcleo central coordena skills especialistas. O sistema roda com Ollama local, possui autenticação JWT com sessões persistidas, aprovação humana para operações sensíveis, streaming SSE, cliente Expo universal e operação Docker always-on. A qualidade foi tratada como parte do produto: há 79 testes automatizados aprovados, métricas, healthchecks, runbook e evidências reproduzíveis.”

A ideia de “universo de inteligência, cuidado e produtividade” resume três escolhas de engenharia. Inteligência é a capacidade local e multiassunto; cuidado é segurança, aprovação e preservação de dados; produtividade é a interface única, as tools e a automação operacional. Essa formulação é adequada para entrevista porque conecta motivação pessoal a decisões técnicas verificáveis.

## 10. Limitações conhecidas e próximos incrementos

O sistema está disponível para uso local, mas ainda é um projeto em evolução. O processo posterior à aprovação humana deve ser ampliado para reexecutar a operação aprovada com idempotência e registrar o resultado final do tool call. O acesso via celular depende da rede local e das regras do firewall. O always-on depende de o Windows e o Docker Desktop estarem ligados; não equivale a alta disponibilidade de produção.

Também é recomendável evoluir os backups para uma rotina agendada com retenção, testar restauração em máquina separada, adicionar observabilidade histórica e validar o aplicativo em dispositivos Android e iOS físicos. Essas melhorias são incrementais e não invalidam o núcleo atualmente executável.

## 11. Referências externas

[1]: https://docs.docker.com/reference/compose-file/services/ "Docker Compose services"

[2]: https://docs.docker.com/reference/compose-file/volumes/ "Docker Compose volumes"

[3]: https://docs.spring.io/spring-boot/reference/actuator/endpoints.html "Spring Boot Actuator endpoints"

[4]: https://docs.ollama.com/api/introduction "Ollama API introduction"

[5]: https://docs.expo.dev/deploy/web/ "Expo web deployment"

## 12. Artefatos principais

| Artefato | Finalidade |
|---|---|
| `start.bat` | Launcher Docker-only e operação local |
| `scripts/install-mx-always-on.ps1` | Instalação da tarefa Always-On |
| `scripts/verify-mx-stack.ps1` | Healthchecks e provas do ambiente |
| `scripts/cleanup-docker.ps1` | Limpeza segura com dry-run |
| `scripts/e2e-auth-chat.ps1` | Registro de execução E2E sem segredos |
| `scripts/summarize-tdd.ps1` | Sumário dos relatórios Surefire |
| `infrastructure/docker/compose/docker-compose.yml` | Orquestração dos serviços |
| `docs/architecture/mx-universe.png` | Diagrama visual |
| `docs/knowledge/qualidade-software.md` | Base de conhecimento da QualitySkill |
| `docs/evidence/` | Provas de inventário e execução |
