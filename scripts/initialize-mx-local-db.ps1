[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [SecureString]$PostgresSuperPassword,
    [ValidateRange(1, 65535)]
    [int]$Port = 15432,
    [string]$Root = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'
$pgBin = 'C:\Program Files\PostgreSQL\16\bin'
$psql = Join-Path $pgBin 'psql.exe'
if (-not (Test-Path $psql)) { throw "psql.exe não encontrado em $pgBin" }

$plainPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($PostgresSuperPassword)
try {
    $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($plainPointer)
    $appPassword = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 40 | ForEach-Object {[char]$_})
    $sql = @"
DO `$`$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'mx') THEN
    ALTER ROLE mx WITH LOGIN PASSWORD '$appPassword';
  ELSE
    CREATE ROLE mx WITH LOGIN PASSWORD '$appPassword';
  END IF;
END
`$`$;
GRANT ALL PRIVILEGES ON DATABASE mx TO mx;
\connect mx
GRANT USAGE, CREATE ON SCHEMA public TO mx;
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA public TO mx;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO mx;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON TABLES TO mx;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
  GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO mx;
"@

    $sqlPath = Join-Path $env:TEMP 'mx-local-db-role.sql'
    [System.IO.File]::WriteAllText(
            $sqlPath,
            $sql,
            (New-Object System.Text.UTF8Encoding($false))
    )
    & $psql -w -v ON_ERROR_STOP=1 -h 127.0.0.1 -p $Port -U postgres -d postgres -f $sqlPath
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível criar a conta de aplicação MX.' }
    Remove-Item -Force $sqlPath -ErrorAction SilentlyContinue

    $localDirectory = Join-Path $Root 'scripts\local'
    New-Item -ItemType Directory -Force -Path $localDirectory | Out-Null
    $envPath = Join-Path $localDirectory 'mx-local.env'
    $environmentText = "SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:$Port/mx`n" +
            "SPRING_DATASOURCE_USERNAME=mx`n" +
            "SPRING_DATASOURCE_PASSWORD=$appPassword`n"
    [System.IO.File]::WriteAllText(
            $envPath,
            $environmentText,
            (New-Object System.Text.UTF8Encoding($false))
    )
    & icacls.exe $envPath /inheritance:r /grant:r "$env:USERNAME`:(R,W)" | Out-Null
    Write-Host "Conta de aplicação configurada e segredo local protegido em $envPath"
}
finally {
    if ($plainPointer -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($plainPointer) }
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}
