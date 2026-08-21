# Pesquisa do motor local de imagens

## Evidências consultadas em 2026-08-21

A documentação da API do AUTOMATIC1111 descreve o endpoint `POST /sdapi/v1/txt2img`, payload JSON com pelo menos `prompt` e resposta contendo `images` como uma lista de imagens codificadas em Base64. Isso coincide com o contrato já implementado em `ImageGenerationService`.

Fonte: https://github.com/AUTOMATIC1111/stable-diffusion-webui/wiki/API

O repositório oficial do Stable Diffusion WebUI Forge para Docker informa que as imagens são destinadas a execução local e em GPU, iniciam o serviço na porta `7860` por padrão e aceitam configuração de porta por `FORGE_PORT_HOST`. O repositório também informa que os modelos e configurações de terceiros não são incluídos na imagem e precisam ser provisionados em volume persistente.

Fonte: https://github.com/ai-dock/stable-diffusion-webui-forge

O Forge é baseado no WebUI do AUTOMATIC1111, otimiza gerenciamento de recursos e velocidade de inferência, e mantém os endpoints de API de `txt2img` e `img2img` como funcionais. Isso permite manter o adapter Java existente e trocar o servidor local sem alterar o contrato do MX.

Fonte: https://github.com/lllyasviel/stable-diffusion-webui-forge

Decisão técnica: usar um servidor local compatível com AUTOMATIC1111, preferencialmente Forge para explorar melhor os 8 GB de VRAM da RTX 2060 Super, mantendo Stable Diffusion/SDXL em resoluções e passos moderados. FLUX não será o padrão para essa GPU devido ao maior consumo de memória e ao risco de latência/OOM. O Compose deve manter o motor separado e tolerar a indisponibilidade temporária, sem impedir que `mx-core` suba.


## Atualização de provisionamento

A documentação do AI-Dock publicada em 2026-08-21 informa que a tag CUDA atual segue o padrão `v2-cuda-12.1.1-base-22.04`, enquanto a tag sem o prefixo `v2` não está publicada no registro consultado. A imagem usada pelo Compose foi corrigida para `ghcr.io/ai-dock/stable-diffusion-webui-forge:v2-cuda-12.1.1-base-22.04`.

Fonte: https://raw.githubusercontent.com/ai-dock/stable-diffusion-webui-forge/main/README.md
Fonte: https://raw.githubusercontent.com/ai-dock/stable-diffusion-webui-forge/main/docker-compose.yaml

O modelo escolhido para a primeira geração local é `stable-diffusion-v1-5/stable-diffusion-v1-5`, usando o arquivo de inferência `v1-5-pruned-emaonly.safetensors`, disponibilizado no card do modelo e indicado para AUTOMATIC1111, Forge e outros clientes locais. O modelo usa resolução base de 512x512 e requer atenção às limitações de texto, viés, segurança e licença CreativeML OpenRAIL-M.

Fonte: https://huggingface.co/stable-diffusion-v1-5/stable-diffusion-v1-5
Fonte do arquivo indicado no card: https://huggingface.co/sd-legacy/stable-diffusion-v1-5/resolve/main/v1-5-pruned-emaonly.safetensors
