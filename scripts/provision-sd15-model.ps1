[CmdletBinding()]
param(
    [string]$Workspace = (Join-Path (Split-Path -Parent $PSScriptRoot) 'data\image-engine\stable-diffusion-webui-forge\models\Stable-diffusion')
)

$ErrorActionPreference = 'Stop'
$url = 'https://huggingface.co/sd-legacy/stable-diffusion-v1-5/resolve/main/v1-5-pruned-emaonly.safetensors'
$target = Join-Path $Workspace 'v1-5-pruned-emaonly.safetensors'
$partial = "$target.part"
$log = "$target.download.log"

New-Item -ItemType Directory -Force -Path $Workspace | Out-Null
if (Test-Path $target) {
    Write-Output "Modelo já existe: $target"
    exit 0
}

$arguments = @('--location', '--fail', '--retry', '5', '--retry-delay', '5', '--continue-at', '-', '--output', $partial, $url)
Write-Output "Iniciando download resumível do checkpoint SD 1.5."
& curl.exe @arguments 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) {
    throw "Download do checkpoint falhou com código $LASTEXITCODE. O arquivo parcial foi preservado em $partial."
}

if (-not (Test-Path $partial) -or (Get-Item $partial).Length -lt 1000000000) {
    throw "O arquivo baixado parece incompleto: $partial"
}
Move-Item -Force $partial $target
Write-Output "Modelo provisionado: $target"
