[CmdletBinding()]
param([switch]$Apply)

$ErrorActionPreference = 'Stop'
$preservedName = '^(mx-|medsync-|deceasedcraft|minecraft$|ts3-|carpg-|core-service|compose$)'
$containers = @(docker ps -a --format '{{.Names}}|{{.Image}}|{{.Status}}')
$candidates = foreach ($line in $containers) {
    $parts = $line -split '\|', 3
    $name = $parts[0]
    $status = $parts[2]
    if ($status -match '^Exited' -and $name -notmatch $preservedName -and $name -match '^[a-z]+_[a-z]+$') {
        $line
    }
}

Write-Output 'Containers candidatos (somente Exited e nomes automáticos):'
$candidates | ForEach-Object { Write-Output $_ }

if ($Apply -and @($candidates).Count -gt 0) {
    $names = @($candidates | ForEach-Object { ($_ -split '\|', 3)[0] })
    docker rm @names
    Write-Output ('RemovedContainers=' + $names.Count)
    docker image prune -a -f
    docker builder prune -f
    Write-Output 'Unused images and build cache pruned. Volumes and networks were not pruned.'
} else {
    Write-Output 'DryRun=true. Use -Apply only after reviewing this list.'
}
