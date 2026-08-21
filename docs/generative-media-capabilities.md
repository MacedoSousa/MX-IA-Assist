# Capacidades generativas multimodais do MX

**Data:** 21 de agosto de 2026
**Escopo:** Geração e análise local de imagens, vídeos e documentos pelo MX Core.

## Visão geral

O MX agora mantém uma cadeia única e rastreável para as modalidades generativas. A solicitação do usuário passa pelo cliente autenticado, pelo **MX Core** e pelo serviço de mídia correspondente; o artefato final é armazenado como anexo pertencente ao mesmo usuário para download ou uso na conversa.

| Modalidade | Implementação local | Entrega | Limites e comportamento |
|---|---|---|---|
| Imagem | Stable Diffusion WebUI Forge via `txt2img` | PNG anexado | Prompt com 1–4.000 caracteres; resolução limitada pela configuração local. |
| Vídeo | Quadro-chave criado pelo Forge e composição MP4 com FFmpeg | MP4 anexado | Duração, pixels, FPS e timeout configuráveis; aplica movimento panorâmico/zoom suave ao quadro visual. |
| Documento | DeepSeek R1, orquestrado pelo MX Core, seguido de renderização local | Markdown, DOCX ou PDF anexado | Prompt, título e formato validados; PDF usa fonte Unicode local para preservar português. |
| Documento recebido | Extração segura de PDF, TXT, DOCX e PPTX | Contexto delimitado para a conversa | O conteúdo do arquivo é dado não confiável, não autorização para ações. |
| Imagem recebida | Modelo visual local pelo caminho multimodal do MX Core | Resposta na conversa | A imagem preserva a orquestração de skill, telemetria e política. |

## Fluxos operacionais

### Imagens

O botão **Gerar imagem** envia o texto atual ao endpoint protegido de mídia. O serviço valida o tamanho e a resolução antes de chamar o Forge local. A imagem retornada é persistida como anexo do usuário, evitando URLs externas temporárias.

### Vídeos

O botão **Gerar vídeo** cria primeiro um quadro-chave com o mesmo prompt de imagem. Em seguida, o FFmpeg gera um MP4 H.264 com zoom suave, preservando a proporção solicitada e sem inserir o texto do prompt na mídia. Essa estratégia é local, reproduzível e adequada ao hardware atual.

> **Transparência importante:** este fluxo cria um vídeo composto a partir de uma imagem generativa; não é um modelo temporal de difusão que inventa movimentos independentes a cada frame. Caso o projeto exija animação complexa, consistência de personagens ou cenas com múltiplos planos, será necessária uma etapa futura com um modelo local específico de image-to-video e nova validação de VRAM, tempo e segurança.

### Documentos

O comando **Gerar documento PDF** usa o conteúdo atual do compositor como solicitação e cria um PDF para download. A API também aceita `MARKDOWN`, `DOCX` e `PDF`, o que permite evoluir a interface para um seletor de formato sem alterar o contrato central. Antes de renderizar, o texto é solicitado pelo `MxCoreModelGateway`, portanto passa por contexto de conversa, anexos delimitados, skill e políticas já existentes.

| Formato | Uso indicado | Verificação automatizada |
|---|---|---|
| Markdown | Anotações, documentação versionável e revisão rápida | MIME, nome seguro e armazenamento pertencente ao usuário. |
| DOCX | Edição posterior em processadores de texto | Assinatura ZIP/Office (`PK`) validada no teste. |
| PDF | Distribuição e leitura estável | Assinatura `%PDF` e fonte Unicode configurada no teste. |

## Segurança, privacidade e qualidade

As rotas exigem autenticação, associam artefatos ao usuário autenticado e aplicam limites antes de iniciar processos locais. O FFmpeg recebe argumentos estruturados, possui timeout e remove o diretório temporário ao fim da tarefa. Nenhum prompt, mídia ou documento de usuário é versionado no Git.

Em **21 de agosto de 2026**, o script autenticado `infrastructure/docker/scripts/smoke-generative.mjs` foi executado contra o host local. Ele confirmou, em sequência, a criação de uma imagem pelo Forge, a composição de um vídeo MP4 local e a geração e o download de um PDF válido. O teste usa credenciais somente em variáveis de ambiente, não versiona os artefatos e verifica a assinatura do PDF antes de reportar sucesso.

| Modalidade | Resultado de validação | Observação |
|---|---|---|
| Imagem | Aprovado | Artefato registrado pela API de anexos. |
| Vídeo | Aprovado | MP4 composto localmente a partir de quadro visual gerado. |
| Documento | Aprovado | PDF gerado, armazenado e baixado com assinatura válida. |

O runtime usa `MX_OLLAMA_TIMEOUT_MS`, com padrão operacional de cinco minutos, para acomodar a inferência híbrida do `deepseek-r1:14b`. Documentos usam um contrato de saída estruturada que desativa o raciocínio interno do modelo, sem desviar a solicitação do MX Core.

O conteúdo gerado deve ser revisado antes de uso acadêmico, jurídico, médico, financeiro ou administrativo. O MX não apresenta automaticamente um documento gerado como fato comprovado: fontes, valores, nomes e datas devem ser conferidos pelo usuário contra o material original.

## Próximas evoluções deliberadas

O backlog técnico preserva quatro avanços que exigem avaliação independente: seleção explícita de formato e metadados no cliente, animação temporal local dedicada, modelos de template para documentos e RAG documental com citações por página/chunk. Cada evolução deverá manter versionamento de configuração, conjunto de testes e avaliação reproduzível.

---

# MX Generative Media Capabilities

The MX now uses one traceable chain for local generative media. A request travels through the authenticated client and **MX Core** to the corresponding media service; the resulting artifact is stored as an attachment owned by the same user.

| Capability | Local implementation | Result |
|---|---|---|
| Image generation | Stable Diffusion WebUI Forge `txt2img` | Owned PNG attachment |
| Video generation | Forge-generated keyframe plus FFmpeg composition | Owned H.264 MP4 attachment |
| Document generation | DeepSeek R1 through MX Core plus local rendering | Owned Markdown, DOCX, or PDF attachment |
| Document understanding | Safe extraction from PDF, TXT, DOCX, and PPTX | Delimited conversation context |
| Image understanding | Local visual-model route through MX Core | Conversation response with Core policies preserved |

The video workflow is intentionally transparent: it creates a visually generated keyframe and animates it with controlled pan/zoom. It is not yet a temporal diffusion or dedicated image-to-video model. Generated documents should also be reviewed before any consequential use; generated text never substitutes verification of original sources, values, dates, or names.
