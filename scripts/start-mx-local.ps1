[CmdletBinding()]
param(
    [string]$Root = 'D:\MX',
    [switch]$WithImageGeneration,
    [switch]$WithVideoGeneration,
    [switch]$WithAudioTranscription,
    [switch]$OpenBrowser,
    [string]$OllamaUrl = 'http://127.0.0.1:11435',
    [int]$PostgresPort = 15432,
    [int]$CorePort = 8080,
    [string]$LanIp = '127.0.0.1',
    [string]$ImageGenerationUrl = 'http://127.0.0.1:7860'
)

$ErrorActionPreference = 'Stop'
$logs = Join-Path $Root 'logs'
New-Item -ItemType Directory -Force -Path $logs | Out-Null

function Resolve-MxFfmpeg {
    $fromPath = Get-Command ffmpeg.exe -ErrorAction SilentlyContinue
    if ($fromPath) { return $fromPath.Source }
    $wingetPackages = Join-Path $env:LOCALAPPDATA 'Microsoft\WinGet\Packages'
    if (Test-Path $wingetPackages) {
        return Get-ChildItem -Path $wingetPackages -Recurse -Filter 'ffmpeg.exe' -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName
    }
}

function Resolve-MxWhisper {
    $fromPath = Get-Command whisper.exe -ErrorAction SilentlyContinue
    if ($fromPath) { return $fromPath.Source }
    $candidate = Join-Path $env:APPDATA 'Python\Python312\Scripts\whisper.exe'
    if (Test-Path $candidate) { return $candidate }
}

$env:OLLAMA_URL = $OllamaUrl
$env:MX_POSTGRES_PORT = $PostgresPort
$env:SERVER_PORT = $CorePort
$env:MX_LAN_IP = $LanIp
if ($WithImageGeneration) {
    $env:MX_IMAGE_GENERATION_ENABLED = 'true'
    $env:MX_IMAGE_GENERATION_URL = $ImageGenerationUrl
    Write-Host "[INFO] Geração de imagens habilitada no endpoint local $ImageGenerationUrl."
} else {
    $env:MX_IMAGE_GENERATION_ENABLED = 'false'
}
if ($WithVideoGeneration) {
    if (-not $WithImageGeneration) {
        $env:MX_IMAGE_GENERATION_ENABLED = 'true'
        $env:MX_IMAGE_GENERATION_URL = $ImageGenerationUrl
        Write-Host "[INFO] Geração de imagens também foi habilitada, pois é pré-requisito do vídeo."
    }
    $ffmpegCommand = Resolve-MxFfmpeg
    if ([string]::IsNullOrWhiteSpace($ffmpegCommand)) { throw 'FFmpeg não encontrado. Instale-o e execute novamente.' }
    $env:MX_VIDEO_GENERATION_ENABLED = 'true'
    $env:MX_VIDEO_GENERATION_FFMPEG_COMMAND = $ffmpegCommand
    Write-Host "[INFO] Geração de vídeo habilitada com FFmpeg em $ffmpegCommand."
} else {
    $env:MX_VIDEO_GENERATION_ENABLED = 'false'
}
if ($WithAudioTranscription) {
    $whisperCommand = Resolve-MxWhisper
    if ([string]::IsNullOrWhiteSpace($whisperCommand)) { throw 'Whisper não encontrado. Instale-o e execute novamente.' }
    $env:MX_AUDIO_TRANSCRIPTION_ENABLED = 'true'
    $env:MX_AUDIO_TRANSCRIPTION_COMMAND = $whisperCommand
    $env:MX_AUDIO_TRANSCRIPTION_MODEL = 'base'
    Write-Host "[INFO] Transcrição de áudio habilitada com Whisper em $whisperCommand."
} else {
    $env:MX_AUDIO_TRANSCRIPTION_ENABLED = 'false'
}

& (Join-Path $Root 'scripts\verify-mx-local.ps1') -Root $Root -RequireImageGeneration:$WithImageGeneration -OllamaUrl $OllamaUrl -PostgresPort $PostgresPort
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if (Get-NetTCPConnection -LocalPort $CorePort -ErrorAction SilentlyContinue) {
    Write-Host "[INFO] MX Core já está escutando na porta $CorePort."
} else {
    $coreLog = Join-Path $logs 'mx-core-console.log'
    $coreErrorLog = Join-Path $logs 'mx-core-error.log'
    Start-Process -FilePath (Join-Path $Root 'scripts\run-backend.bat') -WorkingDirectory $Root -RedirectStandardOutput $coreLog -RedirectStandardError $coreErrorLog
    Write-Host "[INFO] MX Core iniciado. Logs: $coreLog e $coreErrorLog"
}

$deadline = (Get-Date).AddSeconds(90)
while ((Get-Date) -lt $deadline) {
    try {
        if ((Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:$CorePort/actuator/health" -TimeoutSec 3).StatusCode -eq 200) { break }
    } catch { Start-Sleep -Seconds 2 }
}
if ((Get-Date) -ge $deadline) { throw 'MX Core não respondeu ao healthcheck dentro de 90 segundos.' }

if (Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue) {
    Write-Host '[INFO] Expo Web já está escutando na porta 8081.'
} else {
    $webLog = Join-Path $logs 'mx-web-console.log'
    $webErrorLog = Join-Path $logs 'mx-web-error.log'
    Start-Process -FilePath (Join-Path $Root 'scripts\run-expo-web.bat') -ArgumentList $LanIp -WorkingDirectory $Root -RedirectStandardOutput $webLog -RedirectStandardError $webErrorLog
    Write-Host "[INFO] Expo Web iniciado. Logs: $webLog e $webErrorLog"
}

Write-Host "MX local iniciado: http://$LanIp:8081"
if ($OpenBrowser) { Start-Process 'http://localhost:8081' }
