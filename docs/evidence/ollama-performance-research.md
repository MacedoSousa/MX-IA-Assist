# Evidência de desempenho do Ollama no MX

## Fontes oficiais

A documentação oficial do Ollama informa que DeepSeek R1 é um modelo compatível com thinking e que a API aceita o campo `think` com valores booleanos. Com `think: false`, o modelo pode responder diretamente sem produzir a etapa de raciocínio. Fonte: https://docs.ollama.com/capabilities/thinking

A documentação oficial da API `/api/generate` documenta `think` como campo opcional da requisição, `keep_alive` para manter o modelo carregado e `options.num_ctx` para controlar o contexto. Fonte: https://docs.ollama.com/api/generate

## Medição no computador local

Em 21 de agosto de 2026, o container `mx-ollama` confirmou a reserva de uma GPU NVIDIA (`DeviceRequests` com `Capabilities: gpu`). O limite efetivo de CPU e memória reportado pelo Docker foi `NanoCpus=0` e `Memory=0`, indicando que o container não recebeu limite rígido de CPU/RAM, embora o Compose declare defaults de oito CPUs e 12 GB.

O modelo carregado foi `deepseek-r1:14b`, tamanho reportado de 10 GB, com distribuição observada de aproximadamente 41% GPU e 59% CPU, contexto 8192. O ambiente medido estava com `OLLAMA_NUM_PARALLEL=1`, `OLLAMA_MAX_LOADED_MODELS=1` e, após a otimização, `OLLAMA_KEEP_ALIVE=1h`.

Uma chamada direta controlada ao endpoint local `/api/generate`, com `stream=false`, `num_ctx=4096`, `num_predict=32` e prompt `Responda somente: Oi.`, apresentou:

| Situação | TTFB | Total |
|---|---:|---:|
| Primeiro pedido após o modelo estar descarregado | 93,56 s | 93,56 s |
| Pedido seguinte com o modelo aquecido | 5,17 s | 5,17 s |

Conclusão: a demora de aproximadamente um minuto é predominantemente o custo de carregamento do DeepSeek R1 14B em perfil híbrido CPU/GPU após o período de `keep_alive`, não uma espera artificial no cliente SSE. O cliente já processa eventos `started` e `token` progressivamente.

## Decisões de otimização

1. Manter `deepseek-r1:14b` como modelo padrão de máxima capacidade.
2. Aumentar o período de permanência do modelo carregado para reduzir cold starts.
3. Usar `think: false` em mensagens curtas e casuais, como saudações, mantendo thinking para tarefas complexas.
4. Exibir no Web UI o estado de carregamento, o tempo decorrido e a indicação de que o modelo está aquecendo, evitando a aparência de travamento.

## Alterações aplicadas

O Compose e o perfil `application-dev` foram ajustados para `OLLAMA_KEEP_ALIVE=1h`, reduzindo a probabilidade de descarregamento entre mensagens. Os limites declarativos de CPU, memória e shared memory foram removidos do serviço Ollama. O container continua com `OLLAMA_NUM_PARALLEL=1` e `OLLAMA_MAX_LOADED_MODELS=1` para não provocar concorrência artificial acima da VRAM disponível; isso não limita o uso de CPU ou GPU durante uma geração.

O `OllamaService` agora envia `think: false` somente para saudações curtas e casuais (`oi`, `olá`, `hello`, `hi`, `bom dia`, entre outras), mantendo `think: true` para solicitações complexas e para mensagens com imagens. O comportamento foi coberto por teste unitário. O cliente também exibe o estágio da resposta e o tempo decorrido, distinguindo raciocínio, aquecimento do modelo e geração de tokens.

A conclusão operacional é que o hardware não possui um limite rígido de CPU/RAM aplicado pelo Docker, e a GPU NVIDIA está reservada para o Ollama. A configuração usa o hardware disponível de forma conservadora na concorrência, não de forma limitada durante o processamento de uma solicitação individual.

O backend passou a aquecer o modelo de forma assíncrona no evento `ApplicationReadyEvent` quando `OLLAMA_WARMUP_ENABLED=true`. O warmup usa uma saudação curta, portanto envia `think: false`, carrega o `deepseek-r1:14b` antes da primeira interação e mantém a subida do Spring não bloqueada. O comportamento habilitado/desabilitado possui testes unitários dedicados.

## Validação pós-implantação

Após a reconstrução do `mx-core`, o warmup assíncrono concluiu em aproximadamente **155,5 segundos** no primeiro carregamento híbrido. Em seguida, `ollama ps` confirmou `deepseek-r1:14b` carregado por mais 59 minutos, com distribuição instantânea de aproximadamente **38% CPU / 62% GPU**, contexto 8192 e sem limite rígido de CPU/RAM no container (`NanoCpus=0`, `Memory=0`). Uma chamada direta posterior de saudação com `think: false` respondeu em aproximadamente **4,12 segundos**, demonstrando que o atraso de cold start foi isolado do tempo normal de geração.
