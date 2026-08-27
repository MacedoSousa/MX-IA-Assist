[CmdletBinding()]
param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [switch]$WithImageGeneration,
    [switch]$OpenBrowser
)

$ErrorActionPreference = 'Stop'
$logs = Join-Path $Root 'logs'
New-Item -ItemType Directory -Force -Path $logs | Out-Null

& (Join-Path $PSScriptRoot 'verify-mx-local.ps1') -Root $Root -RequireImageGeneration:$WithImageGeneration
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue) {
    Write-Host '[INFO] MX Core já está escutando na porta 8080.'
} else {
    $coreLog = Join-Path $logs 'mx-core-console.log'
    $coreErrorLog = Join-Path $logs 'mx-core-error.log'
    Start-Process -FilePath (Join-Path $PSScriptRoot 'run-backend.bat') -WorkingDirectory $Root -RedirectStandardOutput $coreLog -RedirectStandardError $coreErrorLog
    Write-Host "[INFO] MX Core iniciado. Logs: $coreLog e $coreErrorLog"
}

$deadline = (Get-Date).AddSeconds(90)
while ((Get-Date) -lt $deadline) {
    try {
        if ((Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:8080/actuator/health' -TimeoutSec 3).StatusCode -eq 200) { break }
    } catch { Start-Sleep -Seconds 2 }
}
if ((Get-Date) -ge $deadline) { throw 'MX Core não respondeu ao healthcheck dentro de 90 segundos.' }

if (Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue) {
    Write-Host '[INFO] Expo Web já está escutando na porta 8081.'
} else {
    $webLog = Join-Path $logs 'mx-web-console.log'
    $webErrorLog = Join-Path $logs 'mx-web-error.log'
    Start-Process -FilePath (Join-Path $PSScriptRoot 'run-expo-web.bat') -WorkingDirectory $Root -RedirectStandardOutput $webLog -RedirectStandardError $webErrorLog
    Write-Host "[INFO] Expo Web iniciado. Logs: $webLog e $webErrorLog"
}

Write-Host 'MX local iniciado: http://localhost:8081'
if ($OpenBrowser) { Start-Process 'http://localhost:8081' }
