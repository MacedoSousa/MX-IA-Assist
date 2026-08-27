# Whisper CPU validation — August 27, 2026

## Objective

Validate local transcription of an authorized project audio file without CUDA, without writing transcripts under MX directories, and without exposing recognized text in the console.

| Check | Result |
|---|---|
| Input | `docs/portfolio/mx-presentation-ptbr.wav` — authorized local artifact |
| Engine | Local OpenAI Whisper, `tiny` model |
| Requested language | `pt` |
| Result | `WHISPER_CPU_VALIDATION=PASS` |
| Sanitized metric | 808 recognized characters |
| Temporary output | Created in the user's temporary directory and removed afterwards |
| MX data, Docker, and database | Not accessed or changed |

## Observed infrastructure adjustment

FFmpeg was already installed from the Windows catalog as `Gyan.FFmpeg.Shared`, but its `bin` directory was not available in the session `PATH`. `scripts/validate-whisper-cpu.ps1` finds it under the Windows package directory and adds that path to the current process only. No global environment variable was changed.

`nvidia-smi` still returns an NVML initialization failure. Therefore, this evidence does not claim CUDA support and Whisper must remain on CPU until a separate driver/NVML correction and explicit PyTorch CUDA validation.

> Whisper standard output is redirected to a temporary log removed on completion. The script reports only success, model, language, and character count, never the transcript.
