# Validação Whisper em CPU — 27 de agosto de 2026

## Objetivo

Validar a transcrição local de um áudio autorizado do próprio projeto sem depender de CUDA, sem gravar a transcrição em diretórios do MX e sem expor o texto reconhecido no console.

| Verificação | Resultado |
|---|---|
| Entrada | `docs/portfolio/mx-presentation-ptbr.wav` — artefato local autorizado |
| Motor | OpenAI Whisper local, modelo `tiny` |
| Idioma informado | `pt` |
| Resultado | `WHISPER_CPU_VALIDATION=PASS` |
| Métrica sanitizada | 808 caracteres reconhecidos |
| Arquivo temporário | Gerado no diretório temporário do usuário e removido ao final |
| Dados MX, Docker e banco | Não acessados ou alterados |

## Ajuste de infraestrutura observado

O FFmpeg já estava instalado pelo catálogo Windows como `Gyan.FFmpeg.Shared`, porém seu diretório `bin` não estava disponível no `PATH` da sessão. O validador `scripts/validate-whisper-cpu.ps1` o localiza sob o diretório de pacotes do Windows e adiciona o caminho somente ao processo atual. Nenhuma variável de ambiente global foi alterada.

`nvidia-smi` continua retornando falha de inicialização NVML. Por esse motivo, esta evidência não declara suporte CUDA e Whisper deve seguir em CPU até a correção separada do driver/NVML e a validação explícita de PyTorch CUDA.

> A saída padrão do Whisper é redirecionada a um log temporário removido ao final. O script publica apenas êxito, modelo, idioma e contagem de caracteres, nunca a transcrição.
