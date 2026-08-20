[CmdletBinding()]
param(
    [string]$ReportsDirectory,
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($ReportsDirectory)) {
    $ReportsDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) 'core-service\target\surefire-reports'
}
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) 'docs\evidence\executions'
}
New-Item -ItemType Directory -Force $OutputDirectory | Out-Null
$files = @(Get-ChildItem $ReportsDirectory -Filter 'TEST-*.xml' -File -ErrorAction SilentlyContinue)
$tests = 0; $failures = 0; $errors = 0; $skipped = 0; $time = 0.0
$suites = foreach ($file in $files) {
    [xml]$xml = Get-Content $file.FullName -Raw
    $suite = $xml.testsuite
    $tests += [int]$suite.tests
    $failures += [int]$suite.failures
    $errors += [int]$suite.errors
    $skipped += [int]$suite.skipped
    $time += [double]$suite.time
    [PSCustomObject]@{ Name=$suite.name; Tests=[int]$suite.tests; Failures=[int]$suite.failures; Errors=[int]$suite.errors; Skipped=[int]$suite.skipped; Time=[double]$suite.time }
}
$result = [PSCustomObject]@{
    generatedAt = (Get-Date).ToString('o')
    reportsDirectory = $ReportsDirectory
    reportFiles = $files.Count
    tests = $tests
    failures = $failures
    errors = $errors
    skipped = $skipped
    durationSeconds = [Math]::Round($time, 3)
    passed = ($files.Count -gt 0 -and $failures -eq 0 -and $errors -eq 0)
}
$result | ConvertTo-Json | Out-File (Join-Path $OutputDirectory 'tdd-summary.json') -Encoding utf8
$lines = @(
    '# Backend TDD Summary',
    '',
    "Generated: $($result.generatedAt)",
    "Reports: $($result.reportFiles)",
    "Tests: $($result.tests)",
    "Failures: $($result.failures)",
    "Errors: $($result.errors)",
    "Skipped: $($result.skipped)",
    "DurationSeconds: $($result.durationSeconds)",
    "Result: $(if($result.passed){'PASS'}else{'REVIEW REQUIRED'})",
    '',
    '| Suite | Tests | Failures | Errors | Skipped | Seconds |',
    '|---|---:|---:|---:|---:|---:|'
)
foreach ($s in $suites) { $lines += ('| {0} | {1} | {2} | {3} | {4} | {5} |' -f $s.Name,$s.Tests,$s.Failures,$s.Errors,$s.Skipped,$s.Time) }
$lines | Out-File (Join-Path $OutputDirectory 'tdd-summary.md') -Encoding utf8
Get-Content (Join-Path $OutputDirectory 'tdd-summary.md')
if (-not $result.passed) { exit 1 }
