[CmdletBinding()]
param(
    [switch]$Uninstall,
    [switch]$Status
)

$ErrorActionPreference = 'Stop'
$taskName = 'MX-AI-Assistant-AlwaysOn'
$projectRoot = Split-Path -Parent $PSScriptRoot
$launcher = Join-Path $projectRoot 'start.bat'

if (-not (Test-Path $launcher)) {
    throw "Launcher não encontrado: $launcher"
}

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

$action = New-ScheduledTaskAction -Execute 'cmd.exe' -Argument "/c `"$launcher`"" -WorkingDirectory $projectRoot
$trigger = New-ScheduledTaskTrigger -AtLogOn -User $env:USERNAME
$settings = New-ScheduledTaskSettingsSet -StartWhenAvailable -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -MultipleInstances IgnoreNew -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1)
$principal = New-ScheduledTaskPrincipal -UserId $env:USERNAME -LogonType Interactive -RunLevel Limited
$task = New-ScheduledTask -Action $action -Trigger $trigger -Settings $settings -Principal $principal -Description 'Inicia o stack Docker local do MX Core sem modificar outros servidores.'
Register-ScheduledTask -TaskName $taskName -InputObject $task -Force | Out-Null
Write-Output "Tarefa instalada: $taskName"
Write-Output "Launcher: $launcher"
Write-Output "A política Docker restart: unless-stopped mantém os containers após reinício do daemon."
Write-Output "O Windows e o Docker Desktop precisam permanecer ligados para disponibilidade contínua."
