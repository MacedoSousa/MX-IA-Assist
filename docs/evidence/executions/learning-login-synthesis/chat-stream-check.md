# Evidência de diagnóstico do chat e streaming

**Data da execução:** 2026-08-20

## Diagnóstico

O MX Core, PostgreSQL, Redis, Ollama e cliente web estavam ativos. O healthcheck do Core retornou `UP` e o Ollama disponibilizou o modelo local configurado. A autenticação retornou `HTTP 200`.

A chamada não-streaming para uma mensagem simples retornou `HTTP 200` e `COMPLETED`. O cliente web, porém, marcava o canal como offline após o streaming porque o parser SSE procurava as sequências literais `\\r` e `\\n`, em vez de interpretar quebras de linha reais. Mesmo após essa correção, o Spring Security registrava `AuthorizationDeniedException` em um dispatch assíncrono do `SseEmitter`, encerrando a conexão depois de a resposta já ter sido iniciada.

## Correções

1. O parser SSE passou a usar `\\r?\\n`, `\\r?\\n\\r?\\n` e `\\n` reais no cliente web.
2. O Spring Security passou a permitir `DispatcherType.ASYNC` depois da autenticação inicial, mantendo a verificação do usuário no controller antes da criação do `SseEmitter`.
3. Os scripts temporários de diagnóstico foram removidos após a execução.

## Validação final

| Caminho | Login | Streaming | Eventos | Resultado |
|---|---:|---:|---:|---|
| MX Core local | 200 | 200 | started=1, token=12, completed=true, error=0 | Aprovado |
| Endpoint privado Tailscale | 200 | 200 | started=1, token=12, completed=true, error=0 | Aprovado |

Nenhum token, refresh token, hash ou senha foi registrado nesta evidência. O texto da resposta do modelo não é versionado; somente os estados e as contagens necessárias para auditoria foram preservados.
