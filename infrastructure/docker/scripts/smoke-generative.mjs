const baseUrl = (process.env.MX_API_BASE_URL ?? "http://127.0.0.1:8080").replace(/\/$/, "");
const email = process.env.MX_SMOKE_EMAIL;
const password = process.env.MX_SMOKE_PASSWORD;
const requestTimeoutMs = Number.parseInt(process.env.MX_SMOKE_TIMEOUT_MS ?? "300000", 10);

if (!email || !password) {
  throw new Error("Defina MX_SMOKE_EMAIL e MX_SMOKE_PASSWORD antes de executar o smoke test.");
}

async function request(path, options = {}, token) {
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {}),
    },
    signal: AbortSignal.timeout(requestTimeoutMs),
  });
  if (!response.ok) {
    const detail = await response.text();
    throw new Error(`${path} retornou HTTP ${response.status}: ${detail.slice(0, 300)}`);
  }
  return response;
}

function assertAttachment(attachment, expectedType) {
  if (!attachment?.id || attachment.contentType !== expectedType || !attachment.size) {
    throw new Error(`Resposta de mídia inválida para ${expectedType}.`);
  }
}

console.log("[smoke] autenticando");
const login = await request("/api/auth/login", {
  method: "POST",
  body: JSON.stringify({ email, password, deviceName: "mx-generative-smoke" }),
});
const { token } = await login.json();
if (!token) throw new Error("Autenticação não retornou token.");

console.log("[smoke] gerando imagem");
const image = await request("/api/v1/media/images", {
  method: "POST",
  body: JSON.stringify({
    prompt: "uma composição abstrata minimalista em azul e verde, sem texto",
    width: 512,
    height: 512,
  }),
}, token);
const imageAttachment = await image.json();
assertAttachment(imageAttachment, "image/png");

console.log("[smoke] gerando vídeo");
const video = await request("/api/v1/media/videos", {
  method: "POST",
  body: JSON.stringify({
    prompt: "uma paisagem abstrata serena, sem texto",
    durationSeconds: 2,
    width: 512,
    height: 512,
  }),
}, token);
const videoAttachment = await video.json();
assertAttachment(videoAttachment, "video/mp4");

console.log("[smoke] gerando documento");
const document = await request("/api/v1/media/documents", {
  method: "POST",
  body: JSON.stringify({
    prompt: "Crie uma nota curta de planejamento com objetivo, três ações e próximos passos.",
    title: "smoke-generative",
    format: "PDF",
  }),
}, token);
const documentAttachment = await document.json();
assertAttachment(documentAttachment, "application/pdf");

console.log("[smoke] validando download do PDF");
const downloaded = await request(`/api/v1/attachments/${encodeURIComponent(documentAttachment.id)}`, {}, token);
const pdfHeader = Buffer.from(await downloaded.arrayBuffer()).subarray(0, 4).toString("ascii");
if (pdfHeader !== "%PDF") throw new Error("O documento baixado não possui assinatura PDF válida.");

console.log(JSON.stringify({
  status: "ok",
  modalities: {
    image: { id: imageAttachment.id, contentType: imageAttachment.contentType, size: imageAttachment.size },
    video: { id: videoAttachment.id, contentType: videoAttachment.contentType, size: videoAttachment.size },
    document: { id: documentAttachment.id, contentType: documentAttachment.contentType, size: documentAttachment.size },
  },
}));
