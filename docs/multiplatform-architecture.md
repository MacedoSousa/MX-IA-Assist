# MX — Arquitetura Multiplataforma

**Status:** decisão arquitetural inicial para implementação local

**Escopo:** tornar o MX acessível por navegador web e por aplicativos Android/iOS sem duplicar o núcleo de inteligência, segurança ou execução.

## 1. Decisão central

O **MX Core continua sendo o único cérebro do produto**. Web, Android e iOS são canais de apresentação e interação; eles não possuem roteamento de skills, execução de tools, regras de autonomia ou acesso direto ao Ollama. Toda solicitação entra por uma API versionada, recebe autenticação e correlação, passa pelo MX Core e retorna como resposta ou estado de execução.

> O cliente pode exibir, solicitar e aprovar. Somente o MX Core pode decidir, executar e registrar.

A primeira implementação usará o backend Spring Boot existente como fonte de verdade. Não serão criados três backends nem uma lógica paralela para mobile. A persistência de conversas, runs, aprovações, memórias e preferências será centralizada no backend local.

## 2. Canais do produto

| Canal | Localização proposta | Responsabilidade | Não pode fazer |
|---|---|---|---|
| API | `core-service` | Autenticar, receber comandos, roteiar, executar, persistir e expor estado | Renderizar interface ou decidir por conta própria |
| Web | `clients/mx-app/` via Expo web | Experiência compartilhada para desktop e navegador responsivo | Acessar banco, Ollama ou filesystem diretamente |
| Android/iOS | `clients/mx-app/` via Expo | Experiência nativa compartilhada, streaming, runs e aprovação quando a UI estiver conectada | Reimplementar skills ou executar tools locais sem política |
| Contratos | `docs/api/` e cliente gerado | Definir payloads, erros, estados e compatibilidade | Ser fonte paralela de regras de negócio |

A pasta `frontend/` permanece como interface estática legada. A fonte oficial atual é `clients/mx-app/`, uma aplicação Expo com React Native, TypeScript e React Native Web que compartilha contratos, sessão, chat e streaming entre desktop, Android e iOS.

## 3. API-first

A API será versionada sob `/api/v1`. Os controllers serão adaptadores finos; DTOs de entrada e saída não exporão entidades JPA. O contrato será documentado em OpenAPI e usado para gerar tipos e clientes dos canais.

### Recursos iniciais

| Recurso | Método | Objetivo |
|---|---|---|
| `/api/auth/login` | POST | Iniciar sessão |
| `/api/auth/refresh` | POST | Renovar access token |
| `/api/auth/logout` | POST | Revogar sessão |
| `/api/v1/conversations` | GET/POST | Listar e criar conversas |
| `/api/v1/conversations/{id}/messages` | GET/POST | Ler histórico e enviar mensagem |
| `/api/v1/runs/{id}` | GET | Consultar lifecycle de execução |
| `/api/v1/runs/{id}/events` | GET | Receber eventos por SSE quando disponível |
| `/api/v1/runs/{id}/approval` | POST | Aprovar ou rejeitar ação pendente |
| `/api/v1/health` | GET | Diagnóstico não sensível do serviço |

O envio de mensagem retorna imediatamente um `runId`, `correlationId`, `conversationId` e o estado inicial. A resposta pode ser obtida por consulta, SSE no web ou polling controlado no mobile. O cliente nunca deve presumir que uma requisição síncrona representa a conclusão de uma execução.

## 4. Modelo de estado compartilhado

Os canais devem reconhecer pelo menos os estados `RECEIVED`, `ROUTED`, `EXECUTING`, `AWAITING_APPROVAL`, `VERIFYING`, `COMPLETED`, `FAILED` e `CANCELLED`. A representação visual pode variar, mas o significado e as transições são definidos pelo backend.

Cada atualização carrega `runId`, `correlationId`, `status`, `skillName`, `pendingApproval`, `createdAt` e, quando aplicável, uma resposta segura ou código de erro. O frontend não recebe stack trace, segredo, prompt interno ou conteúdo de ferramenta além do necessário para o usuário decidir.

## 5. Estratégia web

O cliente web será uma aplicação React + TypeScript com Vite, responsiva e preparada para desktop. A tela principal será um workspace do MX com navegação lateral para conversas, execução atual, memória, skills disponíveis e configurações. A conversa ocupará o centro; o painel de execução mostrará skill selecionada, estado, duração, tools solicitadas e aprovações pendentes.

O web client usará um cliente HTTP tipado gerado do contrato OpenAPI, cache de servidor com TanStack Query ou equivalente e uma camada de estado local pequena. Não haverá chamadas diretas a endpoints espalhadas pelos componentes. A UI conhecerá apenas casos de uso do cliente, como `sendMessage`, `getConversation`, `getRun` e `approveRun`.

## 6. Estratégia Android e iOS

O cliente mobile será uma aplicação Expo com React Native, TypeScript, Expo Router e NativeWind. Android e iOS compartilharão o máximo possível de telas, hooks, contratos e casos de uso de cliente. Componentes de layout e navegação respeitarão safe areas, teclado, notificações e comportamento de toque.

A primeira versão mobile priorizará conversa, histórico, estado de run e aprovação. Streaming será implementado primeiro como polling incremental controlado; SSE ou websocket só entrarão quando o lifecycle persistido e a reconexão estiverem estáveis. Rascunhos e preferências podem ser armazenados localmente, mas a fonte de verdade das conversas e runs será o backend.

## 7. Autenticação por canal

A API deve usar uma sessão revogável, com access token de curta duração e refresh controlado. O web client armazenará a sessão em mecanismo seguro compatível com o navegador e seguirá a política de CSRF/CORS definida pelo backend. O mobile utilizará armazenamento seguro do sistema operacional para tokens, nunca armazenamento de texto simples.

A identidade corrente será resolvida no backend por uma porta `CurrentUserPort`. Toda query de conversa, run, memória, arquivo e aprovação será filtrada pelo proprietário autenticado. A URL, o `conversationId` ou o `runId` fornecidos pelo cliente nunca substituem a autorização do MX Core.

## 8. Desenvolvimento local

O backend continuará no `core-service`. O web client será executado em uma porta local separada e apontará para `MX_API_URL`. O mobile aceitará `EXPO_PUBLIC_API_URL`, com perfis para navegador, simulador iOS, emulador Android e dispositivo físico na rede local. Nenhuma URL de desenvolvimento será codificada em componente.

| Ambiente | Configuração esperada |
|---|---|
| Web | `http://localhost:<porta-do-core>` |
| iOS Simulator | `http://localhost:<porta-do-core>` |
| Android Emulator | Host local exposto pelo alias do emulador |
| Dispositivo físico | Endereço LAN da máquina que executa o MX |
| Produção local futura | HTTPS local ou proxy reverso com domínio controlado |

O script de diagnóstico deve verificar backend, banco, Ollama, porta da API, origem permitida e URL configurada nos clientes. O web e o mobile devem exibir erro operacional compreensível quando o backend estiver indisponível.

## 9. Segurança de fronteira

A API aplicará CORS por allowlist, limites de payload, rate limit local por sessão, validação de DTO, redaction de logs e headers de segurança. Endpoints de Actuator não serão expostos publicamente. Aprovação será vinculada a `runId`, usuário, tool, versão da decisão e nonce de uso único para impedir replay.

Nenhum cliente receberá permissão para escolher arbitrariamente uma skill, alterar `AutonomyLevel`, informar uma tool não registrada ou substituir o workspace autorizado. O cliente pode exibir a decisão e solicitar aprovação; a decisão final continua no backend.

## 10. Ordem de implementação multiplataforma

A ordem correta é estabilizar o contrato antes da experiência visual. Primeiro serão fechados sessão, ownership, conversação contínua, runs persistidos e API versionada. Depois será construído o web client, que oferece o ciclo de feedback mais rápido. Em seguida será criado o app Expo, reutilizando os contratos e os casos de uso de cliente. Por fim serão adicionados streaming, notificações, offline controlado, deep links e distribuição.

## 11. Critérios de sucesso

A arquitetura será considerada funcional quando o mesmo usuário puder iniciar uma conversa no web, continuar no Android ou iOS, consultar o mesmo histórico, acompanhar o mesmo `runId`, receber a mesma decisão de skill e aprovar uma ação sensível em um canal sem quebrar a execução em outro. Cada canal deve falhar de forma compreensível quando a API estiver indisponível, sem inventar resposta ou estado concluído.
