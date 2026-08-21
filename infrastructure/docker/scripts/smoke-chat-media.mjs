const baseUrl = (process.env.MX_API_BASE_URL ?? "http://127.0.0.1:8080").replace(/\/$/, "");
const email = process.env.MX_SMOKE_EMAIL;
const password = process.env.MX_SMOKE_PASSWORD;
const prompt = process.env.MX_CHAT_MEDIA_PROMPT
  ?? "Gere uma imagem de um hamster dirigindo um carro vermelho em uma estrada ensolarada, sem texto.";
const timeoutMs = Number.parseInt(process.env.MX_SMOKE_TIMEOUT_MS ?? "300000", 10);

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
    signal: AbortSignal.timeout(timeoutMs),
  });
  if (!response.ok) {
    throw new Error(`${path} retornou HTTP ${response.status}: ${(await response.text()).slice(0, 300)}`);
  }
  return response;
}

console.log("[chat-media] autenticando");
const login = await request("/api/auth/login", {
  method: "POST",
  body: JSON.stringify({ email, password, deviceName: "mx-chat-media-smoke" }),
});
const { token } = await login.json();
if (!token) throw new Error("Autenticação não retornou token.");

console.log("[chat-media] solicitando imagem pelo chat");
const message = await request("/api/v1/conversations/messages", {
  method: "POST",
  body: JSON.stringify({ prompt, idempotencyKey: `chat-media-${crypto.randomUUID()}`, attachmentIds: [] }),
}, token);
const response = await message.json();

if (response.status !== "COMPLETED" || response.skillName !== "media") {
  throw new Error(`Roteamento inválido: skill=${response.skillName ?? "ausente"}, status=${response.status ?? "ausente"}.`);
}
if (!/Gerei a imagem solicitada/i.test(response.answer ?? "")) {
  throw new Error("A resposta do chat não confirmou o artefato de imagem gerado.");
}

console.log(JSON.stringify({
  status: "ok",
  skillName: response.skillName,
  conversationId: response.conversationId,
  runId: response.runId,
  answer: response.answer,
}));
