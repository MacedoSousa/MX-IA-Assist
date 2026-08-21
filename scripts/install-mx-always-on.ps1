param(
    [switch]$Uninstall,
    [switch]$Status
)

$ErrorActionPreference = 'Stop'
$taskName = 'MX-AI-Assistant-AlwaysOn'
$projectRoot = Split-Path -Parent $PSScriptRoot
$launcher = Join-Path $projectRoot 'scripts\run_mx_maintenance.bat'

if ($Uninstall) {
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
    Write-Output "Tarefa removida: $taskName"
    exit 0
}

if ($Status) {
    $task = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    if ($null -eq $task) {
        Write-Output "NOT_INSTALLED"
    } else {
        $task | Get-ScheduledTaskInfo | Select-Object TaskName,LastRunTime,NextRunTime,LastTaskResult,NumberOfMissedRuns | Format-List
    }
    exit 0
}

if (-not (Test-Path $launcher)) {
    throw "Launcher não encontrado: $launcher"
}

$action = New-ScheduledTaskAction -Execute 'cmd.exe' -Argument "/c `"$launcher`"" -WorkingDirectory $projectRoot
$trigger = New-ScheduledTaskTrigger -AtLogOn -User $env:USERNAME
$settings = New-ScheduledTaskSettingsSet -StartWhenAvailable -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -MultipleInstances IgnoreNew -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1)
$principal = New-ScheduledTaskPrincipal -UserId $env:USERNAME -LogonType Interactive -RunLevel Limited
$task = New-ScheduledTask -Action $action -Trigger $trigger -Settings $settings -Principal $principal -Description 'Mantém o agente de memória e autoextensão controlada do MX em execução.'
Register-ScheduledTask -TaskName $taskName -InputObject $task -Force | Out-Null
Write-Output "Tarefa instalada: $taskName"
Write-Output "Launcher: $launcher"
Write-Output "Periodicidade padrão: 6 horas; altere MX_MAINTENANCE_INTERVAL_SECONDS para ajustar."
Write-Output "Push automático permanece desabilitado por padrão."
Write-Output "Os containers Docker usam restart unless-stopped e devem continuar ativos após reinício do daemon."
