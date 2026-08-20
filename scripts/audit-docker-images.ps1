$ErrorActionPreference = 'Stop'
$evidenceDir = Join-Path $PSScriptRoot '..\docs\evidence\docker-inventory'
New-Item -ItemType Directory -Force $evidenceDir | Out-Null

$usedRefs = @(docker ps -a --format '{{.Image}}' | Where-Object { $_ -and $_.Trim() } | Sort-Object -Unique)
$all = @(docker images --format '{{.Repository}}:{{.Tag}}|{{.ID}}|{{.Size}}|{{.CreatedSince}}' | Where-Object { $_ -and $_.Trim() })

$usedRepoNames = @(
  'carpgultimatev8bserv', 'minecraft', 'teamspeak', 'deceasedcraft',
  'core-service', 'medsync', 'compose', 'mx-', 'postgres', 'redis',
  'ollama', 'open-webui', 'mysql', 'nginx', 'grafana', 'prometheus',
  'loki', 'minio', 'cloudflared', 'node-exporter', 'exporter', 'sinusbot'
)

$candidates = foreach ($line in $all) {
  $parts = $line -split '\|', 4
  $ref = $parts[0]
  $id = $parts[1]
  $isUsedByRef = $usedRefs | Where-Object { $_ -eq $ref }
  $isProtected = $usedRepoNames | Where-Object { $ref.ToLowerInvariant().Contains($_) }
  if (-not $isUsedByRef -and -not $isProtected) {
    [PSCustomObject]@{ Image=$ref; Id=$id; Size=$parts[2]; Created=$parts[3] }
  }
}

$candidates | Format-Table -AutoSize | Out-File (Join-Path $evidenceDir 'image-cleanup-candidates.txt') -Encoding utf8
[PSCustomObject]@{
  GeneratedAt = (Get-Date).ToString('o')
  RunningOrStoppedContainerImages = $usedRefs.Count
  TotalImageRows = $all.Count
  CandidateRows = @($candidates).Count
  Policy = 'Never delete images referenced by any container or matching an explicitly preserved service family; volumes are never pruned by this script.'
} | ConvertTo-Json | Out-File (Join-Path $evidenceDir 'image-cleanup-policy.json') -Encoding utf8
Get-Content (Join-Path $evidenceDir 'image-cleanup-candidates.txt')
