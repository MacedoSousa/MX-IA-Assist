Write-Host "Checking MX PostgreSQL..."

$result = docker exec mx-postgres pg_isready -U mx

if ($LASTEXITCODE -eq 0) {

    Write-Host "Database OK"

} else {

    Write-Host "Database unavailable"

    exit 1
}