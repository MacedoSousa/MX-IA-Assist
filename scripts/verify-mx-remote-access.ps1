[CmdletBinding()]
param(
    [string]$Hostname = 'mx-ai.taila61bd3.ts.net',
    [string]$DuckDnsHostname = 'mx-ai.duckdns.org',
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) 'docs\evidence\executions\remote-access'
}
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

$tailscalePath = $null
$tailscaleCommand = Get-Command tailscale.exe -ErrorAction SilentlyContinue
if ($tailscaleCommand) {
    $tailscalePath = $tailscaleCommand.Source
} else {
    $candidate = 'C:\Program Files\Tailscale\tailscale.exe'
    if (Test-Path $candidate) { $tailscalePath = $candidate }
}

$rows = [System.Collections.Generic.List[object]]::new()
function Add-Check([string]$Name, [string]$Status, [string]$Details) {
    $rows.Add([pscustomobject]@{ Check = $Name; Status = $Status; Details = $Details })
}

if (-not $tailscalePath) {
    Add-Check 'tailscale-installed' 'FAIL' 'Tailscale binary not found.'
} else {
    $statusText = (& $tailscalePath status 2>&1 | Out-String).Trim()
    $serveText = (& $tailscalePath serve status 2>&1 | Out-String).Trim()
    $isConnected = $statusText -notmatch 'Logged out|No machine|error'
    $isTailnetOnly = $serveText -match 'tailnet only'
    $isMxProxy = $serveText -match [regex]::Escape('http://127.0.0.1:8082')
    Add-Check 'tailscale-connected' ($(if ($isConnected) {'PASS'} else {'FAIL'})) 'Connected state checked without storing account tokens.'
    Add-Check 'serve-tailnet-only' ($(if ($isTailnetOnly) {'PASS'} else {'FAIL'})) 'Serve must report tailnet only; Funnel must remain disabled.'
    Add-Check 'serve-mx-proxy' ($(if ($isMxProxy) {'PASS'} else {'FAIL'})) 'Serve target must be the local MX web port 8082.'
    $statusText | Set-Content -Encoding utf8 (Join-Path $OutputDirectory 'tailscale-status.txt')
    $serveText | Set-Content -Encoding utf8 (Join-Path $OutputDirectory 'tailscale-serve-status.txt')
}

try {
    $response = Invoke-WebRequest -Uri ("https://{0}/" -f $Hostname) -UseBasicParsing -TimeoutSec 20
    Add-Check 'mx-https' ($(if ([int]$response.StatusCode -eq 200) {'PASS'} else {'FAIL'})) ("HTTP {0}; content type {1}." -f [int]$response.StatusCode, $response.Headers['Content-Type'])
} catch {
    Add-Check 'mx-https' 'FAIL' $_.Exception.Message
}

try {
    $dns = Resolve-DnsName $DuckDnsHostname -ErrorAction Stop | Where-Object { $_.Type -in 'A','AAAA','CNAME' }
    Add-Check 'duckdns-resolves' ($(if ($dns) {'PASS'} else {'FAIL'})) 'The DuckDNS record resolves; exact public address is intentionally omitted.'
} catch {
    Add-Check 'duckdns-resolves' 'FAIL' $_.Exception.Message
}

$rows | Export-Csv -NoTypeInformation -Encoding utf8 (Join-Path $OutputDirectory 'remote-access-check.csv')
$markdown = @(
    '# MX remote-access verification',
    '',
    ('Generated: {0}' -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss K')),
    '',
    '| Check | Status | Details |',
    '|---|---|---|'
)
foreach ($row in $rows) {
    $safeDetails = ($row.Details -replace '\|', '\\|')
    $markdown += ('| {0} | {1} | {2} |' -f $row.Check, $row.Status, $safeDetails)
}
$markdown += @('', 'Security note: no tokens, passwords or exact public IP addresses are stored in this evidence.')
$markdown -join "`n" | Set-Content -Encoding utf8 (Join-Path $OutputDirectory 'remote-access-check.md')
Get-Content (Join-Path $OutputDirectory 'remote-access-check.md')
