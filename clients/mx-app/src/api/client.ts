import {
  clearSession,
  readAccessToken,
  readRefreshToken,
  writeSession,
} from "../session/session";

const DEFAULT_API_URL = "http://localhost:8080";

function resolveDefaultApiUrl(): string {
  if (typeof window !== "undefined" && window.location?.origin) {
    return "";
  }
  return DEFAULT_API_URL;
}

function configuredApiUrl(): string {
  // The browser must always use Nginx's same-origin /api proxy. A build-time
  // localhost URL breaks LAN/mobile access and is rejected by Core CORS.
  if (typeof window !== "undefined" && window.location?.origin) {
    return "";
  }
  return process.env.EXPO_PUBLIC_MX_API_URL?.trim() || resolveDefaultApiUrl();
}

function inferAttachmentContentType(name: string, fallback = "application/octet-stream"): string {
  const extension = name.split(".").pop()?.toLowerCase();
  const byExtension: Record<string, string> = {
    pdf: "application/pdf",
    txt: "text/plain",
    md: "text/markdown",
    csv: "text/csv",
    json: "application/json",
    xml: "application/xml",
    html: "text/html",
    htm: "text/html",
    docx: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    xlsx: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    pptx: "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    mp3: "audio/mpeg",
    wav: "audio/wav",
    mp4: "video/mp4",
    webm: "video/webm",
    png: "image/png",
    jpg: "image/jpeg",
    jpeg: "image/jpeg",
    webp: "image/webp",
  };
  return byExtension[extension ?? ""] || fallback;
}

export type ChatMessage = {
  role: "USER" | "ASSISTANT";
  content: string;
};

export type LoginResponse = {
  id: string;
  email: string;
  name: string;
  role: string;
  token: string;
  expiresIn: number;
  sessionId: string;
  refreshToken: string;
};

export type RefreshResponse = {
  id: string;
  email: string;
  name: string;
  role: string;
  token: string;
  expiresIn: number;
  sessionId: string;
  refreshToken: string;
  refreshExpiresIn: number;
};

export type ExecutionRunStatus =
  | "RECEIVED"
  | "ROUTED"
  | "EXECUTING"
  | "VERIFYING"
  | "AWAITING_APPROVAL"
  | "COMPLETED"
  | "FAILED"
  | "CANCELLED";

export type ExecutionRunStatusResponse = {
  runId: string;
  correlationId: string;
  status: ExecutionRunStatus;
  skillName: string | null;
  pendingApproval: string | null;
  output: string | null;
  errorCode: string | null;
  receivedAt: string;
  finishedAt: string | null;
  pendingApprovalArguments: string | null;
  approvalExpiresAt: string | null;
  approvalNonceRequired: boolean;
  updatedAt: string;
  idempotencyKey: string | null;
};

export type ChatV1StreamStarted = {
  runId: string;
  correlationId: string;
};

export type ChatV1StreamToken = {
  delta: string;
};

export type ChatV1StreamError = {
  code: string;
  message: string;
};

export type ChatStreamHandlers = {
  onStarted?: (event: ChatV1StreamStarted) => void;
  onToken?: (event: ChatV1StreamToken) => void;
  onCompleted?: (event: ChatV1Response) => void;
  onError?: (event: ChatV1StreamError) => void;
};

export type UploadedAttachment = {
  id: string;
  filename: string;
  contentType: string;
  size: number;
  checksumSha256: string;
  createdAt: string;
};

export type AttachmentUploadInput =
  | { uri: string; name: string; type: string; size?: number; file?: Blob }
  | Blob & { name?: string; type?: string };

export type ConversationHistoryMessage = {
  id: string;
  role: "USER" | "ASSISTANT" | "SYSTEM";
  content: string;
  createdAt: string;
};

export type ConversationHistoryPage = {
  content: ConversationHistoryMessage[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type ConversationSummary = {
  id: string;
  title: string;
  summary: string | null;
  createdAt: string;
  topic: string;
  language: string;
  lastMessageAt: string;
  archivedAt: string | null;
  deletedAt: string | null;
};

export type ConversationMutation = {
  title?: string;
};

export type ConversationPage = {
  content: ConversationSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type UserPreference = {
  userId: string;
  learningStyle: string;
  knowledgeLevel: string;
  topicsOfInterest: string[];
  preferredLanguage: string;
  communicationStyle: string;
  vocabularyHints: string[];
  lastActiveAt: string | null;
};

export type UserPreferenceUpdate = {
  learningStyle?: string;
  knowledgeLevel?: string;
  topicsOfInterest?: string[];
  preferredLanguage?: string;
  communicationStyle?: string;
  vocabularyHints?: string[];
};

export type AudioTranscriptionResponse = {
  attachmentId: string;
  text: string;
  engine: string;
};

export type ImageGenerationInput = {
  prompt: string;
  width?: number;
  height?: number;
  negativePrompt?: string;
  seed?: number;
  cfgScale?: number;
};

export type VideoGenerationInput = {
  prompt: string;
  durationSeconds?: number;
  width?: number;
  height?: number;
};

export type DocumentGenerationInput = {
  prompt: string;
  title?: string;
  format?: "MARKDOWN" | "DOCX" | "PDF";
};

export type GeneratedMediaInput = {
  filename: string;
  contentType: string;
  data: string;
};

export type ChatV1Response = {
  status: "COMPLETED";
  correlationId: string | null;
  conversationId: string;
  userMessageId: string;
  assistantMessageId: string;
  skillName: string | null;
  answer: string;
  model: string | null;
  durationMs: number;
  runId: string;
};

export class MxApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly correlationId?: string;

  constructor(status: number, code: string, message: string, correlationId?: string) {
    super(message);
    this.name = "MxApiError";
    this.status = status;
    this.code = code;
    this.correlationId = correlationId;
  }
}

export class MxApiClient {
  private readonly baseUrl: string;
  private refreshPromise: Promise<string | null> | null = null;

  constructor(baseUrl = configuredApiUrl()) {
    this.baseUrl = baseUrl.replace(/\/$/, "");
  }

  async login(email: string, password: string, deviceName = "mx-app"): Promise<LoginResponse> {
    const response = await this.request<LoginResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password, deviceName }),
    }, false);

    if (response.refreshToken) {
      await writeSession(response.token, response.refreshToken);
    }
    return response;
  }

  async refresh(): Promise<RefreshResponse> {
    const refreshToken = await readRefreshToken();
    if (!refreshToken) {
      throw new MxApiError(401, "MX_SESSION_EXPIRED", "A sessão do MX expirou.");
    }

    const response = await this.request<RefreshResponse>("/api/auth/refresh", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
    }, false);

    await writeSession(response.token, response.refreshToken);
    return response;
  }

  async logout(): Promise<void> {
    try {
      await this.request<void>("/api/auth/logout", { method: "POST" }, false);
    } finally {
      await clearSession();
    }
  }

  async sendMessage(
    prompt: string,
    conversationId?: string,
    idempotencyKey?: string,
    attachmentIds: string[] = [],
  ): Promise<ChatV1Response> {
    return this.request<ChatV1Response>("/api/v1/conversations/messages", {
      method: "POST",
      body: JSON.stringify({ conversationId, prompt, idempotencyKey, attachmentIds }),
    });
  }

  async uploadAttachment(input: AttachmentUploadInput): Promise<UploadedAttachment> {
    const body = new FormData();
    if ("uri" in input) {
      const filename = input.name?.trim() || "attachment";
      const declaredType = input.type?.trim() || inferAttachmentContentType(filename);
      const hasBlob = typeof Blob !== "undefined" && input.file instanceof Blob;
      if (hasBlob) {
        const source = input.file as Blob;
        const file = new Blob([source], { type: source.type || declaredType });
        body.append("file", file, filename);
      } else if (typeof window !== "undefined") {
        try {
          const response = await fetch(input.uri);
          if (!response.ok) {
            throw new MxApiError(response.status, "MX_ATTACHMENT_READ_FAILED", `Não foi possível ler o arquivo selecionado (${response.status}).`);
          }
          const downloaded = await response.blob();
          if (!downloaded.size) {
            throw new MxApiError(422, "MX_ATTACHMENT_EMPTY", "O navegador retornou um arquivo vazio para o anexo.");
          }
          const contentType = downloaded.type && downloaded.type !== "application/octet-stream"
            ? downloaded.type
            : declaredType;
          const file = new Blob([downloaded], { type: contentType });
          body.append("file", file, filename);
        } catch (cause) {
          if (cause instanceof MxApiError) throw cause;
          const detail = cause instanceof Error && cause.message ? `: ${cause.message}` : "";
          throw new MxApiError(422, "MX_ATTACHMENT_READ_FAILED", `Não foi possível preparar o arquivo para envio${detail}.`);
        }
      } else {
        // React Native expects the native URI descriptor instead of a browser Blob.
        body.append("file", {
          uri: input.uri,
          name: filename,
          type: declaredType,
        } as unknown as Blob);
      }
    } else {
      const filename = input.name?.trim() || "attachment";
      const contentType = input.type || inferAttachmentContentType(filename);
      const file = typeof Blob !== "undefined" && input instanceof Blob && !input.type
        ? new Blob([input], { type: contentType })
        : input;
      body.append("file", file, filename);
    }
    return this.requestMultipart<UploadedAttachment>("/api/v1/attachments", body);
  }

  async downloadAttachment(attachmentId: string): Promise<Blob> {
    return this.requestBlob(`/api/v1/attachments/${encodeURIComponent(attachmentId)}`);
  }

  async listConversations(
    page = 0,
    size = 20,
    topic?: string,
    query?: string,
  ): Promise<ConversationPage> {
    const params = new URLSearchParams({
      page: String(Math.max(0, page)),
      size: String(Math.max(1, Math.min(size, 100))),
    });
    if (topic?.trim()) params.set("topic", topic.trim());
    if (query?.trim()) params.set("query", query.trim());
    return this.request<ConversationPage>(`/api/v1/conversations?${params.toString()}`, { method: "GET" });
  }

  async createConversation(title?: string): Promise<ConversationSummary> {
    return this.request<ConversationSummary>("/api/v1/conversations", {
      method: "POST",
      body: JSON.stringify({ title }),
    });
  }

  async renameConversation(conversationId: string, title: string): Promise<ConversationSummary> {
    return this.request<ConversationSummary>(`/api/v1/conversations/${encodeURIComponent(conversationId)}`, {
      method: "PATCH",
      body: JSON.stringify({ title }),
    });
  }

  async archiveConversation(conversationId: string): Promise<ConversationSummary> {
    return this.request<ConversationSummary>(
      `/api/v1/conversations/${encodeURIComponent(conversationId)}/archive`,
      { method: "POST" },
    );
  }

  async deleteConversation(conversationId: string): Promise<void> {
    await this.request<void>(`/api/v1/conversations/${encodeURIComponent(conversationId)}`, {
      method: "DELETE",
    });
  }

  async listDeletedConversations(): Promise<ConversationSummary[]> {
    return this.request<ConversationSummary[]>("/api/v1/conversations/trash", { method: "GET" });
  }

  async restoreConversation(conversationId: string): Promise<ConversationSummary> {
    return this.request<ConversationSummary>(
      `/api/v1/conversations/${encodeURIComponent(conversationId)}/restore`,
      { method: "POST" },
    );
  }

  async getPreferences(): Promise<UserPreference> {
    return this.request<UserPreference>("/api/v1/users/me/preferences", { method: "GET" });
  }

  async updatePreferences(update: UserPreferenceUpdate): Promise<UserPreference> {
    return this.request<UserPreference>("/api/v1/users/me/preferences", {
      method: "PUT",
      body: JSON.stringify(update),
    });
  }

  async getConversationHistory(
    conversationId: string,
    page = 0,
    size = 50,
  ): Promise<ConversationHistoryPage> {
    const params = new URLSearchParams({
      page: String(Math.max(0, page)),
      size: String(Math.max(1, Math.min(size, 100))),
    });
    return this.request<ConversationHistoryPage>(
      `/api/v1/conversations/${encodeURIComponent(conversationId)}/messages?${params.toString()}`,
      { method: "GET" },
    );
  }

  async transcribeAudio(attachmentId: string): Promise<AudioTranscriptionResponse> {
    return this.request<AudioTranscriptionResponse>(
      `/api/v1/media/audio/${encodeURIComponent(attachmentId)}/transcription`,
      { method: "POST" },
    );
  }

  async generateImage(input: ImageGenerationInput): Promise<UploadedAttachment> {
    return this.request<UploadedAttachment>("/api/v1/media/images", {
      method: "POST",
      body: JSON.stringify({
        prompt: input.prompt,
        width: input.width ?? 768,
        height: input.height ?? 768,
        negativePrompt: input.negativePrompt,
        seed: input.seed,
        cfgScale: input.cfgScale,
      }),
    });
  }

  async generateVideo(input: VideoGenerationInput): Promise<UploadedAttachment> {
    return this.request<UploadedAttachment>("/api/v1/media/videos", {
      method: "POST",
      body: JSON.stringify({
        prompt: input.prompt,
        durationSeconds: input.durationSeconds ?? 6,
        width: input.width ?? 1280,
        height: input.height ?? 720,
      }),
    });
  }

  async generateDocument(input: DocumentGenerationInput): Promise<UploadedAttachment> {
    return this.request<UploadedAttachment>("/api/v1/media/documents", {
      method: "POST",
      body: JSON.stringify({
        prompt: input.prompt,
        title: input.title,
        format: input.format ?? "MARKDOWN",
      }),
    });
  }

  async sendMessageStream(
    prompt: string,
    conversationId: string | undefined,
    idempotencyKey: string | undefined,
    handlers: ChatStreamHandlers,
    attachmentIds: string[] = [],
  ): Promise<ChatV1Response> {
    return this.requestStream(
      "/api/v1/conversations/messages/stream",
      {
        method: "POST",
        body: JSON.stringify({ conversationId, prompt, idempotencyKey, attachmentIds }),
      },
      handlers,
    );
  }

  async getRun(runId: string): Promise<ExecutionRunStatusResponse> {
    return this.request<ExecutionRunStatusResponse>(`/api/v1/runs/${encodeURIComponent(runId)}`, {
      method: "GET",
    });
  }

  async listRuns(updatedSince?: string, limit = 100): Promise<ExecutionRunStatusResponse[]> {
    const params = new URLSearchParams({ limit: String(Math.max(1, Math.min(limit, 100))) });
    if (updatedSince) {
      params.set("updatedSince", updatedSince);
    }
    return this.request<ExecutionRunStatusResponse[]>(`/api/v1/runs?${params.toString()}`, {
      method: "GET",
    });
  }

  async approveRun(runId: string, approvalNonce?: string): Promise<ExecutionRunStatusResponse> {
    return this.request<ExecutionRunStatusResponse>(
      `/api/v1/runs/${encodeURIComponent(runId)}/approve`,
      {
        method: "POST",
        body: JSON.stringify(approvalNonce ? { approvalNonce } : {}),
      },
    );
  }

  async rejectRun(runId: string, reason: string): Promise<ExecutionRunStatusResponse> {
    return this.request<ExecutionRunStatusResponse>(
      `/api/v1/runs/${encodeURIComponent(runId)}/reject`,
      {
        method: "POST",
        body: JSON.stringify({ reason }),
      },
    );
  }

  private async requestStream(
    path: string,
    init: RequestInit,
    handlers: ChatStreamHandlers,
    retryOnUnauthorized = true,
  ): Promise<ChatV1Response> {
    const token = await readAccessToken();
    const headers = new Headers(init.headers);
    headers.set("Content-Type", "application/json");
    headers.set("Accept", "text/event-stream");
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${this.baseUrl}${path}`, { ...init, headers });
    if (!response.ok && response.status === 401 && retryOnUnauthorized) {
      const refreshedToken = await this.refreshAccessToken();
      if (refreshedToken) {
        return this.requestStream(path, init, handlers, false);
      }
    }

    if (!response.ok) {
      const payload = await response.json().catch(() => null);
      throw new MxApiError(
        response.status,
        payload?.code ?? "MX_API_ERROR",
        payload?.message ?? "Não foi possível iniciar o streaming.",
        payload?.correlationId,
      );
    }

    if (!response.body) {
      throw new MxApiError(502, "MX_STREAM_UNAVAILABLE", "O canal de streaming não está disponível.");
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";
    let completed: ChatV1Response | null = null;

    const dispatch = (rawEvent: string) => {
      let eventName = "message";
      const dataLines: string[] = [];
      for (const line of rawEvent.split(/\r?\n/)) {
        if (line.startsWith("event:")) {
          eventName = line.slice("event:".length).trim();
        } else if (line.startsWith("data:")) {
          dataLines.push(line.slice("data:".length).trimStart());
        }
      }
      if (dataLines.length === 0) {
        return;
      }

      const payload = JSON.parse(dataLines.join("\n")) as unknown;
      switch (eventName) {
        case "started":
          handlers.onStarted?.(payload as ChatV1StreamStarted);
          break;
        case "token":
          handlers.onToken?.(payload as ChatV1StreamToken);
          break;
        case "completed":
          completed = payload as ChatV1Response;
          handlers.onCompleted?.(completed);
          break;
        case "error": {
          const error = payload as ChatV1StreamError;
          handlers.onError?.(error);
          throw new MxApiError(500, error.code, error.message);
        }
        default:
          break;
      }
    };

    const consume = (text: string) => {
      buffer += text;
      const events = buffer.split(/\r?\n\r?\n/);
      buffer = events.pop() ?? "";
      for (const event of events) {
        dispatch(event);
      }
    };

    while (true) {
      const { value, done } = await reader.read();
      if (done) {
        consume(decoder.decode());
        if (buffer.trim()) {
          dispatch(buffer);
        }
        break;
      }
      consume(decoder.decode(value, { stream: true }));
    }

    if (!completed) {
      throw new MxApiError(502, "MX_STREAM_INCOMPLETE", "O streaming foi encerrado antes da resposta final.");
    }
    return completed;
  }

  private async refreshAccessToken(): Promise<string | null> {
    if (!this.refreshPromise) {
      this.refreshPromise = this.refresh()
        .then((response) => response.token)
        .catch(async () => {
          await clearSession();
          return null;
        })
        .finally(() => {
          this.refreshPromise = null;
        });
    }

    return this.refreshPromise;
  }

  private async requestMultipart<T>(path: string, body: FormData, retryOnUnauthorized = true): Promise<T> {
    const token = await readAccessToken();
    const headers = new Headers();
    headers.set("Accept", "application/json");
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "POST",
      headers,
      body,
    });
    if (!response.ok && response.status === 401 && retryOnUnauthorized) {
      const refreshedToken = await this.refreshAccessToken();
      if (refreshedToken) {
        return this.requestMultipart(path, body, false);
      }
    }
    if (!response.ok) {
      const raw = await response.text().catch(() => "");
      let payload: { code?: string; message?: string; correlationId?: string } | null = null;
      try {
        payload = raw ? JSON.parse(raw) : null;
      } catch {
        payload = null;
      }
      const fallbackDetail = raw.replace(/<[^>]+>/g, " ").replace(/\s+/g, " ").trim().slice(0, 240);
      throw new MxApiError(
        response.status,
        payload?.code ?? "MX_API_ERROR",
        payload?.message ?? (fallbackDetail || "Não foi possível enviar o anexo."),
        payload?.correlationId,
      );
    }
    return response.json() as Promise<T>;
  }

  private async requestBlob(path: string, retryOnUnauthorized = true): Promise<Blob> {
    const token = await readAccessToken();
    const headers = new Headers({ Accept: "application/octet-stream" });
    if (token) headers.set("Authorization", `Bearer ${token}`);
    const response = await fetch(`${this.baseUrl}${path}`, { method: "GET", headers });
    if (!response.ok && response.status === 401 && retryOnUnauthorized) {
      const refreshedToken = await this.refreshAccessToken();
      if (refreshedToken) return this.requestBlob(path, false);
    }
    if (!response.ok) {
      const payload = await response.json().catch(() => null);
      throw new MxApiError(response.status, payload?.code ?? "MX_API_ERROR", payload?.message ?? "Não foi possível baixar o anexo.");
    }
    return response.blob();
  }

  private async request<T>(path: string, init: RequestInit, retryOnUnauthorized = true): Promise<T> {
    const token = await readAccessToken();
    const headers = new Headers(init.headers);
    headers.set("Content-Type", "application/json");
    headers.set("Accept", "application/json");
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      ...init,
      headers,
    });
    const payload = await response.json().catch(() => null);

    if (!response.ok && response.status === 401 && retryOnUnauthorized && path !== "/api/auth/refresh") {
      const refreshedToken = await this.refreshAccessToken();
      if (refreshedToken) {
        return this.request<T>(path, init, false);
      }
    }

    if (!response.ok) {
      throw new MxApiError(
        response.status,
        payload?.code ?? "MX_API_ERROR",
        payload?.message ?? payload?.detail ?? "Não foi possível concluir a operação.",
        payload?.correlationId,
      );
    }

    return payload as T;
  }
}

export const mxApi = new MxApiClient();
