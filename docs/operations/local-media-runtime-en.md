# MX prototype local media runtime

## Consolidated state

The primary prototype path uses Docker Core. Image generation remains integrated with Docker Forge, while audio transcription can now run from Core using **CPU Whisper**. These capabilities are not a production availability guarantee, GPU acceleration claim, or external-publishing mechanism.

| Capability | Docker Core state | Operating boundary |
|---|---|---|
| Audio transcription | Enabled with `openai-whisper==20250625`, FFmpeg, and default `tiny` model | Forced to CPU; 300 s timeout; authenticated endpoint test remains pending |
| Whisper cache | Persisted at `data/whisper-cache` | Reusable weights only; must not be versioned |
| Image generation | Docker Forge with healthcheck and auto-update disabled | Transitional dependency; native validation awaits an explicit decision |
| Video generation | Local FFmpeg workflow previously validated | Existing duration/resolution boundaries apply |
| Documents | Local Core generation previously validated | Results are authenticated-user attachments |

## Resource isolation

DeepSeek R1 14B and Forge can compete for the 8 GB GPU. Core therefore adds `--device cpu --fp16 False` to Whisper: transcription uses CPU and does not compete for VRAM with chat or image generation. `nvidia-smi` still fails to initialize NVML; this prototype makes no Whisper CUDA claim.

Whisper requires FFmpeg for audio processing and provides multilingual models. Version `20250625` was pinned because it is the release published by the official source used for this validation.[1] `tiny` prioritizes responsiveness for the first CPU-integrated flow; accuracy and model choice can be measured later on local hardware.

## Evidence and pending validation

Authorized audio was validated locally on CPU, with 808 recognized characters and no persisted transcript. The active Core image contains the `whisper` command, and UI/Core returned `200` after rebuild. An authenticated UI session must still call `POST /api/v1/media/audio/{attachmentId}/transcription`; this will not be simulated with a password or persisted account.

Forge remains a transitional Docker service, with internal API address `image-engine:17860`, healthcheck, and auto-update disabled. It will not be migrated to native until primary topology and NVIDIA driver/NVML are decided. PostgreSQL/Redis exposure also remains unchanged until an infrastructure increment preserves and validates data.

## References

[1] [OpenAI Whisper on PyPI — 20250625 release and FFmpeg requirement](https://pypi.org/project/openai-whisper/)
