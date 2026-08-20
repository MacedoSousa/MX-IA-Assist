#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ASSETS="$ROOT/docs/portfolio/video-assets"
AUDIO="$ROOT/docs/portfolio/mx-presentation-ptbr.wav"
OUT="$ROOT/docs/portfolio/mx-presentation-ptbr.mp4"
DUR="13.048"
ffmpeg -y \
  -loop 1 -t "$DUR" -i "$ASSETS/01-cover.png" \
  -loop 1 -t "$DUR" -i "$ASSETS/02-architecture.png" \
  -loop 1 -t "$DUR" -i "$ASSETS/03-technology.png" \
  -loop 1 -t "$DUR" -i "$ASSETS/04-security.png" \
  -loop 1 -t "$DUR" -i "$ASSETS/05-evidence.png" \
  -i "$AUDIO" \
  -filter_complex "[0:v]format=yuv420p,fade=t=in:st=0:d=0.4,fade=t=out:st=12.6:d=0.4[v0];[1:v]format=yuv420p,fade=t=in:st=0:d=0.4,fade=t=out:st=12.6:d=0.4[v1];[2:v]format=yuv420p,fade=t=in:st=0:d=0.4,fade=t=out:st=12.6:d=0.4[v2];[3:v]format=yuv420p,fade=t=in:st=0:d=0.4,fade=t=out:st=12.6:d=0.4[v3];[4:v]format=yuv420p,fade=t=in:st=0:d=0.4,fade=t=out:st=12.6:d=0.4[v4];[v0][v1][v2][v3][v4]concat=n=5:v=1:a=0[v]" \
  -map "[v]" -map 5:a:0 \
  -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p \
  -c:a aac -b:a 160k -ar 48000 -shortest -movflags +faststart "$OUT"
ffprobe -v error -show_entries format=duration,size -of default=noprint_wrappers=1 "$OUT"
