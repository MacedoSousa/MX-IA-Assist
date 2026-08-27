# Referências de compatibilidade — geração de imagens Forge

Estas fontes foram consultadas em 27 de agosto de 2026 para orientar a evolução do contrato `txt2img` do MX. A implementação deve validar o endpoint contra a instância Forge local antes de expor um parâmetro ao cliente.

| Fonte | Uso no MX |
|---|---|
| [Forge issue: Calling the API `/sdapi/v1/txt2img`](https://github.com/lllyasviel/stable-diffusion-webui-forge/issues/1151) | Confirma o endpoint base utilizado pelo serviço atual. |
| [AUTOMATIC1111: Basic Documentation and Examples for using API](https://github.com/AUTOMATIC1111/stable-diffusion-webui/discussions/3734) | Referência de compatibilidade para o formato de parâmetros do ecossistema WebUI. |
| [SD-WebUI Forge guide](https://docs.clore.ai/guides/image-generation/sd-webui-forge) | Contexto de Forge como derivação otimizada e compatível com o fluxo WebUI. |

O MX continuará enviando um conjunto mínimo e validado de campos: prompt, prompt negativo, dimensões, steps, seed e escala de orientação. Modelos, extensões e opções específicas de instância não devem ser aceitos sem consulta prévia às capacidades expostas pelo Forge local.
