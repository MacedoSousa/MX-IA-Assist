[CmdletBinding()]
param(
    [string]$Executable = "$env:LOCALAPPDATA\Programs\Ollama\ollama.exe",
    [string]$ModelsRoot = "D:\MX\data\ollama\models",
    [int]$Port = 11435,
    [string]$LogsRoot = "D:\MX\logs"
)

$ErrorActionPreference = 'Stop'
if (-not (Test-Path $Executable)) { throw "Executável Ollama não encontrado: $Executable" }
if (-not (Test-Path $ModelsRoot)) { throw "Diretório de modelos não encontrado: $ModelsRoot" }
if (Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue) {
    Write-Host "Ollama já está escutando na porta $Port."
    exit 0
}

New-Item -ItemType Directory -Force -Path $LogsRoot | Out-Null
$env:OLLAMA_HOST = "127.0.0.1:$Port"
$env:OLLAMA_MODELS = $ModelsRoot
Start-Process -FilePath $Executable -ArgumentList 'serve' -WindowStyle Hidden `
    -RedirectStandardOutput (Join-Path $LogsRoot 'ollama-native.log') `
    -RedirectStandardError (Join-Path $LogsRoot 'ollama-native-error.log')
Write-Host "Ollama nativo iniciado na porta $Port usando modelos em $ModelsRoot."
