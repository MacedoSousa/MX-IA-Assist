<#
MX local guest console: ephemeral, loopback-only conversation with local Ollama.
It never calls MX Core, reads credentials, stores history, accesses RAG, files,
attachments, workspaces, approved tools, web search, Docker, or publishing.
#>
param(
    [string]$Prompt,
    [ValidateSet('deepseek-r1:14b', 'qwen3:8b', 'gemma4:e4b')]
    [string]$Model = 'deepseek-r1:14b',
    [string]$OllamaUrl = 'http://127.0.0.1:11435'
)

$ErrorActionPreference = 'Stop'
$allowedOllamaUrl = 'http://127.0.0.1:11435'

if ($OllamaUrl.TrimEnd('/') -ne $allowedOllamaUrl) {
    throw 'O console convidado aceita somente Ollama local em http://127.0.0.1:11435.'
}

function Invoke-MxLocalGuestChat([string]$UserPrompt) {
    $text = $UserPrompt.Trim()
    if (-not $text) { return }

    $request = @{
        model = $Model
        stream = $false
        think = $false
        messages = @(
            @{
                role = 'system'
                content = 'Você é MX Console Local, uma conversa efêmera. Responda em português do Brasil quando apropriado. Não afirme possuir memória persistente, acesso a arquivos, anexos, banco de dados, terminal, internet, skills ou ferramentas. Para essas capacidades, oriente o usuário a entrar na interface autenticada do MX.'
            },
            @{ role = 'user'; content = $text }
        )
        options = @{ num_ctx = 4096 }
    } | ConvertTo-Json -Depth 6 -Compress

    try {
        $response = Invoke-RestMethod -Method Post -Uri "$allowedOllamaUrl/api/chat" -ContentType 'application/json' -Body $request -TimeoutSec 300
        $answer = [string]$response.message.content
        if (-not $answer.Trim()) { throw 'A resposta local veio vazia.' }
        Write-Output $answer.Trim()
    } catch {
        throw 'Não foi possível concluir a conversa local. Verifique o status com scripts\mx-prototype-control.ps1 e confirme que Ollama nativo está ativo.'
    }
}

if ($PSBoundParameters.ContainsKey('Prompt')) {
    Invoke-MxLocalGuestChat $Prompt
    exit 0
}

Write-Output 'MX Console Local · sessão efêmera · sem cadastro · digite /sair para encerrar'
while ($true) {
    $line = Read-Host 'Você'
    if ($null -eq $line -or $line.Trim() -in @('/sair', '/exit', '/quit')) { break }
    Invoke-MxLocalGuestChat $line
}
