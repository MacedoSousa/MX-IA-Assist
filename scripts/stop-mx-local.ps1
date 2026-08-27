[CmdletBinding()]
param()

$ports = @(8080, 8081)
foreach ($port in $ports) {
    Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object {
            Write-Host "Encerrando processo $_ que atende a porta $port"
            Stop-Process -Id $_ -ErrorAction Stop
        }
}
Write-Host 'Processos locais do MX encerrados. Serviços externos como PostgreSQL, Ollama e Forge não foram alterados.'
