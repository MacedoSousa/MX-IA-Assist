# Pesquisa do motor local de imagens

## Evidências consultadas em 2026-08-21

A documentação da API do AUTOMATIC1111 descreve o endpoint `POST /sdapi/v1/txt2img`, payload JSON com pelo menos `prompt` e resposta contendo `images` como uma lista de imagens codificadas em Base64. Isso coincide com o contrato já implementado em `ImageGenerationService`.

Fonte: https://github.com/AUTOMATIC1111/stable-diffusion-webui/wiki/API

O repositório oficial do Stable Diffusion WebUI Forge para Docker informa que as imagens são destinadas a execução local e em GPU, iniciam o serviço na porta `7860` por padrão e aceitam configuração de porta por `FORGE_PORT_HOST`. O repositório também informa que os modelos e configurações de terceiros não são incluídos na imagem e precisam ser provisionados em volume persistente.

Fonte: https://github.com/ai-dock/stable-diffusion-webui-forge

O Forge é baseado no WebUI do AUTOMATIC1111, otimiza gerenciamento de recursos e velocidade de inferência, e mantém os endpoints de API de `txt2img` e `img2img` como funcionais. Isso permite manter o adapter Java existente e trocar o servidor local sem alterar o contrato do MX.

Fonte: https://github.com/lllyasviel/stable-diffusion-webui-forge

Decisão técnica: usar um servidor local compatível com AUTOMATIC1111, preferencialmente Forge para explorar melhor os 8 GB de VRAM da RTX 2060 Super, mantendo Stable Diffusion/SDXL em resoluções e passos moderados. FLUX não será o padrão para essa GPU devido ao maior consumo de memória e ao risco de latência/OOM. O Compose deve manter o motor separado e tolerar a indisponibilidade temporária, sem impedir que `mx-core` suba.
