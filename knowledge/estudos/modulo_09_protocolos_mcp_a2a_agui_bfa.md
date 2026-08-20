# Módulo 09 — MCP, A2A, AG-UI e Backend for Agents

## Fonte
[Protocolos e arquitetura para construção de agentes: MCP, A2A, AG-UI e Backend for Agents](https://cursos.alura.com.br/course/protocolos-agentes-inteligentes). A página apresenta módulos sobre sistemas multiagente, integração A2A, Model Context Protocol, comunicação AG-UI e Backend for Agents.

## Conteúdo observado
A ementa inclui arquitetura multiagente, orquestração e roteamento de intenções; comunicação distribuída com FastAPI e FastMCP; JSON-RPC e APIs HTTP; interfaces React e Streamlit com eventos AG-UI; integração com ferramentas, bases e APIs; RAG e catálogos de agentes; Docker Compose, isolamento, observabilidade e deploy.

## Aplicação ao MX
O MX deve separar quatro contratos. **MCP** expõe ferramentas e recursos com schemas e permissões. **A2A** permite comunicação entre agentes especializados, com identidade, capacidade, tarefa, estado e resultado. **AG-UI** traduz eventos do backend em uma interface interativa, incluindo progresso, citações, solicitação de aprovação e erro. **BFA** organiza a lógica de negócio, orquestração e políticas para que a interface e os agentes não acessem diretamente o domínio.

## Contrato mínimo de evento

```text
AgentEvent
- eventId
- traceId
- conversationId
- type: run_started | progress | citation | tool_proposed | approval_requested | tool_result | message_delta | completed | failed
- actor
- payloadSchemaVersion
- payload
- createdAt
```

## Regras de segurança
Cada servidor MCP deve declarar ferramentas, recursos, schemas de entrada, escopos e limites. A autorização continua no MX Core e deve ser revalidada no runtime. A2A não deve ser considerado confiança implícita: agentes precisam de identidade, autenticação, autorização e limites. Eventos de interface não são ordens de execução; são representação de estado. O BFA deve centralizar política, auditoria, rate limit, validação e correlação.

## Skill derivada
**Publicar capacidade de agente:** registrar agente e versão de contrato; declarar capacidades e schemas; negociar tarefa; validar identidade e autorização; emitir eventos correlacionados; persistir estado; finalizar com resultado assinado ou erro. Para MCP, separar `tools`, `resources` e `prompts`; para A2A, separar descoberta, delegação e retorno; para AG-UI, separar eventos de apresentação de comandos de domínio.

## Decisão para o assistente local
Começar com BFA interno no MX Core e adaptadores locais. Adicionar MCP para ferramentas read-only e recursos documentais. Adicionar A2A apenas quando houver agentes realmente independentes. Usar AG-UI quando a interface precisar exibir progresso, aprovação e resultados parciais. Não distribuir o sistema antes de contratos, observabilidade e autorização estarem testados.

## Referências
[1]: https://cursos.alura.com.br/course/protocolos-agentes-inteligentes "Curso da Alura — Protocolos e arquitetura para construção de agentes"
