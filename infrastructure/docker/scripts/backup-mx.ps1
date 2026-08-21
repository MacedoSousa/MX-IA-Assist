<#
  Reliable local MX backup with no Docker Compose stop.
  Creates a logical PostgreSQL dump plus archives of persistent MX memory and
  runtime configuration. Backup directories are ignored by Git by design.
#>
[CmdletBinding()]
param(
    [string]$DestinationRoot = "",
    [string]$PostgresContainer = "mx-postgres",
    [string]$PostgresDatabase = "mx",
    [string]$PostgresUser = "mx"
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
if ([string]::IsNullOrWhiteSpace($DestinationRoot)) {
    $DestinationRoot = Join-Path $projectRoot "backups"
}
$backupDirectory = Join-Path $DestinationRoot "mx-$timestamp"
New-Item -ItemType Directory -Force -Path $backupDirectory | Out-Null

function Invoke-Docker([string[]]$Arguments) {
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker command failed: docker $($Arguments -join ' ')"
    }
}

Write-Host "Creating MX backup at $backupDirectory"
Invoke-Docker @("inspect", "--format", "{{.State.Running}}", $PostgresContainer)
if ((& docker inspect --format "{{.State.Running}}" $PostgresContainer).Trim() -ne "true") {
    throw "PostgreSQL container '$PostgresContainer' is not running. No backup was created."
}

$temporaryDump = "/tmp/mx-backup-$timestamp.pgdump"
try {
    Invoke-Docker @("exec", $PostgresContainer, "pg_dump", "-U", $PostgresUser, "-d", $PostgresDatabase, "-Fc", "-f", $temporaryDump)
    Invoke-Docker @("cp", "${PostgresContainer}:$temporaryDump", (Join-Path $backupDirectory "postgres.pgdump"))
}
finally {
    & docker exec $PostgresContainer rm -f $temporaryDump 2>$null
}

$memoryArchive = Join-Path $backupDirectory "mx-core-memory.tar"
$runtimeArchive = Join-Path $backupDirectory "mx-runtime-config.tar"
& tar.exe -cf $memoryArchive -C $projectRoot data/attachments data/knowledge data/evolution
if ($LASTEXITCODE -ne 0) { throw "Could not archive MX persistent memory." }
& tar.exe -cf $runtimeArchive -C $projectRoot infrastructure/docker/compose infrastructure/docker/env infrastructure/docker/dsh
if ($LASTEXITCODE -ne 0) { throw "Could not archive MX runtime configuration." }

$manifestPath = Join-Path $backupDirectory "SHA256SUMS.txt"
@("postgres.pgdump", "mx-core-memory.tar", "mx-runtime-config.tar") |
    ForEach-Object {
        $hash = Get-FileHash -Algorithm SHA256 -Path (Join-Path $backupDirectory $_)
        "$($hash.Hash.ToLowerInvariant())  $($_)"
    } | Set-Content -Encoding utf8 $manifestPath

$restoreGuide = @"
# MX backup restore guide

This set was created online: the MX services were not stopped. PostgreSQL uses a
consistent logical dump; core attachments, knowledge and evolution are archived
separately. Verify SHA256SUMS.txt before restoring.

1. Stop MX only for the restoration window: `docker-compose.exe stop`.
2. Extract mx-core-memory.tar and mx-runtime-config.tar into the project root.
3. Restore postgres.pgdump into an empty MX PostgreSQL database with pg_restore.
4. Start services with `docker-compose.exe up -d --no-build`.
"@
Set-Content -Encoding utf8 -Path (Join-Path $backupDirectory "RESTORE.md") -Value $restoreGuide

Write-Host "Backup verified artifacts created: $backupDirectory"
Get-ChildItem $backupDirectory | Select-Object Name, Length, LastWriteTime
