# GPU Validation — 2026-08-21

## Hardware detected on Windows

The Windows inventory reported:

| Item | Observed value |
|---|---|
| GPU | NVIDIA GeForce RTX 2060 SUPER |
| Reported adapter memory | 4,293,918,720 bytes, approximately 4 GiB |
| Driver version | 32.0.16.1088 |
| Host `nvidia-smi` | Failed with `Failed to initialize NVML: Unknown Error` |

The user also reports 32 GB system RAM and a 10th-generation Intel i5. The Windows WMI value is truncated because the adapter-memory field is 32-bit; the container-level NVIDIA query reports the actual 8,192 MiB GPU memory.

## Docker GPU passthrough test

The official validation path succeeded on Windows:

```text
docker run --rm --gpus all nvidia/cuda:12.9.0-base-ubuntu22.04 nvidia-smi --query-gpu=name,memory.total,driver_version --format=csv,noheader,nounits
NVIDIA GeForce RTX 2060 SUPER, 8192, 610.88
```

Therefore, Docker Desktop can expose the RTX 2060 Super to containers even though the host-side `nvidia-smi` command reports an NVML error. The MX Compose file still needs an explicit GPU device reservation for the `ollama` service so that the normal service receives the same access.

## Ollama container state

The command `docker exec mx-ollama ollama list` showed only `qwen3:8b` installed, approximately 5.2 GB. `docker exec mx-ollama ollama ps` did not show an active model at the moment of verification. `docker inspect mx-ollama --format '{{json .HostConfig.DeviceRequests}}'` returned `null`, which means the current container was not created with an explicit NVIDIA GPU device request.

## Docker/Windows documentation evidence

Docker's official documentation states that GPU support on Docker Desktop for Windows requires the WSL2 backend, current NVIDIA drivers with WSL2 GPU paravirtualization support and an updated WSL2 kernel. It recommends validating GPU access with a container launched using `--gpus=all`.

Docker's official Compose documentation specifies GPU reservations under `deploy.resources.reservations.devices`, with `driver: nvidia`, `count` or `device_ids`, and mandatory `capabilities: [gpu]`. `count` and `device_ids` must not be combined.

Sources:

- https://docs.docker.com/desktop/features/gpu/
- https://docs.docker.com/compose/how-tos/gpu-support/

## Ollama GPU benchmark

After recreating `mx-ollama` with the NVIDIA reservation, the container reported:

| Model | Ollama processor report | Resident size | Context observed | Short prompt wall time |
|---|---|---:|---:|---:|
| `qwen3:8b` | `100% GPU` | 5.6 GB | 4096 | 66.2 s in the current Docker/Windows measurement |
| `deepseek-r1:14b` | `36%/64% CPU/GPU` | 10.0 GB | 4096 | 127.4 s in the current Docker/Windows measurement |

The benchmark used `ollama run` with the prompt `Responda apenas OK.` and `Measure-Command`. These are wall-clock measurements including the Docker exec and model invocation, not a tokens-per-second benchmark. They nevertheless confirm the expected trade-off: Qwen3:8B fits fully on the RTX 2060 SUPER, while DeepSeek-R1:14B uses hybrid CPU/GPU offload and is substantially slower.
