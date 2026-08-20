# MX — Arquitetura Clean Architecture

**Versão:** 1.0
**Status:** proposta adotada para a evolução incremental
**Objetivo:** estabelecer limites claros entre regras de negócio, casos de uso, interfaces externas e infraestrutura, permitindo evoluir o MX sem transformar o núcleo em um monólito acoplado a Spring, PostgreSQL, Redis, Ollama ou à interface web.

## 1. Decisão arquitetural

O MX será implementado inicialmente como um **monólito modular**, organizado por domínio e protegido por Clean Architecture. A decisão é deliberada: o sistema precisa de baixa latência, depuração simples, consistência transacional e velocidade de desenvolvimento local. A extração futura de módulos para serviços independentes somente ocorrerá quando houver uma necessidade concreta de escala, isolamento ou ciclo de implantação.

> **Regra de dependência:** as dependências do código apontam para dentro. O domínio não conhece Spring, JPA, HTTP, JSON, Redis, Ollama, Docker, filesystem ou qualquer biblioteca de infraestrutura.

A arquitetura usa quatro círculos. O domínio contém as regras e invariantes; a aplicação coordena casos de uso; os adaptadores convertem protocolos externos em contratos internos; e a infraestrutura fornece implementações concretas.

```text
                 +--------------------------------------+
                 |          Frameworks / Drivers        |
                 | Spring, JPA, PostgreSQL, Redis,      |
                 | Ollama, filesystem, HTTP, frontend   |
                 +-------------------+------------------+
                                     |
                 +-------------------v------------------+
                 |           Interface Adapters         |
                 | REST controllers, DTOs, repositories  |
                 | clients, mappers, tool adapters       |
                 +-------------------+------------------+
                                     |
                 +-------------------v------------------+
                 |              Application              |
                 | use cases, ports, policies, commands  |
                 | orchestration, transactions, errors   |
                 +-------------------+------------------+
                                     |
                 +-------------------v------------------+
                 |                Domain                 |
                 | entities, value objects, invariants,  |
                 | domain services and domain events      |
                 +--------------------------------------+
```

## 2. Estrutura de pacotes

A estrutura deve ser organizada por **feature primeiro** e por camada depois. Isso evita pacotes globais gigantes como `service`, `controller` e `util`, nos quais regras de vários domínios acabam misturadas.

```text
com.macedxs.mx
├── shared
│   ├── domain
│   │   ├── DomainException.java
│   │   ├── PageQuery.java
│   │   └── Clock.java
│   ├── application
│   │   ├── UseCase.java
│   │   └── UnitOfWork.java
│   └── adapters
│       └── web
│
├── identity
│   ├── domain
│   │   ├── User.java
│   │   ├── UserId.java
│   │   ├── Role.java
│   │   └── UserRepository.java
│   ├── application
│   │   ├── RegisterUserUseCase.java
│   │   ├── AuthenticateUserUseCase.java
│   │   ├── RevokeSessionUseCase.java
│   │   └── ports
│   │       ├── PasswordHasher.java
│   │       ├── TokenIssuer.java
│   │       └── AuditPort.java
│   ├── adapters
│   │   └── web
│   └── infrastructure
│       ├── persistence
│       └── security
│
├── conversation
│   ├── domain
│   │   ├── Conversation.java
│   │   ├── Message.java
│   │   └── ConversationRepository.java
│   ├── application
│   │   ├── SendMessageUseCase.java
│   │   └── ports
│   │       └── ModelGateway.java
│   ├── adapters
│   │   └── web
│   └── infrastructure
│       └── persistence
│
├── task
│   ├── domain
│   ├── application
│   ├── adapters
│   └── infrastructure
│
├── agent
│   ├── domain
│   │   ├── AgentDefinition.java
│   │   ├── AgentCapability.java
│   │   └── AgentRun.java
│   ├── application
│   │   ├── ExecuteAgentUseCase.java
│   │   └── ports
│   ├── adapters
│   └── infrastructure
│
├── tool
│   ├── domain
│   │   ├── ToolDefinition.java
│   │   ├── ToolPermission.java
│   │   └── ToolResult.java
│   ├── application
│   │   ├── ExecuteToolUseCase.java
│   │   ├── ToolRegistry.java
│   │   └── ToolPolicy.java
│   ├── adapters
│   └── infrastructure
│
└── bootstrap
    ├── WebConfiguration.java
    ├── PersistenceConfiguration.java
    ├── AiConfiguration.java
    └── SecurityConfiguration.java
```

A migração não será feita por uma reescrita total. Os pacotes existentes podem continuar funcionando durante a transição, mas cada novo caso de uso deve obedecer à estrutura acima. Os componentes antigos serão movidos apenas quando houver testes que protejam seu comportamento.

## 3. Responsabilidade de cada camada

| Camada | Pode conhecer | Não pode conhecer | Responsabilidade |
|---|---|---|---|
| Domínio | Java padrão e abstrações próprias | Spring, JPA, HTTP, JSON, Ollama, Redis | Regras, invariantes, estados e objetos de negócio |
| Aplicação | Domínio e portas | Implementações concretas de banco, modelo ou web | Orquestrar casos de uso, validar fluxo, autorizar e definir transações |
| Adaptadores | Aplicação e contratos externos | Regras de negócio duplicadas | Converter HTTP/JSON/JPA/respostas externas para os contratos internos |
| Infraestrutura | Todas as camadas necessárias para composição | Decisões de negócio espalhadas em configurações | Implementar portas e montar o grafo de dependências |

Controllers devem ser finos. Repositories JPA devem ser detalhes de infraestrutura. O caso de uso deve receber um comando, executar o fluxo e retornar um resultado de aplicação. A decisão sobre permissão deve ocorrer no backend, antes da execução da ferramenta.

## 4. Contratos centrais

### 4.1 Gateway de modelo

O caso de uso não chamará Ollama diretamente. A aplicação dependerá de uma porta estável:

```java
public interface ModelGateway {
    ModelResponse complete(ModelRequest request);
}
```

`ModelRequest` deve conter mensagens, modelo opcional, temperatura, limite de tokens, instrução de sistema e modo de resposta. `ModelResponse` deve conter texto, modelo efetivamente usado, duração, tokens quando disponíveis e metadados. A implementação inicial será `OllamaModelGateway` na infraestrutura. Um `FakeModelGateway` será usado nos testes unitários.

### 4.2 Persistência

Cada agregado terá uma porta de repositório no domínio ou na aplicação, conforme a necessidade do caso de uso:

```java
public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findOwnedById(ConversationId id, UserId ownerId);
}
```

A implementação JPA não será exposta para o domínio. Entidades JPA, mapeadores e detalhes de UUID ficarão na infraestrutura de persistência.

### 4.3 Ferramentas

Uma ferramenta é uma capacidade limitada, não um comando arbitrário do modelo:

```java
public interface Tool {
    ToolName name();
    ToolDefinition definition();
    ToolResult execute(ToolInput input, ExecutionContext context);
}
```

Todo executor deve receber um `ExecutionContext` com usuário, workspace autorizado, nível de autonomia, prazo, correlation ID e permissões. A aplicação verifica a política antes de chamar `execute`. Ferramentas destrutivas nunca serão habilitadas apenas porque o modelo as solicitou.

### 4.4 Auditoria

A aplicação dependerá de uma porta `AuditPort`. O adaptador persistirá eventos como login, logout, execução de agente, aprovação, recusa e execução de ferramenta. Auditoria não deve depender apenas de logs de texto, pois precisa ser consultável e associada ao usuário e à execução.

## 5. Primeiro caso de uso vertical

O primeiro incremento será `SendMessageUseCase`, com o fluxo abaixo:

```text
Receive SendMessageCommand
        ↓
Validate user and prompt
        ↓
Load or create owned conversation
        ↓
Persist user message
        ↓
Build bounded model context
        ↓
Call ModelGateway
        ↓
Persist assistant message
        ↓
Return SendMessageResult
```

Nesta primeira fatia, o caso de uso **não executará ferramentas** e **não tentará coordenar múltiplos agentes**. O gateway de modelo será uma porta, permitindo provar o fluxo com TDD e depois conectar Ollama sem alterar o núcleo.

O segundo incremento adicionará `IntentRouter` e `AgentRegistry`. O terceiro adicionará o primeiro agente especializado e apenas ferramentas de leitura. Assim, cada nova capacidade entrará sobre uma base já testada.

## 6. Regras TDD

O ciclo padrão será **Red, Green, Refactor**. Primeiro será escrito um teste que expresse uma regra ou comportamento observável; depois será criada a menor implementação para fazê-lo passar; por fim o código será refatorado sem alterar o comportamento.

Os testes unitários do domínio e da aplicação não devem subir Spring, banco, Docker ou Ollama. Esses testes usarão fakes e spies explícitos. Testes de adaptadores verificarão mapeamento HTTP e persistência. Testes de contrato verificarão que o adaptador Ollama converte corretamente requisições e respostas. Testes de integração serão reservados para o grafo real de componentes.

| Tipo de teste | Escopo | Dependências permitidas | Meta inicial |
|---|---|---|---|
| Domínio | Entidades, valores e invariantes | Nenhuma externa | Cobertura de regras críticas |
| Aplicação | Casos de uso e políticas | Fakes das portas | Fluxos felizes, falhas e idempotência |
| Adaptador | REST, JPA, Ollama | Componentes locais controlados | Contratos e mapeamentos |
| Integração | Spring + PostgreSQL/Redis | Containers de teste ou ambiente controlado | Migrations, transações e segurança |
| E2E | Fluxo do usuário | Stack completa | Poucos cenários críticos |

A cobertura percentual será um indicador auxiliar, não o objetivo. O objetivo é que cada requisito importante tenha um teste que falhe quando o comportamento for quebrado.

## 7. Aplicação prática de SOLID

**Responsabilidade única** significa que autenticação, emissão de token, persistência e auditoria não ficarão em uma única classe. **Aberto/fechado** será aplicado por portas e registros, permitindo adicionar um novo `ModelGateway` ou `Tool` sem alterar o caso de uso. **Substituição de Liskov** exige que fakes e implementações reais respeitem o mesmo contrato. **Segregação de interfaces** evita uma porta gigante de infraestrutura. **Inversão de dependência** mantém a aplicação dependente de abstrações próprias, enquanto Spring injeta implementações na borda.

Não será aplicado SOLID como criação automática de dezenas de interfaces. Uma abstração somente será criada quando representar uma fronteira real, permitir teste isolado ou proteger uma decisão que pode variar.

## 8. Transações, erros e observabilidade

As transações serão curtas e delimitadas. Chamadas de rede para o modelo ou para ferramentas externas não devem manter transação de banco aberta. Falhas serão representadas por erros de aplicação estáveis, com `correlationId`, estado da execução e mensagem segura para o usuário.

O sistema separará logs técnicos de auditoria. Cada requisição terá correlation ID; cada execução de agente terá run ID; cada chamada a ferramenta terá tool execution ID. Métricas iniciais: latência do caso de uso, latência do modelo, tokens quando disponíveis, falhas por tipo e tempo de execução de ferramentas.

## 9. Segurança por padrão

O usuário e o tenant, quando aplicável, serão parte explícita do contexto de aplicação. Repositories de leitura devem receber o proprietário ou o contexto autorizado, evitando buscar dados por ID sem isolamento. JWT, sessão, revogação, roles e auditoria serão tratados como requisitos funcionais, não como detalhes posteriores.

O nível inicial de autonomia será conservador: perguntas podem ser respondidas; ferramentas somente leitura exigem política compatível; escrita, shell, Git mutável, rede externa e operações destrutivas exigem aprovação. Prompt do usuário, conteúdo recuperado e instruções de ferramenta serão tratados como dados separados, nunca como autoridade para burlar a política do sistema.

## 10. Definition of Done

Um incremento só será considerado concluído quando possuir requisito e critério de aceitação registrados, testes escritos antes ou junto da implementação, código revisado contra as regras de dependência, tratamento de falhas, logs mínimos, documentação atualizada e execução local reproduzível. Não será usado o rótulo “pronto” apenas porque o código compila.

## 11. Decisões de evolução

A primeira versão manterá Java 21, Spring Boot existente, PostgreSQL, Flyway, Redis somente onde houver necessidade comprovada e Ollama atrás de `ModelGateway`. O frontend atual será mantido como adaptador temporário. A extração para microserviços, embeddings, MinIO, GitHub, Cloudflare Tunnel e agentes cooperativos será feita apenas depois de o primeiro fluxo vertical estar estável.


## 12. Estado da implementação em agosto de 2026

A arquitetura descrita neste documento já possui uma fatia vertical mais avançada do que o primeiro fluxo originalmente planejado. O `MxCoreService` é o ponto central atual; `ExecutionRun` acompanha o lifecycle; `StreamingModelGateway` isola o streaming do Ollama; a composição registra as skills `general` e `development`; e o cliente Expo consome a API v1 para web, Android e iOS.

A autenticação atual usa sessão persistida, JWT com `sessionId`, refresh rotativo com hash SHA-256 e logout revogável. A API expõe consulta, aprovação e rejeição de runs. O `PolicyEngine` e o `ToolExecutor` já protegem as tools, incluindo `workspace.list` e `workspace.write_file` sandboxed. A ponte que transforma automaticamente `REQUIRE_APPROVAL` em um `ExecutionRun` persistido em `AWAITING_APPROVAL` permanece como a próxima evolução da aplicação.

Os pacotes legados ainda presentes na árvore devem ser tratados como compatibilidade ou migração gradual. Novos casos de uso devem seguir portas explícitas, DTOs próprios, escopo por usuário e testes isolados; não devem reintroduzir chamadas diretas de controller para JPA, Ollama ou filesystem.
