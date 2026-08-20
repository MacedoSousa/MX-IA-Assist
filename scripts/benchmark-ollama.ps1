[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:11434",
    [string]$Model = "qwen3",
    [int]$Warmup = 2,
    [int]$Samples = 20,
    [int]$TimeoutSec = 120,
    [string]$OutputFile = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ($Warmup -lt 0 -or $Samples -le 0 -or $TimeoutSec -le 0) {
    throw "Warmup must be >= 0, Samples and TimeoutSec must be positive."
}

$endpoint = "$($BaseUrl.TrimEnd('/'))/api/generate"
$prompts = @(
    "Responda em uma frase: o que e teste de software?",
    "Liste tres metricas de qualidade de software.",
    "Explique em uma frase a diferenca entre verificacao e validacao.",
    "Defina confiabilidade de software de forma objetiva."
)

function Invoke-OllamaSample {
    param(
        [Parameter(Mandatory = $true)][string]$Prompt,
        [Parameter(Mandatory = $true)][int]$Index
    )

    $payload = @{
        model  = $Model
        prompt = $Prompt
        stream = $false
        options = @{ temperature = 0 }
    } | ConvertTo-Json -Compress

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $response = Invoke-RestMethod -Method Post -Uri $endpoint -Body $payload -ContentType "application/json" -TimeoutSec $TimeoutSec
        $stopwatch.Stop()
        [pscustomobject]@{
            sample       = $Index
            success      = $true
            durationMs   = [math]::Round($stopwatch.Elapsed.TotalMilliseconds, 2)
            errorType    = $null
        }
    } catch {
        $stopwatch.Stop()
        [pscustomobject]@{
            sample       = $Index
            success      = $false
            durationMs   = [math]::Round($stopwatch.Elapsed.TotalMilliseconds, 2)
            errorType    = $_.Exception.GetType().Name
        }
    }
}

Write-Host "Ollama benchmark: $endpoint | model=$Model | warmup=$Warmup | samples=$Samples | timeout=${TimeoutSec}s"

for ($warmupIndex = 1; $warmupIndex -le $Warmup; $warmupIndex++) {
    $null = Invoke-OllamaSample -Prompt $prompts[($warmupIndex - 1) % $prompts.Count] -Index $warmupIndex
    Write-Host "Warmup $warmupIndex/$Warmup concluido"
}

$results = @()
for ($sampleIndex = 1; $sampleIndex -le $Samples; $sampleIndex++) {
    $result = Invoke-OllamaSample -Prompt $prompts[($sampleIndex - 1) % $prompts.Count] -Index $sampleIndex
    $results += $result
    Write-Host ("Sample {0}/{1}: success={2}, durationMs={3}, errorType={4}" -f $sampleIndex, $Samples, $result.success, $result.durationMs, $result.errorType)
}

$successful = @($results | Where-Object { $_.success } | ForEach-Object { [double]$_.durationMs } | Sort-Object)
$failed = @($results | Where-Object { -not $_.success })

function Get-Percentile {
    param(
        [Parameter(Mandatory = $true)][double[]]$Values,
        [Parameter(Mandatory = $true)][double]$Percentile
    )

    if ($Values.Count -eq 0) {
        return $null
    }
    $rank = [math]::Ceiling($Percentile * $Values.Count) - 1
    $rank = [math]::Max(0, [math]::Min($rank, $Values.Count - 1))
    return $Values[$rank]
}

$summary = [pscustomobject]@{
    generatedAt       = [DateTimeOffset]::Now.ToString("o")
    endpoint          = $endpoint
    model             = $Model
    warmup            = $Warmup
    samples           = $Samples
    timeoutSec        = $TimeoutSec
    successCount      = $successful.Count
    failureCount      = $failed.Count
    failureRate       = [math]::Round($failed.Count / [double]$Samples, 4)
    p50Ms             = Get-Percentile -Values $successful -Percentile 0.50
    p95Ms             = Get-Percentile -Values $successful -Percentile 0.95
    maxMs             = if ($successful.Count -gt 0) { $successful[-1] } else { $null }
    failureTypes      = @($failed | Group-Object errorType | ForEach-Object { [pscustomobject]@{ type = $_.Name; count = $_.Count } })
    samplesDetail     = $results
}

$summary | ConvertTo-Json -Depth 6
if ($OutputFile) {
    $summary | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $OutputFile -Encoding UTF8
    Write-Host "Resultado salvo em $OutputFile"
}
