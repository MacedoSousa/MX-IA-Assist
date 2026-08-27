[CmdletBinding()]
param(
    [string]$Root = 'D:\MX',
    [switch]$Remove
)

$ErrorActionPreference = 'Stop'
$taskName = 'MX - Ollama Native'

if ($Remove) {
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
    Write-Host "Tarefa removida: $taskName"
    exit 0
}

$launcher = Join-Path $Root 'scripts\start-ollama-local.ps1'
if (-not (Test-Path -LiteralPath $launcher)) { throw "Launcher não encontrado: $launcher" }

$currentUser = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
$arguments = "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File `"$launcher`""
$action = New-ScheduledTaskAction -Execute 'powershell.exe' -Argument $arguments
$trigger = New-ScheduledTaskTrigger -AtLogOn -User $currentUser
$settings = New-ScheduledTaskSettingsSet -StartWhenAvailable -MultipleInstances IgnoreNew -ExecutionTimeLimit (New-TimeSpan -Minutes 5)
Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Settings $settings -User $currentUser -RunLevel Limited -Force | Out-Null

Get-ScheduledTask -TaskName $taskName | Select-Object TaskName, State
Write-Host 'Ollama nativo será iniciado no próximo logon. O Core permanece sob controle manual até o corte formal do Docker.'
