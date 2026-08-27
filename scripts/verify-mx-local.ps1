[CmdletBinding()]
param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [switch]$RequireImageGeneration,
    [string]$OllamaUrl = 'http://127.0.0.1:11435',
    [int]$PostgresPort = 15432
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

function Add-PostgresToPath {
    $programFiles = [Environment]::GetFolderPath([Environment+SpecialFolder]::ProgramFiles)
    if ([string]::IsNullOrWhiteSpace($programFiles)) { $programFiles = 'C:\Program Files' }
    $preferred = Join-Path $programFiles 'PostgreSQL\16\bin\pg_isready.exe'
    $candidate = if (Test-Path $preferred) {
        $preferred
    } else {
        Get-ChildItem -Path (Join-Path $programFiles 'PostgreSQL') -Recurse -Filter 'pg_isready.exe' -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName
    }

    if ($candidate -and -not (Get-Command pg_isready -ErrorAction SilentlyContinue)) {
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
if (-not [string]::IsNullOrWhiteSpace($env:OLLAMA_URL)) { $OllamaUrl = $env:OLLAMA_URL }
if (-not [string]::IsNullOrWhiteSpace($env:MX_POSTGRES_PORT)) { $PostgresPort = [int]$env:MX_POSTGRES_PORT }
Test-MxCommand java
Test-MxCommand node
Test-MxCommand npm
Add-OllamaToPath
Add-PostgresToPath
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
    & pg_isready --host 127.0.0.1 --port $PostgresPort --dbname mx | Out-Host
    if ($LASTEXITCODE -ne 0) { $failures.Add("PostgreSQL indisponível em 127.0.0.1:$PostgresPort") }
}
Test-MxEndpoint -Name 'Ollama' -Uri "$OllamaUrl/api/tags" -Required
Test-MxEndpoint -Name 'Forge' -Uri 'http://127.0.0.1:7860/sdapi/v1/sd-models' -Required:$RequireImageGeneration

if ($failures.Count -gt 0) {
    foreach ($failure in $failures) { Write-Host "[FALHA] $failure" }
    throw 'Pré-requisitos pendentes. Corrija os itens acima e execute novamente.'
}

Write-Host '[OK] Pré-requisitos mínimos disponíveis para iniciar o MX diretamente no Windows.'
