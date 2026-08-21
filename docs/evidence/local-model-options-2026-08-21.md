# Opções de modelos locais sem API key — 2026-08-21

## Evidência oficial coletada

### Qwen3.6

Fonte: https://ollama.com/library/qwen3.6

A página oficial do Ollama apresenta o Qwen3.6 como uma família com foco em coding agentic e preservação de raciocínio. As variantes exibidas incluem `qwen3.6:27b` com aproximadamente 17 GB, `qwen3.6:35b` com aproximadamente 24 GB, ambas com contexto de 256K e entrada de texto e imagem. As variantes MLX são específicas para Apple Silicon e não são o alvo principal do MX em Windows/CPU.

### DeepSeek-R1

Fonte: https://ollama.com/library/deepseek-r1:14b

A página oficial apresenta `deepseek-r1:14b` com aproximadamente 9 GB, 14,8 bilhões de parâmetros, quantização Q4_K_M e licença MIT. A família também disponibiliza variantes destiladas maiores, como 32B e 70B, mas o custo de memória e a latência em CPU aumentam substancialmente.

## Interpretação para o MX

Os pesos locais do Ollama podem ser executados sem API key; a API local em `localhost:11434` não exige autenticação por chave. O limitador real é o hardware: tamanho do modelo, RAM/VRAM disponível, contexto e velocidade de memória. A troca do `qwen3:8b` por um modelo de 27B–35B pode elevar a qualidade, mas não deve ser aplicada cegamente na máquina atual CPU-only, porque pode tornar respostas interativas muito mais lentas ou causar pressão de memória.

### Gemma 4

Fonte: https://ollama.com/library/gemma4:e4b

A página oficial descreve a família Gemma 4 como voltada a raciocínio, workflows agentic, coding e entendimento multimodal. A variante `gemma4:e4b` é um modelo edge com 4,5B parâmetros efetivos, suporta texto, imagem e áudio, e possui contexto de 128K. As variantes workstation incluem `gemma4:12b`, `gemma4:26b` (MoE com 4B ativos) e `gemma4:31b`. A página informa licença Apache 2.0 para a variante consultada e execução pelo comando `ollama run gemma4:e4b`.

Para o MX, `gemma4:e4b` é uma alternativa interessante para multimodalidade local com menor custo que modelos de 27B–35B, mas a superioridade em relação ao `qwen3:8b` deve ser medida no hardware real e na qualidade das respostas em português, código e tarefas do projeto.
