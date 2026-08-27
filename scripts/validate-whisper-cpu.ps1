<#
MX prototype evidence helper: validates an authorized audio transcription on CPU.
It writes output only under the current user's temporary directory, prints no transcript,
and removes temporary files on completion. It does not touch MX data, Docker, or secrets.
#>
param(
    [Parameter(Mandatory = $true)]
    [ValidateScript({ Test-Path -LiteralPath $_ -PathType Leaf })]
    [string]$InputAudio,
    [ValidateSet('tiny', 'base', 'small')]
    [string]$Model = 'tiny',
    [ValidateSet('pt', 'en')]
    [string]$Language = 'pt'
)

$ErrorActionPreference = 'Stop'
$python = Get-Command python.exe -ErrorAction Stop
$ffmpeg = Get-Command ffmpeg.exe -ErrorAction SilentlyContinue
if ($null -eq $ffmpeg) {
    $wingetPackages = Join-Path $env:LOCALAPPDATA 'Microsoft\WinGet\Packages'
    $ffmpeg = Get-ChildItem -LiteralPath $wingetPackages -Recurse -Filter 'ffmpeg.exe' -File -ErrorAction SilentlyContinue |
        Select-Object -First 1
}
if ($null -eq $ffmpeg) {
    throw 'FFmpeg não foi encontrado. Instale-o antes de validar a transcrição local.'
}
$ffmpegPath = if ($ffmpeg.PSObject.Properties.Name -contains 'Source') { $ffmpeg.Source } else { $ffmpeg.FullName }
if ([string]::IsNullOrWhiteSpace($ffmpegPath)) {
    throw 'O caminho do FFmpeg não pôde ser resolvido.'
}
$env:Path = "$(Split-Path -Parent $ffmpegPath)$([IO.Path]::PathSeparator)$env:Path"
$temporaryOutput = Join-Path $env:TEMP ("mx-whisper-validation-" + [guid]::NewGuid().ToString('N'))
$whisperLog = Join-Path $temporaryOutput 'whisper.log'

try {
    New-Item -ItemType Directory -Path $temporaryOutput -Force | Out-Null
    & $python.Source -m whisper $InputAudio --model $Model --language $Language --task transcribe --fp16 False --output_dir $temporaryOutput --output_format txt *> $whisperLog
    if ($LASTEXITCODE -ne 0) {
        throw "Whisper encerrou com código $LASTEXITCODE."
    }

    $transcript = Get-ChildItem -LiteralPath $temporaryOutput -Filter '*.txt' -File | Select-Object -First 1
    if ($null -eq $transcript) {
        throw 'Whisper não gerou o arquivo de transcrição esperado.'
    }

    $characters = (Get-Content -LiteralPath $transcript.FullName -Raw).Trim().Length
    if ($characters -eq 0) {
        throw 'Whisper gerou uma transcrição vazia.'
    }

    Write-Output 'WHISPER_CPU_VALIDATION=PASS'
    Write-Output "MODEL=$Model"
    Write-Output "LANGUAGE=$Language"
    Write-Output "TRANSCRIPT_CHARACTERS=$characters"
} finally {
    if (Test-Path -LiteralPath $temporaryOutput) {
        Remove-Item -LiteralPath $temporaryOutput -Recurse -Force
    }
}
