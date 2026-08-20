import {
  clearSession,
  readAccessToken,
  readRefreshToken,
  writeSession,
} from "../session/session";

const DEFAULT_API_URL = "http://localhost:8080";

function resolveDefaultApiUrl(): string {
  if (typeof window !== "undefined" && window.location?.hostname) {
    return `http://${window.location.hostname}:8080`;
  }
  return DEFAULT_API_URL;
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

  constructor(baseUrl = process.env.EXPO_PUBLIC_MX_API_URL ?? resolveDefaultApiUrl()) {
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
  ): Promise<ChatV1Response> {
    return this.request<ChatV1Response>("/api/v1/conversations/messages", {
      method: "POST",
      body: JSON.stringify({ conversationId, prompt, idempotencyKey }),
    });
  }

  async sendMessageStream(
    prompt: string,
    conversationId: string | undefined,
    idempotencyKey: string | undefined,
    handlers: ChatStreamHandlers,
  ): Promise<ChatV1Response> {
    return this.requestStream(
      "/api/v1/conversations/messages/stream",
      {
        method: "POST",
        body: JSON.stringify({ conversationId, prompt, idempotencyKey }),
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
      for (const line of rawEvent.split(/\\r?\\n/)) {
        if (line.startsWith("event:")) {
          eventName = line.slice("event:".length).trim();
        } else if (line.startsWith("data:")) {
          dataLines.push(line.slice("data:".length).trimStart());
        }
      }
      if (dataLines.length === 0) {
        return;
      }

      const payload = JSON.parse(dataLines.join("\\n")) as unknown;
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
      const events = buffer.split(/\\r?\\n\\r?\\n/);
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
        payload?.message ?? "Não foi possível concluir a operação.",
        payload?.correlationId,
      );
    }

    return payload as T;
  }
}

export const mxApi = new MxApiClient();
