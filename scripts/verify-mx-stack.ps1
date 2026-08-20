[CmdletBinding()]
param(
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) 'docs\evidence\executions'
}
New-Item -ItemType Directory -Force $OutputDirectory | Out-Null

function Get-HttpProbe([string]$Name, [string]$Url) {
    $started = Get-Date
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 15
        [PSCustomObject]@{ Name=$Name; Url=$Url; StatusCode=$response.StatusCode; Body=$response.Content; DurationMs=[int]((Get-Date)-$started).TotalMilliseconds; Passed=($response.StatusCode -eq 200) }
    } catch {
        [PSCustomObject]@{ Name=$Name; Url=$Url; StatusCode=$null; Body=$_.Exception.Message; DurationMs=[int]((Get-Date)-$started).TotalMilliseconds; Passed=$false }
    }
}

$probes = @(
    (Get-HttpProbe 'MX Core health' 'http://localhost:8080/actuator/health'),
    (Get-HttpProbe 'MX Web health' 'http://localhost:8082/health'),
    (Get-HttpProbe 'Ollama models' 'http://localhost:11434/api/tags'),
    (Get-HttpProbe 'Open WebUI health' 'http://localhost:3000/health')
)
$containers = @(docker ps -a --format '{{.Names}}|{{.Image}}|{{.Status}}|{{.Ports}}')
$restartPolicies = @(docker inspect --format '{{.Name}}|{{.HostConfig.RestartPolicy.Name}}' $(docker ps -aq) 2>$null)
$preservedPatterns = 'carpgultimatev8bserv|carpg|minecraft|teamspeak|deceasedcraft|core-service|medsync'
$preserved = @($containers | Where-Object { $_ -match $preservedPatterns })
$mx = @($containers | Where-Object { $_ -match '^mx-' })
$task = Get-ScheduledTask -TaskName 'MX-AI-Assistant-AlwaysOn' -ErrorAction SilentlyContinue
$taskInfo = if ($task) { $task | Get-ScheduledTaskInfo | Select-Object TaskName,LastRunTime,LastTaskResult,NumberOfMissedRuns } else { $null }

$report = [PSCustomObject]@{
    generatedAt = (Get-Date).ToString('o')
    machine = $env:COMPUTERNAME
    projectRoot = (Split-Path -Parent $PSScriptRoot)
    probes = $probes
    mxContainers = $mx
    preservedContainers = $preserved
    restartPolicies = $restartPolicies
    alwaysOnTask = $taskInfo
    allPassed = (@($probes | Where-Object { -not $_.Passed }).Count -eq 0 -and $null -ne $taskInfo)
}
$report | ConvertTo-Json -Depth 8 | Out-File (Join-Path $OutputDirectory 'mx-stack-verification.json') -Encoding utf8

$md = New-Object System.Text.StringBuilder
[void]$md.AppendLine('# MX Stack Verification')
[void]$md.AppendLine('')
[void]$md.AppendLine("Generated: $($report.generatedAt)")
[void]$md.AppendLine('')
[void]$md.AppendLine('| Probe | URL | HTTP | Result |')
[void]$md.AppendLine('|---|---|---:|---|')
foreach ($p in $probes) { [void]$md.AppendLine(('| {0} | ``{1}`` | {2} | {3} |' -f $p.Name, $p.Url, $p.StatusCode, $(if($p.Passed){'PASS'}else{'FAIL'}))) }
[void]$md.AppendLine('')
[void]$md.AppendLine('## MX containers')
[void]$md.AppendLine('')
[void]$md.AppendLine('```text')
$mx | ForEach-Object { [void]$md.AppendLine($_) }
[void]$md.AppendLine('```')
[void]$md.AppendLine('')
[void]$md.AppendLine('## Preserved containers')
[void]$md.AppendLine('')
[void]$md.AppendLine('```text')
$preserved | ForEach-Object { [void]$md.AppendLine($_) }
[void]$md.AppendLine('```')
[void]$md.AppendLine('')
[void]$md.AppendLine('## Always-on task')
[void]$md.AppendLine('')
[void]$md.AppendLine('```text')
if ($taskInfo) { $taskInfo | Out-String | ForEach-Object { [void]$md.AppendLine($_.TrimEnd()) } } else { [void]$md.AppendLine('NOT_INSTALLED') }
[void]$md.AppendLine('```')
[void]$md.AppendLine('')
[void]$md.AppendLine("Overall result: **$(if($report.allPassed){'PASS'}else{'REVIEW REQUIRED'})**")
$md.ToString() | Out-File (Join-Path $OutputDirectory 'mx-stack-verification.md') -Encoding utf8
Get-Content (Join-Path $OutputDirectory 'mx-stack-verification.md')
