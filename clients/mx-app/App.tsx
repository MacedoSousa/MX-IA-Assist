import { StatusBar } from "expo-status-bar";
import { useEffect, useMemo, useRef, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";

import {
  MxApiError,
  mxApi,
  type ChatMessage,
  type ExecutionRunStatusResponse,
} from "./src/api/client";
import {
  clearSession,
  readAccessToken,
  readConversationId,
  readDraftPrompt,
  readRunCursor,
  readRefreshToken,
  writeConversationId,
  writeDraftPrompt,
  writeRunCursor,
  writeSession,
} from "./src/session/session";

type ViewState = "checking" | "login" | "chat";

type RenderMessage = ChatMessage & { id: string };

function mergeRuns(current: ExecutionRunStatusResponse[], incoming: ExecutionRunStatusResponse[]) {
  const byId = new Map(current.map((run) => [run.runId, run]));
  incoming.forEach((run) => byId.set(run.runId, run));
  return Array.from(byId.values()).sort(
    (left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt),
  );
}

function newestUpdatedAt(runs: ExecutionRunStatusResponse[], current?: string): string | undefined {
  return runs.reduce<string | undefined>((latest, run) => {
    if (!latest || Date.parse(run.updatedAt) > Date.parse(latest)) return run.updatedAt;
    return latest;
  }, current);
}

export default function App() {
  const [viewState, setViewState] = useState<ViewState>("checking");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [prompt, setPrompt] = useState("");
  const [messages, setMessages] = useState<RenderMessage[]>([]);
  const [conversationId, setConversationId] = useState<string | undefined>();
  const [runs, setRuns] = useState<ExecutionRunStatusResponse[]>([]);
  const [activeRunId, setActiveRunId] = useState<string | null>(null);
  const [approvalNonce, setApprovalNonce] = useState("");
  const [busy, setBusy] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [online, setOnline] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const runCursorRef = useRef<string | undefined>(undefined);

  const activeRun = runs.find((run) => run.runId === activeRunId)
    ?? runs.find((run) => run.status === "AWAITING_APPROVAL")
    ?? null;

  useEffect(() => {
    async function restoreSession() {
      try {
        const savedConversationId = await readConversationId();
        const savedRunCursor = await readRunCursor();
        const draft = await readDraftPrompt();
        setConversationId(savedConversationId ?? undefined);
        runCursorRef.current = savedRunCursor ?? undefined;
        setPrompt(draft);

        const accessToken = await readAccessToken();
        if (accessToken) {
          setViewState("chat");
          return;
        }
        const refreshToken = await readRefreshToken();
        if (!refreshToken) {
          setViewState("login");
          return;
        }
        await mxApi.refresh();
        setViewState("chat");
      } catch {
        await clearSession();
        setViewState("login");
      }
    }
    void restoreSession();
  }, []);

  useEffect(() => {
    if (viewState !== "chat") return;
    let cancelled = false;

    const syncRuns = async () => {
      if (cancelled) return;
      setSyncing(true);
      try {
        const runsFromServer = await mxApi.listRuns(runCursorRef.current, 100);
        if (!cancelled) {
          setRuns((current) => mergeRuns(current, runsFromServer));
          const newest = runsFromServer[0];
          if (newest) setActiveRunId((current) => current ?? newest.runId);
          const nextCursor = newestUpdatedAt(runsFromServer, runCursorRef.current);
          if (nextCursor && nextCursor !== runCursorRef.current) {
            runCursorRef.current = nextCursor;
            await writeRunCursor(nextCursor);
          }
          setOnline(true);
        }
      } catch (cause) {
        if (!cancelled && !(cause instanceof MxApiError && cause.status === 401)) {
          setOnline(false);
        }
      } finally {
        if (!cancelled) setSyncing(false);
      }
    };

    void syncRuns();
    const timer = setInterval(() => void syncRuns(), 5000);
    return () => {
      cancelled = true;
      clearInterval(timer);
    };
  }, [viewState]);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const updateOnline = () => setOnline(window.navigator.onLine);
    updateOnline();
    window.addEventListener("online", updateOnline);
    window.addEventListener("offline", updateOnline);
    return () => {
      window.removeEventListener("online", updateOnline);
      window.removeEventListener("offline", updateOnline);
    };
  }, []);

  const subtitle = useMemo(() => {
    if (viewState === "checking") return "Preparando sua sessão local";
    if (viewState === "login") return "Seu assistente pessoal, em todos os seus dispositivos";
    if (!online) return "Offline: rascunho preservado; nenhuma tool será executada automaticamente";
    return syncing ? "Sincronizando runs e aprovações" : "O MX Core coordena as skills especialistas por você";
  }, [online, syncing, viewState]);

  async function handleLogin() {
    if (!email.trim() || !password) {
      setError("Informe e-mail e senha para entrar.");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const response = await mxApi.login(email.trim(), password);
      await writeSession(response.token, response.refreshToken);
      setViewState("chat");
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível entrar no MX.");
    } finally {
      setBusy(false);
    }
  }

  async function handleSend() {
    const value = prompt.trim();
    if (!value || busy || !online) return;

    const requestId = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
    const assistantId = `${requestId}-assistant`;
    setError(null);
    setBusy(true);
    setPrompt("");
    await writeDraftPrompt("");
    setMessages((current) => [
      ...current,
      { id: `${requestId}-user`, role: "USER", content: value },
      { id: assistantId, role: "ASSISTANT", content: "" },
    ]);

    try {
      const response = await mxApi.sendMessageStream(value, conversationId, requestId, {
        onStarted: ({ runId }) => setActiveRunId(runId),
        onToken: ({ delta }) => {
          setMessages((current) => current.map((message) =>
            message.id === assistantId ? { ...message, content: `${message.content}${delta}` } : message,
          ));
        },
      });
      setConversationId(response.conversationId);
      await writeConversationId(response.conversationId);
      setActiveRunId(response.runId);
      setMessages((current) => current.map((message) =>
        message.id === assistantId ? { ...message, content: response.answer } : message,
      ));
    } catch (cause) {
      setMessages((current) => current.filter((message) => message.id !== assistantId));
      await writeDraftPrompt(value);
      setOnline(false);
      if (cause instanceof MxApiError && cause.status === 401) {
        await clearSession();
        setViewState("login");
      }
      setError(cause instanceof MxApiError ? cause.message : "O MX não conseguiu concluir a mensagem.");
    } finally {
      setBusy(false);
    }
  }

  async function refreshActiveRun() {
    if (!activeRunId) return;
    try {
      const run = await mxApi.getRun(activeRunId);
      setRuns((current) => mergeRuns(current, [run]));
      const nextCursor = newestUpdatedAt([run], runCursorRef.current);
      if (nextCursor && nextCursor !== runCursorRef.current) {
        runCursorRef.current = nextCursor;
        await writeRunCursor(nextCursor);
      }
      setOnline(true);
    } catch {
      setOnline(false);
    }
  }

  async function handleApprove() {
    if (!activeRun || activeRun.status !== "AWAITING_APPROVAL") return;
    if (activeRun.approvalNonceRequired && !approvalNonce.trim()) {
      setError("Informe o nonce de aprovação fornecido pelo fluxo autorizado.");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const updated = await mxApi.approveRun(activeRun.runId, approvalNonce.trim() || undefined);
      setRuns((current) => mergeRuns(current, [updated]));
      setApprovalNonce("");
      setActiveRunId(updated.runId);
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível aprovar esta execução.");
    } finally {
      setBusy(false);
    }
  }

  async function handleReject() {
    if (!activeRun || activeRun.status !== "AWAITING_APPROVAL") return;
    setBusy(true);
    setError(null);
    try {
      const updated = await mxApi.rejectRun(activeRun.runId, "Rejeitado pelo usuário no canal atual.");
      setRuns((current) => mergeRuns(current, [updated]));
      setApprovalNonce("");
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível rejeitar esta execução.");
    } finally {
      setBusy(false);
    }
  }

  async function handleLogout() {
    try {
      await mxApi.logout();
    } finally {
      await clearSession();
      setMessages([]);
      setRuns([]);
      setConversationId(undefined);
      setActiveRunId(null);
      setViewState("login");
    }
  }

  if (viewState === "checking") {
    return <View style={styles.centered}><ActivityIndicator color="#63e6be" /><Text style={styles.muted}>Conectando ao MX Core...</Text></View>;
  }

  if (viewState === "login") {
    return (
      <KeyboardAvoidingView style={styles.screen} behavior={Platform.OS === "ios" ? "padding" : undefined}>
        <StatusBar style="light" />
        <View style={styles.loginCard}>
          <Text style={styles.eyebrow}>MX CORE</Text>
          <Text style={styles.title}>Seu assistente central.</Text>
          <Text style={styles.subtitle}>{subtitle}</Text>
          <TextInput autoCapitalize="none" autoComplete="email" keyboardType="email-address" onChangeText={setEmail} placeholder="seu@email.com" placeholderTextColor="#748198" style={styles.input} value={email} />
          <TextInput autoCapitalize="none" autoComplete="password" onChangeText={setPassword} placeholder="Senha" placeholderTextColor="#748198" secureTextEntry style={styles.input} value={password} />
          {error ? <Text style={styles.error}>{error}</Text> : null}
          <Pressable disabled={busy} onPress={handleLogin} style={({ pressed }) => [styles.primaryButton, pressed && styles.pressed, busy && styles.disabled]}>
            {busy ? <ActivityIndicator color="#08111f" /> : <Text style={styles.primaryButtonText}>Entrar no MX</Text>}
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    );
  }

  return (
    <KeyboardAvoidingView style={styles.screen} behavior={Platform.OS === "ios" ? "padding" : undefined}>
      <StatusBar style="light" />
      <View style={styles.header}>
        <View style={styles.headerText}>
          <Text style={styles.eyebrow}>MX CORE</Text>
          <Text style={styles.headerTitle}>Olá. Como posso ajudar?</Text>
          <Text style={styles.subtitle}>{subtitle}</Text>
        </View>
        <Pressable onPress={handleLogout} style={({ pressed }) => [styles.logout, pressed && styles.pressed]}><Text style={styles.logoutText}>Sair</Text></Pressable>
      </View>

      {activeRun?.status === "AWAITING_APPROVAL" ? (
        <View style={styles.approvalCard}>
          <Text style={styles.approvalTitle}>Ação aguardando aprovação</Text>
          <Text style={styles.muted}>Tool: {activeRun.pendingApproval ?? "não informado"}</Text>
          {activeRun.pendingApprovalArguments ? <Text style={styles.approvalArguments}>{activeRun.pendingApprovalArguments}</Text> : null}
          {activeRun.approvalExpiresAt ? <Text style={styles.muted}>Expira em: {new Date(activeRun.approvalExpiresAt).toLocaleString()}</Text> : null}
          {activeRun.approvalNonceRequired ? <TextInput onChangeText={setApprovalNonce} placeholder="Nonce de aprovação" placeholderTextColor="#748198" style={styles.input} value={approvalNonce} /> : null}
          <View style={styles.approvalActions}>
            <Pressable disabled={busy} onPress={handleApprove} style={[styles.primaryButton, styles.smallButton, busy && styles.disabled]}><Text style={styles.primaryButtonText}>Aprovar</Text></Pressable>
            <Pressable disabled={busy} onPress={handleReject} style={[styles.secondaryButton, styles.smallButton, busy && styles.disabled]}><Text style={styles.logoutText}>Rejeitar</Text></Pressable>
          </View>
        </View>
      ) : null}

      <View style={styles.runBar}>
        <Text style={styles.muted}>{activeRun ? `Run ${activeRun.status}` : "Nenhuma execução selecionada"}</Text>
        <Pressable onPress={refreshActiveRun} disabled={!activeRunId || syncing}><Text style={styles.refreshText}>Atualizar</Text></Pressable>
      </View>

      <FlatList
        contentContainerStyle={styles.messageList}
        data={messages}
        keyExtractor={(item) => item.id}
        ListEmptyComponent={<View style={styles.emptyState}><Text style={styles.emptyTitle}>O MX está pronto.</Text><Text style={styles.muted}>Comece uma conversa. Os runs ficam sincronizados para acompanhamento em outro canal.</Text></View>}
        renderItem={({ item }) => <View style={[styles.bubble, item.role === "USER" ? styles.userBubble : styles.assistantBubble]}><Text style={styles.bubbleRole}>{item.role === "USER" ? "Você" : "MX"}</Text><Text style={styles.bubbleText}>{item.content}</Text></View>}
      />

      {error ? <Text style={styles.error}>{error}</Text> : null}
      <View style={styles.composer}>
        <TextInput editable={!busy && online} multiline onChangeText={(value) => { setPrompt(value); void writeDraftPrompt(value); }} onSubmitEditing={handleSend} placeholder={online ? "Escreva sua solicitação..." : "Offline: seu rascunho será preservado"} placeholderTextColor="#748198" returnKeyType="send" style={styles.promptInput} value={prompt} />
        <Pressable disabled={busy || !online || !prompt.trim()} onPress={handleSend} style={({ pressed }) => [styles.sendButton, pressed && styles.pressed, (busy || !online || !prompt.trim()) && styles.disabled]}>{busy ? <ActivityIndicator color="#08111f" /> : <Text style={styles.sendButtonText}>Enviar</Text>}</Pressable>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: "#08111f", paddingHorizontal: 18, paddingTop: 36 },
  centered: { alignItems: "center", backgroundColor: "#08111f", flex: 1, gap: 12, justifyContent: "center" },
  loginCard: { alignSelf: "center", backgroundColor: "#101c2d", borderColor: "#1e3048", borderRadius: 24, borderWidth: 1, gap: 14, marginTop: 70, maxWidth: 480, padding: 28, width: "100%" },
  header: { alignItems: "flex-start", flexDirection: "row", justifyContent: "space-between", paddingBottom: 12 },
  headerText: { flex: 1, paddingRight: 12 },
  eyebrow: { color: "#63e6be", fontSize: 12, fontWeight: "800", letterSpacing: 2 },
  title: { color: "#f4f7fb", fontSize: 32, fontWeight: "800", lineHeight: 38 },
  headerTitle: { color: "#f4f7fb", fontSize: 24, fontWeight: "800" },
  subtitle: { color: "#9cabc0", fontSize: 15, lineHeight: 22 },
  muted: { color: "#9cabc0", fontSize: 14, lineHeight: 21 },
  input: { backgroundColor: "#0b1728", borderColor: "#29405d", borderRadius: 12, borderWidth: 1, color: "#f4f7fb", fontSize: 16, paddingHorizontal: 14, paddingVertical: 13 },
  primaryButton: { alignItems: "center", backgroundColor: "#63e6be", borderRadius: 12, justifyContent: "center", minHeight: 46, paddingHorizontal: 16 },
  primaryButtonText: { color: "#08111f", fontSize: 15, fontWeight: "800" },
  secondaryButton: { alignItems: "center", borderColor: "#29405d", borderRadius: 12, borderWidth: 1, justifyContent: "center", minHeight: 46, paddingHorizontal: 16 },
  smallButton: { flex: 1 },
  disabled: { opacity: 0.55 },
  pressed: { opacity: 0.82, transform: [{ scale: 0.98 }] },
  error: { color: "#ff8b8b", fontSize: 13, lineHeight: 18 },
  logout: { borderColor: "#29405d", borderRadius: 10, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 8 },
  logoutText: { color: "#c4d1e3", fontWeight: "700" },
  runBar: { alignItems: "center", borderBottomColor: "#1e3048", borderBottomWidth: 1, flexDirection: "row", justifyContent: "space-between", paddingBottom: 8 },
  refreshText: { color: "#63e6be", fontWeight: "700" },
  approvalCard: { backgroundColor: "#332b18", borderColor: "#8a6d2f", borderRadius: 16, borderWidth: 1, gap: 8, marginBottom: 10, padding: 14 },
  approvalTitle: { color: "#ffe7a3", fontSize: 16, fontWeight: "800" },
  approvalArguments: { color: "#e8d8ad", fontFamily: Platform.OS === "ios" ? "Menlo" : "monospace", fontSize: 12 },
  approvalActions: { flexDirection: "row", gap: 8 },
  messageList: { flexGrow: 1, gap: 12, paddingBottom: 16, paddingTop: 12 },
  emptyState: { alignItems: "center", gap: 8, marginTop: 110, paddingHorizontal: 24 },
  emptyTitle: { color: "#f4f7fb", fontSize: 20, fontWeight: "800" },
  bubble: { borderRadius: 16, maxWidth: "88%", padding: 14 },
  userBubble: { alignSelf: "flex-end", backgroundColor: "#164f52" },
  assistantBubble: { alignSelf: "flex-start", backgroundColor: "#101c2d", borderColor: "#1e3048", borderWidth: 1 },
  bubbleRole: { color: "#63e6be", fontSize: 11, fontWeight: "800", letterSpacing: 1, marginBottom: 4, textTransform: "uppercase" },
  bubbleText: { color: "#f4f7fb", fontSize: 16, lineHeight: 23 },
  composer: { alignItems: "flex-end", backgroundColor: "#101c2d", borderColor: "#1e3048", borderRadius: 16, borderWidth: 1, flexDirection: "row", gap: 10, marginBottom: 18, padding: 10 },
  promptInput: { color: "#f4f7fb", flex: 1, fontSize: 16, maxHeight: 120, minHeight: 42, paddingHorizontal: 6, paddingVertical: 9 },
  sendButton: { alignItems: "center", backgroundColor: "#63e6be", borderRadius: 10, justifyContent: "center", minHeight: 42, minWidth: 76, paddingHorizontal: 12 },
  sendButtonText: { color: "#08111f", fontWeight: "800" },
});
