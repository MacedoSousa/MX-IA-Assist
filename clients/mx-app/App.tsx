import { StatusBar } from "expo-status-bar";
import { useEffect, useMemo, useRef, useState } from "react";
import * as Clipboard from "expo-clipboard";
import * as DocumentPicker from "expo-document-picker";
import {
  ActivityIndicator,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  NativeSyntheticEvent,
  Text,
  TextInput,
  TextInputKeyPressEventData,
  View,
} from "react-native";

import {
  MxApiError,
  mxApi,
  type ChatMessage,
  type ConversationHistoryMessage,
  type ConversationSummary,
  type ExecutionRunStatusResponse,
  type UploadedAttachment,
} from "./src/api/client";
import {
  clearSession,
  readAccessToken,
  readConversationId,
  readDraftPrompt,
  readProjects,
  readRunCursor,
  readRefreshToken,
  writeConversationId,
  writeDraftPrompt,
  writeProjects,
  writeRunCursor,
  writeSession,
} from "./src/session/session";

type ViewState = "checking" | "login" | "chat";

type RenderMessage = ChatMessage & { id: string; attachmentNames?: string[] };

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
  const [conversationIndex, setConversationIndex] = useState<ConversationSummary[]>([]);
  const [showHistory, setShowHistory] = useState(false);
  const [showTrash, setShowTrash] = useState(false);
  const [historyTopic, setHistoryTopic] = useState("");
  const [historySearch, setHistorySearch] = useState("");
  const [deletedConversations, setDeletedConversations] = useState<ConversationSummary[]>([]);
  const [editingConversationId, setEditingConversationId] = useState<string | null>(null);
  const [editingConversationTitle, setEditingConversationTitle] = useState("");
  const [runs, setRuns] = useState<ExecutionRunStatusResponse[]>([]);
  const [activeRunId, setActiveRunId] = useState<string | null>(null);
  const [approvalNonce, setApprovalNonce] = useState("");
  const [busy, setBusy] = useState(false);
  const [uploadingAttachment, setUploadingAttachment] = useState(false);
  const [generatingImage, setGeneratingImage] = useState(false);
  const [generatingVideo, setGeneratingVideo] = useState(false);
  const [transcribingAttachmentId, setTranscribingAttachmentId] = useState<string | null>(null);
  const [uploadedAttachments, setUploadedAttachments] = useState<UploadedAttachment[]>([]);
  const [projects, setProjects] = useState<import("./src/session/session").LocalProject[]>([]);
  const [showProjects, setShowProjects] = useState(false);
  const [newProjectName, setNewProjectName] = useState("");
  const [newProjectPath, setNewProjectPath] = useState("");
  const [creatingFile, setCreatingFile] = useState(false);
  const [newFileName, setNewFileName] = useState("");
  const [newFileContent, setNewFileContent] = useState("");
  const [syncing, setSyncing] = useState(false);
  const [online, setOnline] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const runCursorRef = useRef<string | undefined>(undefined);

  const activeRun = runs.find((run) => run.runId === activeRunId)
    ?? runs.find((run) => run.status === "AWAITING_APPROVAL")
    ?? null;

  async function loadConversationHistory(savedConversationId: string | null) {
    if (!savedConversationId) return;
    try {
      const firstPage = await mxApi.getConversationHistory(savedConversationId, 0, 100);
      const restored = firstPage.content
        .filter((message): message is ConversationHistoryMessage & { role: "USER" | "ASSISTANT" } => message.role === "USER" || message.role === "ASSISTANT")
        .map((message) => ({
          id: message.id,
          role: message.role,
          content: message.content,
        } satisfies RenderMessage));
      setMessages(restored);
    } catch {
      // A restored session must remain usable even if history is temporarily unavailable.
    }
  }

  async function loadConversationIndex() {
    try {
      const page = await mxApi.listConversations(0, 50, historyTopic, historySearch);
      setConversationIndex(page.content);
    } catch {
      // The conversation remains usable when the index is temporarily unavailable.
    }
  }

  async function loadDeletedConversations() {
    try {
      setDeletedConversations(await mxApi.listDeletedConversations());
    } catch {
      setError("Não foi possível carregar a lixeira de conversas.");
    }
  }

  async function handleCreateConversation() {
    if (busy || !online) return;
    setBusy(true);
    setError(null);
    try {
      const created = await mxApi.createConversation("Nova conversa");
      setConversationId(created.id);
      await writeConversationId(created.id);
      setMessages([]);
      setUploadedAttachments([]);
      setShowTrash(false);
      setShowHistory(true);
      await loadConversationIndex();
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível criar a conversa.");
    } finally {
      setBusy(false);
    }
  }

  function beginRenameConversation(conversation: ConversationSummary) {
    setEditingConversationId(conversation.id);
    setEditingConversationTitle(conversation.title || "");
  }

  async function handleRenameConversation(conversationId: string) {
    const title = editingConversationTitle.trim();
    if (!title) {
      setError("Informe um nome para a conversa.");
      return;
    }
    try {
      await mxApi.renameConversation(conversationId, title);
      setEditingConversationId(null);
      setEditingConversationTitle("");
      await loadConversationIndex();
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível renomear a conversa.");
    }
  }

  async function handleArchiveConversation(conversationIdToArchive: string) {
    try {
      await mxApi.archiveConversation(conversationIdToArchive);
      if (conversationIdToArchive === conversationId) {
        setConversationId(undefined);
        await writeConversationId(null);
        setMessages([]);
      }
      await loadConversationIndex();
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível arquivar a conversa.");
    }
  }

  async function handleDeleteConversation(conversationIdToDelete: string) {
    try {
      await mxApi.deleteConversation(conversationIdToDelete);
      if (conversationIdToDelete === conversationId) {
        setConversationId(undefined);
        await writeConversationId(null);
        setMessages([]);
      }
      await loadConversationIndex();
      if (showTrash) await loadDeletedConversations();
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível remover a conversa.");
    }
  }

  async function handleRestoreConversation(conversationIdToRestore: string) {
    try {
      await mxApi.restoreConversation(conversationIdToRestore);
      await loadDeletedConversations();
      await loadConversationIndex();
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível restaurar a conversa.");
    }
  }

  async function openConversation(conversation: ConversationSummary) {
    setConversationId(conversation.id);
    await writeConversationId(conversation.id);
    await loadConversationHistory(conversation.id);
    setShowHistory(false);
  }

  useEffect(() => {
    async function restoreSession() {
      try {
        const savedConversationId = await readConversationId();
        const savedRunCursor = await readRunCursor();
        const draft = await readDraftPrompt();
        const savedProjects = await readProjects();
        setProjects(savedProjects);
        setConversationId(savedConversationId ?? undefined);
        runCursorRef.current = savedRunCursor ?? undefined;
        setPrompt(draft);

        const accessToken = await readAccessToken();
        if (accessToken) {
          await loadConversationHistory(savedConversationId);
          await loadConversationIndex();
          setViewState("chat");
          return;
        }
        const refreshToken = await readRefreshToken();
        if (!refreshToken) {
          setViewState("login");
          return;
        }
        await mxApi.refresh();
        await loadConversationHistory(savedConversationId);
        await loadConversationIndex();
        setViewState("chat");
      } catch {
        await clearSession();
        setViewState("login");
      }
    }
    void restoreSession();
  }, []);

  useEffect(() => {
    if (viewState !== "chat" || !showHistory) return;
    const timer = setTimeout(() => void loadConversationIndex(), 250);
    return () => clearTimeout(timer);
  }, [historySearch, historyTopic, showHistory, viewState]);

  useEffect(() => {
    if (viewState !== "chat" || !showTrash) return;
    void loadDeletedConversations();
  }, [showTrash, viewState]);

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
      const savedConversationId = await readConversationId();
      setConversationId(savedConversationId ?? undefined);
      await loadConversationHistory(savedConversationId);
      await loadConversationIndex();
      setViewState("chat");
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível entrar no MX.");
    } finally {
      setBusy(false);
    }
  }

  async function handlePickAttachment() {
    if (busy || uploadingAttachment || !online) return;
    setError(null);
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: ["*/*"],
        copyToCacheDirectory: true,
        multiple: false,
      });
      if (result.canceled || !result.assets?.[0]) return;
      const asset = result.assets[0];
      setUploadingAttachment(true);
      const uploaded = await mxApi.uploadAttachment({
        uri: asset.uri,
        name: asset.name,
        type: asset.mimeType ?? "application/octet-stream",
        size: asset.size,
        file: "file" in asset && asset.file instanceof Blob ? asset.file : undefined,
      });
      setUploadedAttachments((current) => [...current, uploaded]);
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível anexar o arquivo.");
    } finally {
      setUploadingAttachment(false);
    }
  }

  function removeAttachment(attachmentId: string) {
    if (busy) return;
    setUploadedAttachments((current) => current.filter((item) => item.id !== attachmentId));
  }

  async function handleDownloadAttachment(attachment: UploadedAttachment) {
    setError(null);
    try {
      const blob = await mxApi.downloadAttachment(attachment.id);
      if (Platform.OS === "web" && typeof document !== "undefined") {
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = attachment.filename;
        link.click();
        window.setTimeout(() => URL.revokeObjectURL(url), 1000);
        return;
      }
      setError("O anexo foi gerado. No aplicativo, use o compartilhamento do sistema para salvá-lo.");
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível baixar o anexo.");
    }
  }

  async function handleTranscribeAudio(attachment: UploadedAttachment) {
    if (busy || uploadingAttachment || generatingImage || generatingVideo || transcribingAttachmentId) return;
    setTranscribingAttachmentId(attachment.id);
    setError(null);
    try {
      const transcription = await mxApi.transcribeAudio(attachment.id);
      setPrompt((current) => current.trim() ? `${current.trim()}\n\n${transcription.text}` : transcription.text);
      await writeDraftPrompt(transcription.text);
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível transcrever o áudio.");
    } finally {
      setTranscribingAttachmentId(null);
    }
  }

  async function handleGenerateImage() {
    const imagePrompt = prompt.trim();
    if (!imagePrompt || busy || uploadingAttachment || generatingImage || generatingVideo || !online) return;
    setGeneratingImage(true);
    setError(null);
    try {
      const generated = await mxApi.generateImage({ prompt: imagePrompt });
      setUploadedAttachments((current) => [...current, generated]);
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível gerar a imagem local.");
    } finally {
      setGeneratingImage(false);
    }
  }

  async function handleGenerateVideo() {
    const videoPrompt = prompt.trim();
    if (!videoPrompt || busy || uploadingAttachment || generatingImage || generatingVideo || !online) return;
    setGeneratingVideo(true);
    setError(null);
    try {
      const generated = await mxApi.generateVideo({ prompt: videoPrompt, durationSeconds: 6 });
      setUploadedAttachments((current) => [...current, generated]);
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível gerar o vídeo local.");
    } finally {
      setGeneratingVideo(false);
    }
  }

  async function handleCreateProject() {
    const name = newProjectName.trim();
    if (!name) {
      setError("Informe um nome para o projeto.");
      return;
    }
    const project = {
      id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
      name,
      path: newProjectPath.trim() || `workspace/${name.replace(/[^a-zA-Z0-9._-]+/g, "-").toLowerCase()}`,
      createdAt: new Date().toISOString(),
    };
    const nextProjects = [project, ...projects];
    setProjects(nextProjects);
    await writeProjects(nextProjects);
    setNewProjectName("");
    setNewProjectPath("");
    setError(null);
  }

  async function handleChooseProjectFolder() {
    if (Platform.OS !== "web") {
      setNewProjectPath("workspace/");
      return;
    }
    try {
      const picker = (globalThis as typeof globalThis & {
        showDirectoryPicker?: () => Promise<{ name: string }>;
      }).showDirectoryPicker;
      if (!picker) {
        setNewProjectPath("workspace/");
        return;
      }
      const directory = await picker();
      setNewProjectPath(directory.name);
    } catch {
      // Cancelar a escolha da pasta não deve interromper o chat.
    }
  }

  async function handleCreateFile() {
    const filename = newFileName.trim();
    if (!filename) {
      setError("Informe o nome do arquivo.");
      return;
    }
    if (!online || uploadingAttachment) return;
    setUploadingAttachment(true);
    setError(null);
    try {
      const file = Object.assign(new Blob([newFileContent], { type: "text/plain" }), { name: filename });
      const uploaded = await mxApi.uploadAttachment(file);
      setUploadedAttachments((current) => [...current, uploaded]);
      setCreatingFile(false);
      setNewFileName("");
      setNewFileContent("");
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível criar o arquivo.");
    } finally {
      setUploadingAttachment(false);
    }
  }

  async function handlePasteFromClipboard() {
    try {
      const pasted = (await Clipboard.getStringAsync()).trim();
      if (!pasted) return;
      const nextPrompt = prompt.trim() ? `${prompt.trim()}\n${pasted}` : pasted;
      setPrompt(nextPrompt);
      await writeDraftPrompt(nextPrompt);
    } catch {
      setError("O sistema bloqueou a leitura da área de transferência. Use Ctrl+V ou cole diretamente no campo.");
    }
  }

  function handlePromptKeyPress(event: NativeSyntheticEvent<TextInputKeyPressEventData>) {
    const nativeEvent = event.nativeEvent as TextInputKeyPressEventData & { shiftKey?: boolean };
    if (nativeEvent.key !== "Enter" || nativeEvent.shiftKey) return;
    (event as unknown as { preventDefault?: () => void }).preventDefault?.();
    void handleSend();
  }

  async function handleSend() {
    const value = prompt.trim() || (uploadedAttachments.length > 0 ? "Analise os anexos enviados." : "");
    if (!value || busy || !online) return;

    const requestId = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
    const assistantId = `${requestId}-assistant`;
    const attachmentIds = uploadedAttachments.map((attachment) => attachment.id);
    const attachmentNames = uploadedAttachments.map((attachment) => attachment.filename);
    setError(null);
    setBusy(true);
    setPrompt("");
    setUploadedAttachments([]);
    await writeDraftPrompt("");
    setMessages((current) => [
      ...current,
      { id: `${requestId}-user`, role: "USER", content: value, attachmentNames },
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
      }, attachmentIds);
      setConversationId(response.conversationId);
      await writeConversationId(response.conversationId);
      await loadConversationHistory(response.conversationId);
      await loadConversationIndex();
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
      <View style={styles.appShell}>
        <View style={styles.topBar}>
          <View style={styles.brandBlock}>
            <View style={styles.brandMark}><Text style={styles.brandMarkText}>MX</Text></View>
            <View style={styles.brandCopy}>
              <Text style={styles.eyebrow}>ASSISTENTE LOCAL</Text>
              <Text style={styles.headerTitle}>Olá. Como posso ajudar?</Text>
              <Text style={styles.subtitle}>{subtitle}</Text>
            </View>
          </View>
          <View style={styles.topBarActions}>
            <View style={[styles.statusPill, online ? styles.statusPillOnline : styles.statusPillOffline]}><View style={[styles.statusDot, online ? styles.statusDotOnline : styles.statusDotOffline]} /><Text style={styles.statusText}>{online ? "Online" : "Offline"}</Text></View>
            <Pressable onPress={handleLogout} style={({ pressed }) => [styles.logout, pressed && styles.pressed]}><Text style={styles.logoutText}>Sair</Text></Pressable>
          </View>
        </View>
        {error ? <View style={styles.errorBanner}><Text style={styles.errorBannerText}>{error}</Text></View> : null}

        <View style={styles.workspace}>
          <View style={styles.sidebar}>
            <View style={styles.sidebarCard}>
              <Text style={styles.sidebarKicker}>ESPAÇO DE TRABALHO</Text>
              <Pressable disabled={busy || !online} onPress={() => void handleCreateConversation()} style={({ pressed }) => [styles.sidebarMainAction, pressed && styles.pressed, (busy || !online) && styles.disabled]}><Text style={styles.sidebarMainActionText}>+ Nova conversa</Text></Pressable>
              <Pressable onPress={() => setShowHistory((current) => !current)} style={({ pressed }) => [styles.sidebarAction, showHistory && styles.sidebarActionActive, pressed && styles.pressed]}><Text style={styles.sidebarActionText}>Histórico e lixeira</Text><Text style={styles.sidebarActionMeta}>{conversationIndex.length} conversas</Text></Pressable>
              <Pressable onPress={() => setShowProjects((current) => !current)} style={({ pressed }) => [styles.sidebarAction, showProjects && styles.sidebarActionActive, pressed && styles.pressed]}><Text style={styles.sidebarActionText}>Projetos e arquivos</Text><Text style={styles.sidebarActionMeta}>{projects.length} projetos locais</Text></Pressable>
              <Text style={styles.sidebarHint}>Enter envia. Shift + Enter cria uma nova linha.</Text>
            </View>

            {showHistory ? <View style={[styles.historyPanel, styles.sidebarPanel]}>
              <View style={styles.historyHeaderRow}><View><Text style={styles.panelTitle}>{showTrash ? "Lixeira" : "Conversas"}</Text><Text style={styles.muted}>{showTrash ? "Restaure conversas removidas." : "Retome ou organize seu histórico."}</Text></View><View style={styles.historyHeaderActions}><Pressable onPress={() => void handleCreateConversation()} style={({ pressed }) => [styles.primaryButton, styles.compactButton, pressed && styles.pressed]}><Text style={styles.primaryButtonText}>+ Nova</Text></Pressable><Pressable onPress={() => setShowTrash((current) => !current)} style={({ pressed }) => [styles.secondaryButton, styles.compactButton, pressed && styles.pressed]}><Text style={styles.logoutText}>{showTrash ? "Voltar" : "Lixeira"}</Text></Pressable></View></View>
              {!showTrash ? <><View style={styles.historyFilters}><TextInput onChangeText={setHistoryTopic} placeholder="Tópico" placeholderTextColor="#748198" style={styles.historyInput} value={historyTopic} /><TextInput onChangeText={setHistorySearch} placeholder="Buscar" placeholderTextColor="#748198" style={styles.historyInput} value={historySearch} /></View>{conversationIndex.length === 0 ? <Text style={styles.muted}>Nenhuma conversa encontrada.</Text> : <FlatList data={conversationIndex} keyExtractor={(item) => item.id} renderItem={({ item }) => <View style={styles.historyItem}>{editingConversationId === item.id ? <View style={styles.renameRow}><TextInput autoFocus onChangeText={setEditingConversationTitle} onSubmitEditing={() => void handleRenameConversation(item.id)} placeholder="Nome" placeholderTextColor="#748198" style={styles.historyInput} value={editingConversationTitle} /><Pressable onPress={() => void handleRenameConversation(item.id)} style={styles.historyActionButton}><Text style={styles.historyActionText}>Salvar</Text></Pressable></View> : <><Pressable onPress={() => void openConversation(item)} style={({ pressed }) => [styles.historyOpenArea, pressed && styles.pressed]}><Text style={styles.historyItemTitle} numberOfLines={1}>{item.title || "Conversa sem título"}</Text><Text style={styles.historyItemMeta}>{item.topic} · {new Date(item.lastMessageAt).toLocaleDateString()}</Text></Pressable><View style={styles.historyActionRow}><Pressable onPress={() => beginRenameConversation(item)} style={styles.historyActionButton}><Text style={styles.historyActionText}>Renomear</Text></Pressable><Pressable onPress={() => void handleArchiveConversation(item.id)} style={styles.historyActionButton}><Text style={styles.historyActionText}>Arquivar</Text></Pressable><Pressable onPress={() => void handleDeleteConversation(item.id)} style={[styles.historyActionButton, styles.dangerAction]}><Text style={styles.dangerText}>Excluir</Text></Pressable></View></>}</View>} style={styles.historyList} />}</> : <>{deletedConversations.length === 0 ? <Text style={styles.muted}>A lixeira está vazia.</Text> : <FlatList data={deletedConversations} keyExtractor={(item) => item.id} renderItem={({ item }) => <View style={styles.historyItem}><Text style={styles.historyItemTitle} numberOfLines={1}>{item.title || "Conversa sem título"}</Text><Text style={styles.historyItemMeta}>Removida em {item.deletedAt ? new Date(item.deletedAt).toLocaleDateString() : "data desconhecida"}</Text><View style={styles.historyActionRow}><Pressable onPress={() => void handleRestoreConversation(item.id)} style={styles.historyActionButton}><Text style={styles.historyActionText}>Restaurar</Text></Pressable></View></View>} style={styles.historyList} />}</>}</View> : null}

            {showProjects ? <View style={[styles.projectPanel, styles.sidebarPanel]}><Text style={styles.panelTitle}>Projetos locais</Text><Text style={styles.muted}>Use pastas como referência segura para leitura e criação.</Text><View style={styles.projectFormRow}><TextInput onChangeText={setNewProjectName} placeholder="Nome" placeholderTextColor="#748198" style={styles.historyInput} value={newProjectName} /><Pressable onPress={() => void handleChooseProjectFolder()} style={({ pressed }) => [styles.secondaryButton, styles.inlineButton, pressed && styles.pressed]}><Text style={styles.logoutText}>Pasta</Text></Pressable></View><TextInput onChangeText={setNewProjectPath} placeholder="Workspace/projeto" placeholderTextColor="#748198" style={styles.historyInput} value={newProjectPath} /><Pressable onPress={() => void handleCreateProject()} style={({ pressed }) => [styles.primaryButton, pressed && styles.pressed]}><Text style={styles.primaryButtonText}>Criar projeto</Text></Pressable>{projects.length === 0 ? <Text style={styles.muted}>Nenhum projeto cadastrado.</Text> : projects.map((project) => <View key={project.id} style={styles.projectItem}><Text style={styles.historyItemTitle}>{project.name}</Text><Text style={styles.historyItemMeta}>{project.path}</Text></View>)}</View> : null}
          </View>

          <View style={styles.chatColumn}>
            <View style={styles.chatHeader}><View><Text style={styles.chatHeaderKicker}>CONVERSA ATUAL</Text><Text style={styles.chatHeaderTitle}>{conversationIndex.find((item) => item.id === conversationId)?.title || "Nova conversa"}</Text><Text style={styles.chatHeaderMeta}>{messages.length} mensagens · {uploadedAttachments.length} anexos pendentes</Text></View><View style={styles.chatHeaderActions}><Pressable onPress={() => setShowHistory((current) => !current)} style={({ pressed }) => [styles.headerButton, pressed && styles.pressed]}><Text style={styles.headerButtonText}>{showHistory ? "Ocultar histórico" : "Histórico"}</Text></Pressable><Pressable onPress={() => setShowProjects((current) => !current)} style={({ pressed }) => [styles.headerButton, pressed && styles.pressed]}><Text style={styles.headerButtonText}>{showProjects ? "Ocultar projetos" : "Projetos"}</Text></Pressable></View></View>

            {activeRun?.status === "AWAITING_APPROVAL" ? <View style={styles.approvalCard}><Text style={styles.approvalTitle}>Ação aguardando aprovação</Text><Text style={styles.muted}>Tool: {activeRun.pendingApproval ?? "não informado"}</Text>{activeRun.pendingApprovalArguments ? <Text style={styles.approvalArguments}>{activeRun.pendingApprovalArguments}</Text> : null}{activeRun.approvalExpiresAt ? <Text style={styles.muted}>Expira em: {new Date(activeRun.approvalExpiresAt).toLocaleString()}</Text> : null}{activeRun.approvalNonceRequired ? <TextInput onChangeText={setApprovalNonce} placeholder="Nonce de aprovação" placeholderTextColor="#748198" style={styles.input} value={approvalNonce} /> : null}<View style={styles.approvalActions}><Pressable disabled={busy} onPress={handleApprove} style={[styles.primaryButton, styles.smallButton, busy && styles.disabled]}><Text style={styles.primaryButtonText}>Aprovar</Text></Pressable><Pressable disabled={busy} onPress={handleReject} style={[styles.secondaryButton, styles.smallButton, busy && styles.disabled]}><Text style={styles.logoutText}>Rejeitar</Text></Pressable></View></View> : null}
            <View style={styles.runBar}><Text style={styles.muted}>{activeRun ? `Run ${activeRun.status}` : "Nenhuma execução selecionada"}</Text><Pressable onPress={refreshActiveRun} disabled={!activeRunId || syncing}><Text style={styles.refreshText}>{syncing ? "Sincronizando..." : "Atualizar"}</Text></Pressable></View>
            <FlatList contentContainerStyle={styles.messageList} data={messages} keyExtractor={(item) => item.id} ListEmptyComponent={<View style={styles.emptyState}><Text style={styles.emptyTitle}>O MX está pronto.</Text><Text style={styles.muted}>Converse, anexe arquivos ou descreva uma imagem ou vídeo para começar.</Text></View>} renderItem={({ item }) => <View style={[styles.bubble, item.role === "USER" ? styles.userBubble : styles.assistantBubble]}><Text style={styles.bubbleRole}>{item.role === "USER" ? "Você" : "MX"}</Text><Text style={styles.bubbleText}>{item.content}</Text>{item.attachmentNames?.map((name) => <Text key={name} style={styles.attachmentText}>Anexo: {name}</Text>)}</View>} />
            {uploadedAttachments.length > 0 ? <View style={styles.attachmentBar}>{uploadedAttachments.map((attachment) => <View key={attachment.id} style={styles.attachmentChip}><Text numberOfLines={1} style={styles.attachmentChipText}>{attachment.contentType.startsWith("video/") ? "Vídeo" : attachment.contentType.startsWith("image/") ? "Imagem" : attachment.contentType.startsWith("audio/") ? "Áudio" : "Arquivo"} · {attachment.filename}</Text>{attachment.contentType.startsWith("audio/") ? <Pressable disabled={busy || !!transcribingAttachmentId} onPress={() => void handleTranscribeAudio(attachment)}><Text style={styles.transcribeText}>{transcribingAttachmentId === attachment.id ? "..." : "Transcrever"}</Text></Pressable> : null}<Pressable disabled={busy} onPress={() => void handleDownloadAttachment(attachment)}><Text style={styles.downloadText}>Baixar</Text></Pressable><Pressable disabled={busy} onPress={() => removeAttachment(attachment.id)}><Text style={styles.removeAttachment}>×</Text></Pressable></View>)}</View> : null}
            {creatingFile ? <View style={styles.filePanel}><Text style={styles.panelTitle}>Criar arquivo</Text><TextInput onChangeText={setNewFileName} placeholder="nome-do-arquivo.txt" placeholderTextColor="#748198" style={styles.historyInput} value={newFileName} /><TextInput multiline onChangeText={setNewFileContent} placeholder="Conteúdo do arquivo" placeholderTextColor="#748198" style={styles.fileContentInput} value={newFileContent} /><View style={styles.projectFormRow}><Pressable onPress={() => void handleCreateFile()} style={[styles.primaryButton, styles.inlineButton]}><Text style={styles.primaryButtonText}>Criar e anexar</Text></Pressable><Pressable onPress={() => setCreatingFile(false)} style={[styles.secondaryButton, styles.inlineButton]}><Text style={styles.logoutText}>Cancelar</Text></Pressable></View></View> : null}
            <View style={styles.composer}><View style={styles.composerActions}><Text style={styles.composerLabel}>AÇÕES RÁPIDAS</Text><View style={styles.actionGroup}><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online} onPress={handlePickAttachment} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online) && styles.disabled]}><Text style={styles.actionButtonText}>{uploadingAttachment ? "Enviando..." : "Anexar arquivo"}</Text></Pressable><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online} onPress={() => setCreatingFile((current) => !current)} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online) && styles.disabled]}><Text style={styles.actionButtonText}>Novo arquivo</Text></Pressable><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online} onPress={() => void handlePasteFromClipboard()} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online) && styles.disabled]}><Text style={styles.actionButtonText}>Colar texto</Text></Pressable><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online || !prompt.trim()} onPress={() => void handleGenerateImage()} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online || !prompt.trim()) && styles.disabled]}><Text style={styles.actionButtonText}>{generatingImage ? "Gerando imagem..." : "Gerar imagem"}</Text></Pressable><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online || !prompt.trim()} onPress={() => void handleGenerateVideo()} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online || !prompt.trim()) && styles.disabled]}><Text style={styles.actionButtonText}>{generatingVideo ? "Gerando vídeo..." : "Gerar vídeo"}</Text></Pressable></View></View><View style={styles.composerInputRow}><TextInput editable={!busy && !uploadingAttachment && !generatingVideo && online} multiline onChangeText={(value) => { setPrompt(value); void writeDraftPrompt(value); }} onKeyPress={Platform.OS === "web" ? handlePromptKeyPress : undefined} onSubmitEditing={Platform.OS === "web" ? undefined : () => void handleSend()} placeholder={online ? "Escreva uma mensagem ou descreva o que deseja criar..." : "Offline: seu rascunho será preservado"} placeholderTextColor="#748198" returnKeyType="send" style={styles.promptInput} value={prompt} /><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !!transcribingAttachmentId || !online || (!prompt.trim() && uploadedAttachments.length === 0)} onPress={handleSend} style={({ pressed }) => [styles.sendButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online || (!prompt.trim() && uploadedAttachments.length === 0)) && styles.disabled]}>{busy ? <ActivityIndicator color="#08111f" /> : <Text style={styles.sendButtonText}>Enviar</Text>}</Pressable></View></View>
          </View>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: "#07101d", paddingHorizontal: 18, paddingTop: 22 },
  appShell: { flex: 1, gap: 14, maxWidth: 1480, width: "100%", alignSelf: "center" },
  topBar: { alignItems: "center", flexDirection: "row", justifyContent: "space-between", paddingHorizontal: 4, paddingVertical: 6 },
  brandBlock: { alignItems: "center", flexDirection: "row", flexShrink: 1, gap: 12 },
  brandMark: { alignItems: "center", backgroundColor: "#63e6be", borderRadius: 13, height: 42, justifyContent: "center", width: 42 },
  brandMarkText: { color: "#07101d", fontSize: 14, fontWeight: "900", letterSpacing: 1 },
  brandCopy: { flexShrink: 1, gap: 2 },
  topBarActions: { alignItems: "center", flexDirection: "row", gap: 10 },
  statusPill: { alignItems: "center", borderRadius: 999, flexDirection: "row", gap: 7, paddingHorizontal: 10, paddingVertical: 7 },
  statusPillOnline: { backgroundColor: "#10362f" },
  statusPillOffline: { backgroundColor: "#3b2028" },
  statusDot: { borderRadius: 5, height: 9, width: 9 },
  statusDotOnline: { backgroundColor: "#63e6be" },
  statusDotOffline: { backgroundColor: "#ff8b8b" },
  statusText: { color: "#e3edf6", fontSize: 12, fontWeight: "800" },
  errorBanner: { backgroundColor: "#3b2028", borderColor: "#8d4652", borderRadius: 10, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 9 },
  errorBannerText: { color: "#ffd1d1", fontSize: 13, lineHeight: 18 },
  workspace: { flex: 1, flexDirection: Platform.OS === "web" ? "row" : "column", gap: 14, minHeight: 0 },
  sidebar: { gap: 12, width: Platform.OS === "web" ? 292 : "100%" },
  sidebarCard: { backgroundColor: "#0e1b2b", borderColor: "#1d3049", borderRadius: 16, borderWidth: 1, gap: 9, padding: 14 },
  sidebarKicker: { color: "#718aa6", fontSize: 10, fontWeight: "900", letterSpacing: 1.4 },
  sidebarMainAction: { alignItems: "center", backgroundColor: "#63e6be", borderRadius: 10, justifyContent: "center", minHeight: 42, paddingHorizontal: 12 },
  sidebarMainActionText: { color: "#07101d", fontSize: 13, fontWeight: "900" },
  sidebarAction: { backgroundColor: "#12243a", borderColor: "#223d5a", borderRadius: 10, borderWidth: 1, gap: 2, paddingHorizontal: 11, paddingVertical: 9 },
  sidebarActionActive: { backgroundColor: "#18394a", borderColor: "#2a6e70" },
  sidebarActionText: { color: "#d9e9f8", fontSize: 13, fontWeight: "800" },
  sidebarActionMeta: { color: "#8ca4bd", fontSize: 11 },
  sidebarHint: { color: "#768da6", fontSize: 11, lineHeight: 16, paddingTop: 4 },
  sidebarPanel: { marginBottom: 0, maxHeight: 360 },
  chatColumn: { backgroundColor: "#0b1727", borderColor: "#1b3049", borderRadius: 18, borderWidth: 1, flex: 1, gap: 10, minHeight: 0, minWidth: 0, padding: 14 },
  chatHeader: { alignItems: "center", borderBottomColor: "#1a3048", borderBottomWidth: 1, flexDirection: "row", justifyContent: "space-between", paddingBottom: 12 },
  chatHeaderKicker: { color: "#63e6be", fontSize: 10, fontWeight: "900", letterSpacing: 1.3 },
  chatHeaderTitle: { color: "#f4f7fb", fontSize: 18, fontWeight: "900", marginTop: 3 },
  chatHeaderMeta: { color: "#8197ae", fontSize: 11, marginTop: 3 },
  chatHeaderActions: { alignItems: "center", flexDirection: "row", gap: 6 },
  headerButton: { borderColor: "#2b4865", borderRadius: 9, borderWidth: 1, paddingHorizontal: 9, paddingVertical: 7 },
  headerButtonText: { color: "#b9d8f5", fontSize: 11, fontWeight: "800" },
  centered: { alignItems: "center", backgroundColor: "#08111f", flex: 1, gap: 12, justifyContent: "center" },
  loginCard: { alignSelf: "center", backgroundColor: "#101c2d", borderColor: "#1e3048", borderRadius: 24, borderWidth: 1, gap: 14, marginTop: 70, maxWidth: 480, padding: 28, width: "100%" },
  header: { alignItems: "flex-start", flexDirection: "row", justifyContent: "space-between", paddingBottom: 12 },
  headerText: { flex: 1, paddingRight: 12 },
  headerActions: { alignItems: "flex-end", gap: 8 },
  historyButton: { borderColor: "#2a6e70", borderRadius: 10, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 8 },
  historyButtonText: { color: "#a9e8d2", fontWeight: "800" },
  projectButton: { borderColor: "#4c5f8a", borderRadius: 10, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 8 },
  projectButtonText: { color: "#c7d3ff", fontWeight: "800" },
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
  historyPanel: { backgroundColor: "#101c2d", borderColor: "#1e3048", borderRadius: 14, borderWidth: 1, gap: 8, marginBottom: 10, padding: 10 },
  historyHeaderRow: { alignItems: "center", flexDirection: "row", justifyContent: "space-between", gap: 8 },
  historyHeaderActions: { alignItems: "center", flexDirection: "row", gap: 6 },
  compactButton: { minHeight: 36, paddingHorizontal: 10, paddingVertical: 6 },
  renameRow: { alignItems: "center", flexDirection: "row", gap: 6, paddingVertical: 4 },
  historyOpenArea: { flex: 1, minWidth: 0, paddingRight: 6 },
  archivedLabel: { color: "#f2c879", fontSize: 11, fontWeight: "800", marginTop: 3 },
  historyActionRow: { flexDirection: "row", flexWrap: "wrap", gap: 6, marginTop: 7 },
  historyActionButton: { borderColor: "#365779", borderRadius: 8, borderWidth: 1, paddingHorizontal: 8, paddingVertical: 5 },
  historyActionText: { color: "#b9d8ff", fontSize: 11, fontWeight: "800" },
  dangerAction: { borderColor: "#8d4652" },
  dangerText: { color: "#ff9b9b", fontSize: 11, fontWeight: "800" },
  projectPanel: { backgroundColor: "#101c2d", borderColor: "#30456b", borderRadius: 14, borderWidth: 1, gap: 8, marginBottom: 10, padding: 12 },
  filePanel: { backgroundColor: "#101c2d", borderColor: "#2a6e70", borderRadius: 14, borderWidth: 1, gap: 8, marginBottom: 10, padding: 12 },
  panelTitle: { color: "#f4f7fb", fontSize: 15, fontWeight: "800" },
  projectFormRow: { flexDirection: "row", gap: 8 },
  inlineButton: { flex: 1 },
  projectItem: { borderBottomColor: "#1e3048", borderBottomWidth: 1, paddingVertical: 8 },
  fileContentInput: { backgroundColor: "#0b1728", borderColor: "#29405d", borderRadius: 9, borderWidth: 1, color: "#f4f7fb", fontSize: 14, minHeight: 90, padding: 10, textAlignVertical: "top" },
  historyFilters: { flexDirection: "row", gap: 8 },
  historyInput: { backgroundColor: "#0b1728", borderColor: "#29405d", borderRadius: 9, borderWidth: 1, color: "#f4f7fb", flex: 1, fontSize: 13, minHeight: 38, paddingHorizontal: 10 },
  historyList: { maxHeight: 180 },
  historyItem: { borderBottomColor: "#1e3048", borderBottomWidth: 1, paddingVertical: 9 },
  historyItemTitle: { color: "#f4f7fb", fontSize: 14, fontWeight: "800" },
  historyItemMeta: { color: "#9cabc0", fontSize: 12, marginTop: 3 },
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
  attachmentText: { color: "#a9e8d2", fontSize: 12, marginTop: 6 },
  attachmentBar: { flexDirection: "row", flexWrap: "wrap", gap: 6, marginBottom: 8 },
  attachmentChip: { alignItems: "center", backgroundColor: "#102a35", borderColor: "#2a6e70", borderRadius: 10, borderWidth: 1, flexDirection: "row", maxWidth: "100%", paddingHorizontal: 9, paddingVertical: 6 },
  attachmentChipText: { color: "#c6f4e4", flexShrink: 1, fontSize: 12 },
  removeAttachment: { color: "#ff9b9b", fontSize: 18, lineHeight: 16, marginLeft: 6 },
  transcribeText: { color: "#a9e8d2", fontSize: 11, fontWeight: "700", marginLeft: 6 },
  downloadText: { color: "#8fd6ff", fontSize: 11, fontWeight: "800", marginLeft: 6 },
  attachButton: { alignItems: "center", borderColor: "#29405d", borderRadius: 10, borderWidth: 1, justifyContent: "center", minHeight: 42, paddingHorizontal: 10 },
  attachButtonText: { color: "#63e6be", fontSize: 12, fontWeight: "800" },
  mediaButton: { alignItems: "center", borderColor: "#2a6e70", borderRadius: 10, borderWidth: 1, justifyContent: "center", minHeight: 42, paddingHorizontal: 9 },
  mediaButtonText: { color: "#a9e8d2", fontSize: 12, fontWeight: "800" },
  composer: { backgroundColor: "#101c2d", borderColor: "#243b57", borderRadius: 18, borderWidth: 1, gap: 10, marginBottom: 18, padding: 12 },
  composerActions: { gap: 7 },
  composerLabel: { color: "#8ea7c4", fontSize: 11, fontWeight: "800", letterSpacing: 1, textTransform: "uppercase" },
  actionGroup: { flexDirection: "row", flexWrap: "wrap", gap: 7 },
  actionButton: { alignItems: "center", backgroundColor: "#15283d", borderColor: "#31516f", borderRadius: 10, borderWidth: 1, justifyContent: "center", minHeight: 38, paddingHorizontal: 10 },
  actionButtonText: { color: "#c5e5ff", fontSize: 12, fontWeight: "800" },
  composerInputRow: { alignItems: "flex-end", flexDirection: "row", gap: 8 },
  promptInput: { backgroundColor: "#0b1728", borderColor: "#29405d", borderRadius: 12, borderWidth: 1, color: "#f4f7fb", flex: 1, fontSize: 16, maxHeight: 120, minHeight: 48, paddingHorizontal: 12, paddingVertical: 10 },
  sendButton: { alignItems: "center", backgroundColor: "#63e6be", borderRadius: 10, justifyContent: "center", minHeight: 42, minWidth: 76, paddingHorizontal: 12 },
  sendButtonText: { color: "#08111f", fontWeight: "800" },
});
