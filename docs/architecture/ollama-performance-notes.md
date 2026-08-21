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


## Atualização verificada em 2026-08-21

A página oficial do modelo [Qwen3.8](https://ollama.com/library/qwen3.8) informa que o modelo `qwen3.8:27b` tem aproximadamente **18 GB**, janela de contexto de **256K**, suporte a texto e imagem, e melhorias em código, trabalho profissional, pesquisa, tarefas agentic e visão. Isso o torna potencialmente mais potente que o `qwen3:8b` atual, mas não deve ser adotado automaticamente em uma máquina CPU-only com memória limitada: o custo de carregamento e inferência pode reduzir drasticamente a latência percebida.

A [FAQ oficial do Ollama](https://docs.ollama.com/faq) confirma que `OLLAMA_CONTEXT_LENGTH` define o contexto padrão do servidor, `num_ctx` pode ser enviado por requisição, `ollama ps` mostra a fração CPU/GPU utilizada, `OLLAMA_NO_CLOUD=1` desativa recursos cloud, e `keep_alive` pode manter um modelo carregado por duração configurável ou valor negativo. A documentação também informa que o padrão é 4096 tokens e que modelos podem ser pré-carregados com uma requisição vazia.

A [documentação oficial de hardware](https://docs.ollama.com/gpu) confirma suporte a GPU NVIDIA, AMD, Metal e Vulkan conforme hardware/driver; no Windows com WSL2, aceleração Docker depende de suporte GPU e NVIDIA Container Toolkit. A ausência de NVML anterior no ambiente do MX significa que a recomendação segura permanece CPU-only até uma verificação real de `ollama ps`, `nvidia-smi`/driver ou logs do runtime.

**Decisão:** manter `qwen3:8b` como modelo padrão rápido até medir RAM, CPU/GPU e tokens por segundo; testar `qwen3.8:27b` como perfil de alta capacidade opcional, nunca substituí-lo cegamente. Para entradas visuais, o modelo visual deve ser selecionado de forma configurável e validado por `/api/tags`/`ollama ps`. Docker Compose local não oferece autoscaling horizontal real; o caminho correto é escalonamento vertical, fila limitada, `OLLAMA_NUM_PARALLEL`, memória/CPU, keep-alive, cache e, apenas se necessário, réplicas cuidadosamente limitadas com estado externo.
