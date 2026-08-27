[CmdletBinding()]
param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [switch]$RequireImageGeneration
)

$ErrorActionPreference = 'Stop'
$failures = [System.Collections.Generic.List[string]]::new()

function Test-MxCommand {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        $script:failures.Add("Executável ausente: $Name")
    }
}

function Add-OllamaToPath {
    $candidate = Join-Path $env:LOCALAPPDATA 'Programs\Ollama\ollama.exe'
    if ((Test-Path $candidate) -and -not (Get-Command ollama -ErrorAction SilentlyContinue)) {
        $env:Path = "$(Split-Path -Parent $candidate);$env:Path"
    }
}

function Test-MxEndpoint {
    param([string]$Name, [string]$Uri, [switch]$Required)
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $Uri -TimeoutSec 4
        if ($response.StatusCode -eq 200) {
            Write-Host "[OK] $Name respondeu em $Uri"
            return
        }
    } catch {
        if ($Required) {
            $script:failures.Add("$Name indisponível em $Uri")
        } else {
            Write-Host "[INFO] $Name ainda não está disponível em $Uri"
        }
    }
}

Write-Host '=== Pré-requisitos do MX Local ==='
Test-MxCommand java
Test-MxCommand node
Test-MxCommand npm
Add-OllamaToPath
Test-MxCommand ollama
Test-MxCommand pg_isready

if (-not (Test-Path (Join-Path $Root 'core-service\mvnw.cmd'))) {
    $failures.Add('Maven Wrapper do MX Core não encontrado.')
}
if (-not (Test-Path (Join-Path $Root 'clients\mx-app\package.json'))) {
    $failures.Add('Cliente Expo não encontrado.')
}
if (-not (Test-Path 'C:\Windows\Fonts\arial.ttf')) {
    $failures.Add('Fonte Arial não encontrada; configure MX_DOCUMENT_GENERATION_PDF_FONT_PATH antes de gerar PDFs.')
}

if (Get-Command pg_isready -ErrorAction SilentlyContinue) {
    & pg_isready --host 127.0.0.1 --port 5432 --dbname mx | Out-Host
    if ($LASTEXITCODE -ne 0) { $failures.Add('PostgreSQL indisponível em 127.0.0.1:5432') }
}
Test-MxEndpoint -Name 'Ollama' -Uri 'http://127.0.0.1:11434/api/tags' -Required
Test-MxEndpoint -Name 'Forge' -Uri 'http://127.0.0.1:7860/sdapi/v1/sd-models' -Required:$RequireImageGeneration

if ($failures.Count -gt 0) {
    Write-Error ("Pré-requisitos pendentes:`n - " + ($failures -join "`n - "))
    exit 1
}

Write-Host '[OK] Pré-requisitos mínimos disponíveis para iniciar o MX diretamente no Windows.'
