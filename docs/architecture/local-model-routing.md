# Local Model Routing — MX

## Hardware baseline

The target machine has 32 GB of system RAM, a 10th-generation Intel i5 and an NVIDIA GeForce RTX 2060 SUPER with 8,192 MiB visible inside Docker. The official Docker GPU test succeeded, so the MX can use CPU and GPU together when the Ollama service is created with an NVIDIA device reservation.

## Capability matrix

| Workload | Primary model/service | CPU/GPU expectation | Decision |
|---|---|---|---|
| Fast general chat and short answers | `qwen3:8b` | Fits the 8 GB GPU more realistically than 14B+ models; CPU remains fallback | Keep as default |
| Difficult reasoning, mathematics and complex code | `deepseek-r1:14b` | Approximately 9 GB in Q4_K_M, so it cannot fit entirely in 8 GB VRAM; use hybrid CPU/GPU offload and lower concurrency | Optional slow reasoning profile |
| Text, image and audio understanding | `gemma4:e4b` | Multimodal edge model; use GPU when available and CPU fallback when not | Optional multimodal profile |
| High-capacity coding and agentic work | `qwen3.6:27b` | Approximately 17 GB; requires system RAM plus hybrid offload and will be slow on an i5/RTX 2060 Super | Do not make default |
| Large context and high-capacity reasoning | `qwen3.6:35b` or larger | Approximately 24 GB or more; unsuitable for the current interactive profile | Reserve for future hardware |
| Text documents | MX attachment extraction + selected text model | CPU extraction; GPU inference | Supported by the current attachment pipeline |
| Generated documents | LLM output in Markdown/HTML/plain text, then deterministic file rendering | CPU rendering, GPU for drafting | Supported; DOCX/PDF should be rendered by dedicated libraries/tools |
| Images | Configurable local image-generation endpoint | Separate image model/service; not DeepSeek-R1 | Keep optional Stable Diffusion-compatible endpoint |
| Audio transcription | Local Whisper command/service | CPU or GPU depending on installation | Keep optional and bounded by timeout/size |
| Video | ffmpeg/frame extraction + Whisper/vision model + LLM | CPU preprocessing; GPU for selected inference | Requires a pipeline; DeepSeek-R1 alone cannot create or understand video natively |

## Recommended routing policy

The MX should not use one large model for every request. The fast profile should use `qwen3:8b` for ordinary conversation and short answers. A reasoning profile may select `deepseek-r1:14b` only when the request requires multi-step analysis, mathematics or difficult code. A multimodal profile should use `gemma4:e4b` or another explicitly configured vision/audio model for images, audio and visual documents.

DeepSeek-R1:14B is a text reasoning model. It can analyze text extracted from PDFs, office files, source code and transcripts, and it can draft document content. It is not, by itself, an image generator, video generator, audio transcription model or native visual model. Those capabilities must remain separate services in the MX architecture.

## Volume limits

Large volumes should be handled through ingestion, chunking, metadata, pagination, bounded retrieval and asynchronous jobs rather than by placing all data into a single prompt. The current MX attachment limits and local lexical knowledge retrieval are safe defaults. A future semantic index can be added without changing the core conversation contract.

The RTX 2060 SUPER has enough memory for a practical fast 8B profile, but not for fully resident 14B or 27B quantized models after accounting for context and runtime overhead. Hybrid CPU/GPU offload is possible, but it trades memory fit for lower tokens per second. The system should therefore preserve a fast fallback and limit concurrent large-model requests to one.

## References

1. [Ollama Qwen3.6 library](https://ollama.com/library/qwen3.6)
2. [Ollama DeepSeek-R1 library](https://ollama.com/library/deepseek-r1:14b)
3. [Ollama Gemma 4 library](https://ollama.com/library/gemma4:e4b)
4. [Docker Desktop GPU support for Windows](https://docs.docker.com/desktop/features/gpu/)
5. [Docker Compose GPU support](https://docs.docker.com/compose/how-tos/gpu-support/)

## English summary

The MX should use model routing rather than forcing one large model onto every request. Keep `qwen3:8b` as the fast default, use `deepseek-r1:14b` for optional complex reasoning, and use `gemma4:e4b` or another configured multimodal model for image and audio understanding. DeepSeek-R1:14B can analyze extracted attachment text and draft documents, but it does not natively generate images, video or audio. Video requires a separate frame/transcription pipeline.
