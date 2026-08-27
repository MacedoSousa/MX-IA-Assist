# Runtime de mídia local do protótipo MX

## Estado consolidado

O caminho primário do protótipo utiliza o Core Docker. A geração de imagem permanece integrada ao Forge em Docker, e a transcrição agora pode ser executada pelo próprio Core com **Whisper em CPU**. Essas capacidades não devem ser confundidas com garantia de disponibilidade de produção, aceleração GPU ou publicação externa.

| Capacidade | Estado no Core Docker | Limite de operação |
|---|---|---|
| Transcrição de áudio | Habilitada com `openai-whisper==20250625`, FFmpeg e modelo padrão `tiny` | Forçada em CPU; timeout de 300 s; o teste autenticado do endpoint permanece pendente |
| Cache do Whisper | Persistido em `data/whisper-cache` | Contém pesos reutilizáveis, não deve ser versionado |
| Geração de imagens | Forge Docker, healthcheck disponível, atualização automática desabilitada | Dependência transitória; validação nativa permanece fora de escopo até decisão explícita |
| Geração de vídeo | Fluxo FFmpeg local previamente validado | Deve respeitar os limites existentes de duração e resolução |
| Documentos | Geração local do Core já validada | Resultados são anexos do usuário autenticado |

## Isolamento de recursos

DeepSeek R1 14B e Forge podem disputar a GPU de 8 GB. Por isso, o comando executado pelo Core acrescenta `--device cpu --fp16 False` ao Whisper: transcrição usa CPU e não disputa VRAM com o chat ou a imagem. `nvidia-smi` ainda falha ao inicializar NVML; não há declaração de CUDA ativo para Whisper neste protótipo.

O Whisper requer FFmpeg para processar áudio e disponibiliza modelos multilíngues. A versão `20250625` foi fixada porque é a versão publicada pela fonte oficial utilizada nesta validação.[1] A escolha de `tiny` prioriza a responsividade para o primeiro fluxo integrado em CPU; precisão e modelo podem ser reavaliados posteriormente com medições no hardware local.

## Evidências e pendências

O áudio autorizado foi validado localmente em CPU, com 808 caracteres reconhecidos e sem preservar a transcrição. A imagem do Core ativo contém o comando `whisper`, e UI/Core responderam `200` após a reconstrução. Ainda falta usar uma sessão autenticada na UI para acionar `POST /api/v1/media/audio/{attachmentId}/transcription`; essa validação não será simulada com senha ou conta persistida.

O Forge segue como serviço Docker transitório, com API interna pelo endereço `image-engine:17860`, healthcheck e atualização automática desabilitada. Não será migrado para nativo enquanto a topologia primária e o driver NVIDIA/NVML não estiverem definidos. A exposição de PostgreSQL/Redis também permanece sem alteração até um incremento de infraestrutura que preserve e valide os dados.

## Referências

[1] [OpenAI Whisper no PyPI — versão 20250625 e requisito de FFmpeg](https://pypi.org/project/openai-whisper/)
