# Create necessary directories for Docker volumes
$dirs = @(
    "d:\MX\data\postgres",
    "d:\MX\data\redis",
    "d:\MX\data\ollama",
    "d:\MX\data\open-webui",
    "d:\MX\logs"
)

foreach ($dir in $dirs) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "Created directory: $dir"
    } else {
        Write-Host "Directory already exists: $dir"
    }
}

Write-Host "All required directories are ready."
