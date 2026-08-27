# Diagnóstico NVIDIA/NVML — 27 de agosto de 2026

## Evidências somente de leitura

| Verificação | Resultado |
|---|---|
| Adaptador | NVIDIA GeForce RTX 2060 SUPER |
| Gerenciador de Dispositivos | Estado `OK` |
| Driver reportado pelo Windows | `32.0.16.1656` |
| Arquivos `nvml.dll` e `nvidia-smi.exe` | Presentes em `C:\Windows\System32` |
| Consulta `nvidia-smi -L` | Falhou: `Failed to initialize NVML: Unknown Error` |
| PyTorch local | `2.13.0+cpu` |
| CUDA compilado/disponível no PyTorch | `null` / `false` |

O adaptador é reconhecido, mas a camada NVML não responde e o PyTorch instalado é CPU-only. Assim, não há evidência para declarar CUDA operacional no Whisper. A transcrição do MX permanece explicitamente em CPU; o modelo de conversa e o Forge preservam suas próprias políticas de GPU.

## Próxima intervenção, ainda não executada

A recuperação requer uma janela de manutenção para instalar ou reinstalar o driver NVIDIA assinado e reiniciar o Windows, seguida de nova execução de `nvidia-smi` e `scripts/inspect-pytorch-cuda.py`. Essa intervenção afeta o subsistema gráfico e pode interromper temporariamente Docker/WSL, Forge e Ollama; por isso não é aplicada automaticamente pelo MX.
