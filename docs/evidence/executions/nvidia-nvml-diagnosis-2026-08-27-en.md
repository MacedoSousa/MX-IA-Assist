# NVIDIA/NVML diagnosis — August 27, 2026

## Read-only evidence

| Check | Result |
|---|---|
| Adapter | NVIDIA GeForce RTX 2060 SUPER |
| Device Manager | `OK` state |
| Windows-reported driver | `32.0.16.1656` |
| `nvml.dll` and `nvidia-smi.exe` | Present in `C:\Windows\System32` |
| `nvidia-smi -L` | Failed: `Failed to initialize NVML: Unknown Error` |
| Local PyTorch | `2.13.0+cpu` |
| PyTorch compiled/available CUDA | `null` / `false` |

The adapter is recognized, but NVML does not respond and installed PyTorch is CPU-only. There is therefore no evidence to claim Whisper CUDA is operational. MX transcription remains explicitly on CPU; chat model and Forge keep their own GPU policies.

## Next intervention, not executed

Recovery requires a maintenance window to install or reinstall the signed NVIDIA driver and restart Windows, followed by a new `nvidia-smi` and `scripts/inspect-pytorch-cuda.py` run. This affects the graphics subsystem and may temporarily interrupt Docker/WSL, Forge, and Ollama, so MX does not apply it automatically.
