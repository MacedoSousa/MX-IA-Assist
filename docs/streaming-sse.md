# Streaming SSE no MX Core

## Objetivo

O endpoint de streaming permite que o cliente receba a resposta do modelo local de forma incremental, sem esperar que todo o texto seja produzido. O streaming é uma capacidade do MX Core; o cliente não acessa Ollama diretamente.

```text
Cliente Expo → POST /api/v1/conversations/messages/stream
             → ChatV1SseController
             → MX Core
             → skill selecionada
             → StreamingModelGateway
             → Ollama local
```

## Contrato

A requisição usa o mesmo payload básico do caminho síncrono:

```json
{
  "conversationId": null,
  "prompt": "Explique este erro de compilação",
  "idempotencyKey": "opcional-na-versao-atual"
}
```

O endpoint exige um access token JWT:

```text
POST /api/v1/conversations/messages/stream
Authorization: Bearer <access-token>
Content-Type: application/json
Accept: text/event-stream
```

O contrato completo está em [`docs/api/mx-v1.yaml`](api/mx-v1.yaml). O prompt possui limites de tamanho definidos no contrato; entradas vazias ou inválidas são rejeitadas antes de chegar ao modelo.

## Eventos

| Evento SSE | Payload mínimo | Semântica |
|---|---|---|
| `started` | `runId`, `correlationId` | O MX Core recebeu a mensagem e criou uma execução rastreável. |
| `token` | `delta` | Fragmento incremental do texto produzido pela skill/modelo. |
| `completed` | `status`, `runId` | O fluxo terminou com estado final conhecido. |
| `error` | `code`, `message` | O fluxo falhou de forma controlada; a mensagem não deve ser interpretada como resposta completa. |

Exemplo:

```text
event: started
data: {"runId":"...","correlationId":"..."}

event: token
data: {"delta":"A primeira parte"}

event: token
data: {"delta":" da resposta."}

event: completed
data: {"status":"COMPLETED","runId":"..."}
```

O campo `delta` é acrescentado pelo cliente ao buffer da mensagem do assistant. O cliente deve preservar o `runId` recebido no evento `started` para permitir consulta posterior do estado do run.

## Fluxo interno

O `ChatV1SseController` executa o processamento em executor virtual dedicado. O `MxCoreService` cria o `ExecutionRun`, emite a transição `RECEIVED`, roteia para a skill, marca `ROUTED` e `EXECUTING`, e encaminha a execução ao gateway de streaming. Durante o retorno, a skill emite deltas; no encerramento, o MX persiste `VERIFYING` e então `COMPLETED`, ou `FAILED` em caso de erro controlado.

O adapter Ollama recebe respostas NDJSON. Cada objeto contendo um fragmento é convertido em `onChunk`; o término do stream dispara `onCompleted`. Status HTTP não-2xx, JSON inválido, resposta vazia ou indisponibilidade do modelo devem gerar `onError` e um estado final seguro, sem fabricar uma resposta de sucesso.

## Cliente e refresh de sessão

O cliente Expo usa `sendMessageStream()` e um parser SSE. Se a requisição inicial ou uma chamada protegida receber `401`, o cliente tenta uma única renovação coordenada usando o refresh token. Promessas concorrentes compartilham a mesma renovação para evitar rotação duplicada; se o refresh falhar, a sessão local é limpa.

A sessão é armazenada em `localStorage` na web e em `expo-secure-store` nos dispositivos nativos. O cliente não coloca tokens na URL nem os envia ao Ollama.

## Reconexão e estado atual

A versão atual atualiza a interface token a token, mas ainda não implementa reconexão completa com cursor de eventos ou replay idempotente. Se a conexão cair, o cliente deve consultar `GET /api/v1/runs/{runId}` para obter o estado persistido antes de decidir se exibe erro, resposta parcial ou conclusão.

A implementação futura deve adicionar `Last-Event-ID` ou cursor equivalente, chave de idempotência por mensagem, deduplicação de deltas e reconciliação entre canais. A reconexão não pode reexecutar uma tool nem duplicar uma mensagem persistida.

## Regras operacionais

O streaming deve possuir timeout e cancelamento cooperativo. Erros de telemetria não podem derrubar a resposta principal; métricas e logs devem usar `correlationId` sem registrar prompts ou respostas completas fora da política de retenção.

O endpoint deve ser exercitado com o backend, PostgreSQL e Ollama disponíveis. Para diagnosticar uma falha, registre `runId`, `correlationId`, código do evento `error`, status do `/actuator/health` e logs redigidos do MX Core. Nunca inclua access token ou refresh token em tickets ou logs.
