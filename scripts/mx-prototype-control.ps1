<#
MX prototype control: one local entry point for observability and explicitly bounded actions.
It never reads secret files, invokes free-form commands, alters Docker, publishes content,
or touches databases, backups, attachments, memory, or workspaces.
#>
param(
    [ValidateSet('Status', 'OpenMx', 'OpenGuestConsole', 'StartShadow', 'StopShadow')]
    [string]$Action = 'Status',
    [switch]$WithImageGeneration,
    [switch]$WithVideoGeneration
)

$ErrorActionPreference = 'Stop'
$root = 'D:\MX'
$mxUiUrl = 'http://127.0.0.1:8082'
$coreReferenceHealthUrl = 'http://127.0.0.1:8080/actuator/health'
$shadowHealthUrl = 'http://127.0.0.1:18080/actuator/health'

function Get-HttpStatus([string]$Url) {
    try {
        return (& curl.exe --silent --output NUL --write-out '%{http_code}' --max-time 3 $Url)
    } catch {
        return '000'
    }
}

function Get-ListenerPid([int]$Port) {
    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($listener) {
        return [int]$listener.OwningProcess
    }
    return $null
}

function Get-PostgresStatus {
    $command = Get-Command 'pg_isready.exe' -ErrorAction SilentlyContinue
    if (-not $command) {
        $postgresRoot = 'C:\Program Files\PostgreSQL'
        if (Test-Path -LiteralPath $postgresRoot) {
            $candidate = Get-ChildItem -LiteralPath $postgresRoot -Recurse -Filter 'pg_isready.exe' -ErrorAction SilentlyContinue |
                Select-Object -First 1
            if ($candidate) {
                $command = [PSCustomObject]@{ Source = $candidate.FullName }
            }
        }
    }
    if (-not $command) { return 'NOT_FOUND' }
    & $command.Source -h 127.0.0.1 -p 15432 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        return 'READY'
    }
    return 'NOT_READY'
}

function Get-PrototypeStatus {
    $dockerAvailable = $null -ne (Get-Command 'docker.exe' -ErrorAction SilentlyContinue)
    [PSCustomObject]@{
        mode = 'PROTOTYPE'
        primaryAccess = [PSCustomObject]@{
            label = 'MX UI com Core Docker de referência'
            url = $mxUiUrl
            coreHealthUrl = $coreReferenceHealthUrl
            persistence = 'PostgreSQL Docker preservado'
            note = 'O Core nativo em 18080 é shadow e não é a rota de uso diário.'
        }
        mxUi = [PSCustomObject]@{ url = $mxUiUrl; status = Get-HttpStatus "$mxUiUrl/" }
        coreShadow = [PSCustomObject]@{ healthUrl = $shadowHealthUrl; status = Get-HttpStatus $shadowHealthUrl; pid = Get-ListenerPid 18080 }
        coreReference = [PSCustomObject]@{ healthUrl = $coreReferenceHealthUrl; status = Get-HttpStatus $coreReferenceHealthUrl; pid = Get-ListenerPid 8080 }
        ollamaNative = [PSCustomObject]@{ url = 'http://127.0.0.1:11435'; status = Get-HttpStatus 'http://127.0.0.1:11435/api/tags' }
        forgeTransient = [PSCustomObject]@{ url = 'http://127.0.0.1:7860'; status = Get-HttpStatus 'http://127.0.0.1:7860/' }
        dshLoopback = [PSCustomObject]@{ url = 'http://127.0.0.1:3080'; status = Get-HttpStatus 'http://127.0.0.1:3080/' }
        postgresqlNative = Get-PostgresStatus
        dockerAvailable = $dockerAvailable
        safety = 'No Docker, database, backup, memory, attachment, workspace or publishing action was performed.'
    } | ConvertTo-Json -Depth 4
}

switch ($Action) {
    'OpenMx' {
        if ((Get-HttpStatus "$mxUiUrl/") -ne '200') {
            throw "A interface MX em $mxUiUrl não está respondendo com HTTP 200. Execute Status para diagnosticar."
        }
        Start-Process $mxUiUrl
        Write-Output "MX_UI_OPENED=$mxUiUrl"
    }
    'OpenGuestConsole' {
        $console = Join-Path $root 'scripts\mx-local-chat.ps1'
        if (-not (Test-Path -LiteralPath $console)) { throw 'Console local convidado não encontrado.' }
        Start-Process powershell.exe -ArgumentList @('-NoExit', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $console, '-Model', 'qwen3:8b')
        Write-Output 'MX_GUEST_CONSOLE_OPENED=qwen3:8b'
    }
    'StartShadow' {
        if ((Get-HttpStatus $shadowHealthUrl) -eq '200') {
            Write-Output 'CORE_SHADOW=ALREADY_HEALTHY'
            break
        }
        $launcher = Join-Path $root 'scripts\local\start-mx-core-shadow.ps1'
        if (-not (Test-Path -LiteralPath $launcher)) { throw 'Launcher do Core shadow não encontrado.' }
        & $launcher -WithImageGeneration:$WithImageGeneration -WithVideoGeneration:$WithVideoGeneration
        Write-Output 'CORE_SHADOW=START_REQUESTED'
    }
    'StopShadow' {
        $pid = Get-ListenerPid 18080
        if ($null -eq $pid) {
            Write-Output 'CORE_SHADOW=NOT_RUNNING'
            break
        }
        Stop-Process -Id $pid -Force
        Write-Output 'CORE_SHADOW=STOPPED'
    }
    default {
        Get-PrototypeStatus
    }
}
