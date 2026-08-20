[CmdletBinding()]
param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) 'docs\evidence\executions'
}
New-Item -ItemType Directory -Force $OutputDirectory | Out-Null
$stamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$email = "mx.e2e.$stamp@example.local"
$password = 'MX-E2E-Local-2026!'
$started = Get-Date

try {
    $register = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/register" -ContentType 'application/json' -Body (@{
        name = 'MX E2E Test'
        email = $email
        password = $password
    } | ConvertTo-Json)

    $login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/login" -ContentType 'application/json' -Body (@{
        email = $email
        password = $password
        deviceName = 'e2e-script'
    } | ConvertTo-Json)

    $headers = @{ Authorization = "Bearer $($login.token)" }
    $chat = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/conversations/messages" -Headers $headers -ContentType 'application/json' -Body (@{
        prompt = 'Responda exatamente: MX E2E OK'
        idempotencyKey = "e2e-$stamp"
    } | ConvertTo-Json)

    $result = [PSCustomObject]@{
        generatedAt = (Get-Date).ToString('o')
        durationMs = [int]((Get-Date)-$started).TotalMilliseconds
        baseUrl = $BaseUrl
        registeredUserId = $register.id
        email = $email
        sessionId = $login.sessionId
        chatStatus = $chat.status
        skillName = $chat.skillName
        runId = $chat.runId
        answer = $chat.answer
        passed = ($chat.status -eq 'COMPLETED' -and $chat.answer -match 'MX E2E OK')
    }
} catch {
    $result = [PSCustomObject]@{
        generatedAt = (Get-Date).ToString('o')
        durationMs = [int]((Get-Date)-$started).TotalMilliseconds
        baseUrl = $BaseUrl
        email = $email
        passed = $false
        error = $_.Exception.Message
    }
}

$result | ConvertTo-Json -Depth 8 | Out-File (Join-Path $OutputDirectory 'e2e-auth-chat.json') -Encoding utf8
$result | ConvertTo-Json -Depth 8
if (-not $result.passed) { exit 1 }
