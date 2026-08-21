# Notas de desempenho do Ollama / Ollama performance notes

## Decisões confirmadas

A API `/api/generate` aceita `options.num_ctx` para controlar o contexto por requisição, e `keep_alive` mantém o modelo carregado pelo período configurado. O serviço MX envia ambos no payload. O projeto usa `num_ctx` configurável, com padrão de 8192, porque o MX combina memória recente, conhecimento de estudos e anexos; o valor deve ser reduzido se a máquina ficar sob pressão de memória.

No servidor Ollama, `OLLAMA_NUM_PARALLEL`, `OLLAMA_MAX_LOADED_MODELS`, `OLLAMA_KEEP_ALIVE`, `OLLAMA_CONTEXT_LENGTH`, `OLLAMA_FLASH_ATTENTION` e `OLLAMA_KV_CACHE_TYPE` são variáveis documentadas. O Compose mantém um único modelo carregado, paralelismo padrão 2, keep-alive de 10 minutos, flash attention habilitado e cache KV q8_0, todos substituíveis por variáveis de ambiente.

A documentação oficial recomenda verificar `ollama ps` para confirmar se o modelo está em CPU, GPU ou dividido entre os dois. Como a máquina do projeto não apresentou NVML no diagnóstico anterior, o Compose não força uma reserva de GPU. O caminho para GPU via Windows/WSL2 permanece opcional e deve ser habilitado somente após confirmar Docker Desktop, WSL2 e toolkit compatíveis.

Por segurança, a porta publicada do Ollama foi limitada a `127.0.0.1`; o `mx-core` continua acessando o serviço pelo DNS interno `ollama:11434`. O endpoint externo do MX continua sendo o cliente web protegido pelo Tailscale Serve, não o servidor Ollama.

## Referências

1. [Ollama FAQ — contexto, keep-alive, GPU e Docker](https://docs.ollama.com/faq)
2. [Ollama — Context length](https://docs.ollama.com/context-length)
3. [Ollama — Environment variables](https://www.mintlify.com/ollama/ollama/advanced/environment-variables)

## English summary

The MX sends `options.num_ctx` and `keep_alive` to Ollama for every generation request. Server-level parallelism, loaded-model count, keep-alive, context length, flash attention, and KV-cache type are configurable through Compose environment variables. Because the current machine did not expose NVML in the previous diagnostic, no GPU reservation is forced. Ollama is published only on localhost while the core accesses it through the internal Compose network.
