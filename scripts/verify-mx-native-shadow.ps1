param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [Parameter(Mandatory = $true)][string]$LoginEmail,
    [Parameter(Mandatory = $true)][string]$LoginPassword,
    [switch]$TestImageGeneration,
    [switch]$TestVideoGeneration,
    [switch]$TestAttachmentUpload,
    [switch]$TestPdfGeneration,
    [string]$AttachmentPath = 'D:\MX\mx-attachment-context.pdf'
)

$loginBody = @{ email = $LoginEmail; password = $LoginPassword } | ConvertTo-Json -Compress
$login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/login" -ContentType "application/json" -Body $loginBody -TimeoutSec 15
$headers = @{ Authorization = "Bearer $($login.token)" }
$attachmentStored = $false
$attachmentId = $null
$attachmentIds = New-Object 'System.Collections.Generic.List[string]'
if ($TestAttachmentUpload) {
    if (-not (Test-Path -LiteralPath $AttachmentPath)) { throw "PDF de validação não encontrado: $AttachmentPath" }
    $attachmentJson = & curl.exe --silent --show-error --fail -X POST "$BaseUrl/api/v1/attachments" -H "Authorization: Bearer $($login.token)" -F "file=@$AttachmentPath;type=application/pdf"
    $attachment = $attachmentJson | ConvertFrom-Json
    $attachmentId = $attachment.id
    $attachmentStored = $null -ne $attachmentId
    if ($attachmentStored) { [void]$attachmentIds.Add([string]$attachmentId) }
}
$idempotencyKey = if ($attachmentStored) { 'native-shadow-attachment-smoke-20260827' } else { 'native-shadow-smoke-20260827' }
$messageBody = @{
    prompt = "Responda somente: MX nativo pronto."
    idempotencyKey = $idempotencyKey
    attachmentIds = $attachmentIds
} | ConvertTo-Json -Compress
$reply = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/conversations/messages" -Headers $headers -ContentType "application/json" -Body $messageBody -TimeoutSec 180

$imageCreated = $false
if ($TestImageGeneration) {
    $imageBody = @{
        prompt = 'A small brass compass resting on a deep-blue linen desk pad, editorial product photograph, soft daylight, no text'
        width = 512
        height = 512
        negativePrompt = 'text, watermark, logo, distorted geometry, blurry'
        seed = 20260827
        cfgScale = 7.0
    } | ConvertTo-Json -Compress
    $image = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/media/images" -Headers $headers -ContentType "application/json" -Body $imageBody -TimeoutSec 300
    $imageCreated = $null -ne $image.id
}

$videoCreated = $false
if ($TestVideoGeneration) {
    $videoBody = @{
        prompt = 'A brass compass resting on a deep-blue linen desk pad, subtle camera movement, no text'
        durationSeconds = 2
        width = 512
        height = 512
    } | ConvertTo-Json -Compress
    $video = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/media/videos" -Headers $headers -ContentType "application/json" -Body $videoBody -TimeoutSec 300
    $videoCreated = $null -ne $video.id
}

$pdfCreated = $false
if ($TestPdfGeneration) {
    $documentBody = @{
        prompt = '# Verificação nativa`n`nDocumento gerado pelo MX Core em execução direta no Windows.'
        title = 'verificacao-nativa'
        format = 'PDF'
    } | ConvertTo-Json -Compress
    $document = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/media/documents" -Headers $headers -ContentType "application/json" -Body $documentBody -TimeoutSec 60
    $pdfCreated = $null -ne $document.id
}

[PSCustomObject]@{
    LoginEmail = $login.email
    AttachmentStored = $attachmentStored
    ConversationCreated = $null -ne $reply.conversationId
    ImageCreated = $imageCreated
    VideoCreated = $videoCreated
    PdfCreated = $pdfCreated
    ResponseProperties = ($reply.PSObject.Properties.Name -join ",")
} | ConvertTo-Json -Compress
