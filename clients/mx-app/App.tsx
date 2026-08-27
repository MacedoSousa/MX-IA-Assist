import { StatusBar } from "expo-status-bar";
/**
 * Design: Ateliê de Inteligência — azul-ink como moldura, marfim em superfícies
 * de leitura e verde-mar/cobre como sinais. O MX continua sendo um espaço de
 * trabalho completo, não uma tela de chat isolada.
 */
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
import { mergeRuns, newestUpdatedAt } from "./src/features/runs/run-state";
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
  const [generatingDocument, setGeneratingDocument] = useState(false);
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
  const [responseElapsedSeconds, setResponseElapsedSeconds] = useState(0);
  const [receivedToken, setReceivedToken] = useState(false);
  const responseStartedAtRef = useRef<number | undefined>(undefined);
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

  useEffect(() => {
    if (!busy) {
      setResponseElapsedSeconds(0);
      return;
    }
    const timer = setInterval(() => {
      const startedAt = responseStartedAtRef.current;
      if (startedAt) setResponseElapsedSeconds(Math.floor((Date.now() - startedAt) / 1000));
    }, 1000);
    return () => clearInterval(timer);
  }, [busy]);

  const subtitle = useMemo(() => {
    if (viewState === "checking") return "Preparando sua sessão local";
    if (viewState === "login") return "Seu assistente pessoal, em todos os seus dispositivos";
    if (!online) return "Offline: rascunho preservado; nenhuma tool será executada automaticamente";
    return syncing ? "Sincronizando runs e aprovações" : "O MX Core coordena as skills especialistas por você";
  }, [online, syncing, viewState]);

  const responseStage = useMemo(() => {
    if (!busy) return "DeepSeek R1 14B · execução local";
    if (!receivedToken && responseElapsedSeconds >= 12) return `Aquecendo o modelo local · ${responseElapsedSeconds}s`;
    if (!receivedToken) return `Raciocinando · ${responseElapsedSeconds}s`;
    return `Gerando resposta · ${responseElapsedSeconds}s`;
  }, [busy, receivedToken, responseElapsedSeconds]);

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
      const generated = await mxApi.generateImage({
        prompt: imagePrompt,
        negativePrompt: "blurry, low quality, lowres, out of focus, soft focus, distorted, deformed, duplicate, watermark, signature, text artifacts",
        cfgScale: 7,
      });
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
      const generated = await mxApi.generateVideo({ prompt: videoPrompt, durationSeconds: 5, width: 768, height: 432 });
      setUploadedAttachments((current) => [...current, generated]);
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível gerar o vídeo local.");
    } finally {
      setGeneratingVideo(false);
    }
  }

  async function handleGenerateDocument() {
    const documentPrompt = prompt.trim();
    if (!documentPrompt || busy || uploadingAttachment || generatingImage || generatingVideo || generatingDocument || !online) return;
    setGeneratingDocument(true);
    setError(null);
    try {
      const generated = await mxApi.generateDocument({ prompt: documentPrompt, format: "PDF" });
      setUploadedAttachments((current) => [...current, generated]);
    } catch (cause) {
      setError(cause instanceof MxApiError ? cause.message : "Não foi possível gerar o documento local.");
    } finally {
      setGeneratingDocument(false);
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
    responseStartedAtRef.current = Date.now();
    setResponseElapsedSeconds(0);
    setReceivedToken(false);
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
          setReceivedToken(true);
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
      responseStartedAtRef.current = undefined;
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
            <View style={styles.brandMark}><Text style={styles.brandMarkText}>✦</Text></View>
            <View style={styles.brandCopy}>
              <Text style={styles.eyebrow}>ESTÚDIO PESSOAL · LOCAL-FIRST</Text>
              <Text style={styles.headerTitle}>MX · espaço de inteligência</Text>
              <Text style={styles.subtitle}>Trabalho, memória e criação no mesmo contexto.</Text>
            </View>
          </View>
          <View style={styles.topBarMeta}><Text style={styles.topBarMetaLabel}>MX CORE  ›  CONVERSAS</Text><Text style={styles.topBarMetaValue}>deepseek-r1:14b</Text><Text style={styles.topBarMetaHint}>{responseStage}</Text></View>
          <View style={styles.topBarActions}>
            <View style={[styles.statusPill, online ? styles.statusPillOnline : styles.statusPillOffline]}><View style={[styles.statusDot, online ? styles.statusDotOnline : styles.statusDotOffline]} /><Text style={styles.statusText}>{online ? "Online" : "Offline"}</Text></View>
            <Pressable onPress={handleLogout} style={({ pressed }) => [styles.logout, pressed && styles.pressed]}><Text style={styles.logoutText}>Sair</Text></Pressable>
          </View>
        </View>
        {error ? <View style={styles.errorBanner}><Text style={styles.errorBannerText}>{error}</Text></View> : null}

        <View style={styles.workspace}>
          <View style={styles.sidebar}>
            <View style={styles.sidebarCard}>
              <Text style={styles.sidebarKicker}>ÍNDICE DO SISTEMA</Text>
              <Text style={styles.sidebarTitle}>Tudo em um só contexto.</Text>
              <Text style={styles.sidebarDescription}>Converse, organize projetos e use suas ferramentas locais com privacidade e continuidade.</Text>
              <View style={styles.sidebarSignal}><View style={styles.sidebarSignalDot} /><Text style={styles.sidebarSignalText}>{online ? "Sistema operacional" : "Modo offline"}</Text></View>
              <Pressable disabled={busy || !online} onPress={() => void handleCreateConversation()} style={({ pressed }) => [styles.sidebarMainAction, pressed && styles.pressed, (busy || !online) && styles.disabled]}><Text style={styles.sidebarMainActionText}>+ Nova conversa</Text></Pressable>
              <Pressable onPress={() => { setShowHistory(false); setShowProjects(false); }} style={({ pressed }) => [styles.sidebarAction, !showHistory && !showProjects && styles.sidebarActionActive, pressed && styles.pressed]}><Text style={styles.sidebarActionText}>Visão geral</Text><Text style={styles.sidebarActionMeta}>Contexto e atividade</Text></Pressable>
              <Pressable onPress={() => { setShowHistory((current) => !current); setShowProjects(false); }} style={({ pressed }) => [styles.sidebarAction, showHistory && styles.sidebarActionActive, pressed && styles.pressed]}><Text style={styles.sidebarActionText}>Conversas</Text><Text style={styles.sidebarActionMeta}>{conversationIndex.length} contextos salvos</Text></Pressable>
              <Pressable onPress={() => { setShowProjects((current) => !current); setShowHistory(false); }} style={({ pressed }) => [styles.sidebarAction, showProjects && styles.sidebarActionActive, pressed && styles.pressed]}><Text style={styles.sidebarActionText}>Projetos</Text><Text style={styles.sidebarActionMeta}>{projects.length} workspaces locais</Text></Pressable>
              <Text style={styles.sidebarHint}>CONHECIMENTO · AGENTS · MÍDIA vivem nas conversas e projetos. Enter envia; Shift + Enter cria uma nova linha.</Text>
            </View>

            {showHistory ? <View style={[styles.historyPanel, styles.sidebarPanel]}>
              <View style={styles.historyHeaderRow}><View><Text style={styles.panelTitle}>{showTrash ? "Lixeira" : "Conversas"}</Text><Text style={styles.muted}>{showTrash ? "Restaure conversas removidas." : "Retome ou organize seu histórico."}</Text></View><View style={styles.historyHeaderActions}><Pressable onPress={() => void handleCreateConversation()} style={({ pressed }) => [styles.primaryButton, styles.compactButton, pressed && styles.pressed]}><Text style={styles.primaryButtonText}>+ Nova</Text></Pressable><Pressable onPress={() => setShowTrash((current) => !current)} style={({ pressed }) => [styles.secondaryButton, styles.compactButton, pressed && styles.pressed]}><Text style={styles.logoutText}>{showTrash ? "Voltar" : "Lixeira"}</Text></Pressable></View></View>
              {!showTrash ? <><View style={styles.historyFilters}><TextInput onChangeText={setHistoryTopic} placeholder="Tópico" placeholderTextColor="#748198" style={styles.historyInput} value={historyTopic} /><TextInput onChangeText={setHistorySearch} placeholder="Buscar" placeholderTextColor="#748198" style={styles.historyInput} value={historySearch} /></View>{conversationIndex.length === 0 ? <Text style={styles.muted}>Nenhuma conversa encontrada.</Text> : <FlatList data={conversationIndex} keyExtractor={(item) => item.id} renderItem={({ item }) => <View style={styles.historyItem}>{editingConversationId === item.id ? <View style={styles.renameRow}><TextInput autoFocus onChangeText={setEditingConversationTitle} onSubmitEditing={() => void handleRenameConversation(item.id)} placeholder="Nome" placeholderTextColor="#748198" style={styles.historyInput} value={editingConversationTitle} /><Pressable onPress={() => void handleRenameConversation(item.id)} style={styles.historyActionButton}><Text style={styles.historyActionText}>Salvar</Text></Pressable></View> : <><Pressable onPress={() => void openConversation(item)} style={({ pressed }) => [styles.historyOpenArea, pressed && styles.pressed]}><Text style={styles.historyItemTitle} numberOfLines={1}>{item.title || "Conversa sem título"}</Text><Text style={styles.historyItemMeta}>{item.topic} · {new Date(item.lastMessageAt).toLocaleDateString()}</Text></Pressable><View style={styles.historyActionRow}><Pressable onPress={() => beginRenameConversation(item)} style={styles.historyActionButton}><Text style={styles.historyActionText}>Renomear</Text></Pressable><Pressable onPress={() => void handleArchiveConversation(item.id)} style={styles.historyActionButton}><Text style={styles.historyActionText}>Arquivar</Text></Pressable><Pressable onPress={() => void handleDeleteConversation(item.id)} style={[styles.historyActionButton, styles.dangerAction]}><Text style={styles.dangerText}>Excluir</Text></Pressable></View></>}</View>} style={styles.historyList} />}</> : <>{deletedConversations.length === 0 ? <Text style={styles.muted}>A lixeira está vazia.</Text> : <FlatList data={deletedConversations} keyExtractor={(item) => item.id} renderItem={({ item }) => <View style={styles.historyItem}><Text style={styles.historyItemTitle} numberOfLines={1}>{item.title || "Conversa sem título"}</Text><Text style={styles.historyItemMeta}>Removida em {item.deletedAt ? new Date(item.deletedAt).toLocaleDateString() : "data desconhecida"}</Text><View style={styles.historyActionRow}><Pressable onPress={() => void handleRestoreConversation(item.id)} style={styles.historyActionButton}><Text style={styles.historyActionText}>Restaurar</Text></Pressable></View></View>} style={styles.historyList} />}</>}</View> : null}

            {showProjects ? <View style={[styles.projectPanel, styles.sidebarPanel]}><Text style={styles.panelTitle}>Projetos locais</Text><Text style={styles.muted}>Use pastas como referência segura para leitura e criação.</Text><View style={styles.projectFormRow}><TextInput onChangeText={setNewProjectName} placeholder="Nome" placeholderTextColor="#748198" style={styles.historyInput} value={newProjectName} /><Pressable onPress={() => void handleChooseProjectFolder()} style={({ pressed }) => [styles.secondaryButton, styles.inlineButton, pressed && styles.pressed]}><Text style={styles.logoutText}>Pasta</Text></Pressable></View><TextInput onChangeText={setNewProjectPath} placeholder="Workspace/projeto" placeholderTextColor="#748198" style={styles.historyInput} value={newProjectPath} /><Pressable onPress={() => void handleCreateProject()} style={({ pressed }) => [styles.primaryButton, pressed && styles.pressed]}><Text style={styles.primaryButtonText}>Criar projeto</Text></Pressable>{projects.length === 0 ? <Text style={styles.muted}>Nenhum projeto cadastrado.</Text> : projects.map((project) => <View key={project.id} style={styles.projectItem}><Text style={styles.historyItemTitle}>{project.name}</Text><Text style={styles.historyItemMeta}>{project.path}</Text></View>)}</View> : null}
          </View>

          <View style={styles.chatColumn}>
            <View style={styles.chatHeader}><View style={styles.chatHeaderIdentity}><View style={styles.chatHeaderOrb}><Text style={styles.chatHeaderOrbText}>✦</Text></View><View><Text style={styles.chatHeaderKicker}>CONVERSA ATUAL</Text><Text style={styles.chatHeaderTitle}>{conversationIndex.find((item) => item.id === conversationId)?.title || "Nova conversa"}</Text><Text style={styles.chatHeaderMeta}>{messages.length} mensagens · {uploadedAttachments.length} anexos pendentes</Text></View></View><View style={styles.chatHeaderActions}><Pressable onPress={() => setShowHistory((current) => !current)} style={({ pressed }) => [styles.headerButton, pressed && styles.pressed]}><Text style={styles.headerButtonText}>{showHistory ? "Ocultar histórico" : "Histórico"}</Text></Pressable><Pressable onPress={() => setShowProjects((current) => !current)} style={({ pressed }) => [styles.headerButton, pressed && styles.pressed]}><Text style={styles.headerButtonText}>{showProjects ? "Ocultar projetos" : "Projetos"}</Text></Pressable></View></View>

            {activeRun?.status === "AWAITING_APPROVAL" ? <View style={styles.approvalCard}><Text style={styles.approvalTitle}>Ação aguardando aprovação</Text><Text style={styles.muted}>Tool: {activeRun.pendingApproval ?? "não informado"}</Text>{activeRun.pendingApprovalArguments ? <Text style={styles.approvalArguments}>{activeRun.pendingApprovalArguments}</Text> : null}{activeRun.approvalExpiresAt ? <Text style={styles.muted}>Expira em: {new Date(activeRun.approvalExpiresAt).toLocaleString()}</Text> : null}{activeRun.approvalNonceRequired ? <TextInput onChangeText={setApprovalNonce} placeholder="Nonce de aprovação" placeholderTextColor="#748198" style={styles.input} value={approvalNonce} /> : null}<View style={styles.approvalActions}><Pressable disabled={busy} onPress={handleApprove} style={[styles.primaryButton, styles.smallButton, busy && styles.disabled]}><Text style={styles.primaryButtonText}>Aprovar</Text></Pressable><Pressable disabled={busy} onPress={handleReject} style={[styles.secondaryButton, styles.smallButton, busy && styles.disabled]}><Text style={styles.logoutText}>Rejeitar</Text></Pressable></View></View> : null}
            <View style={styles.runBar}><View style={styles.runStatus}><View style={[styles.runDot, busy && styles.runDotBusy]} /><Text style={styles.runStatusText}>{busy ? responseStage : activeRun ? `Run ${activeRun.status}` : "Pronto para executar"}</Text></View><Pressable onPress={refreshActiveRun} disabled={!activeRunId || syncing}><Text style={styles.refreshText}>{syncing ? "Sincronizando..." : "Atualizar"}</Text></Pressable></View>
            <FlatList contentContainerStyle={styles.messageList} data={messages} keyExtractor={(item) => item.id} ListEmptyComponent={<View style={styles.emptyState}><View style={styles.emptyBadge}><Text style={styles.emptyBadgeText}>LOCAL · PRIVADO · SEM API KEY</Text></View><Text style={styles.emptyTitle}>Seu assistente está pronto.</Text><Text style={styles.muted}>Converse, anexe arquivos ou descreva uma imagem ou vídeo para começar.</Text><View style={styles.emptyTips}><Text style={styles.emptyTip}>⌘  Enter para enviar</Text><Text style={styles.emptyTip}>↗  Shift + Enter para nova linha</Text><Text style={styles.emptyTip}>◌  O modelo aquece após períodos ocioso</Text></View></View>} renderItem={({ item }) => <View style={[styles.bubble, item.role === "USER" ? styles.userBubble : styles.assistantBubble]}><Text style={styles.bubbleRole}>{item.role === "USER" ? "Você" : "MX · DEEPSEEK"}</Text>{item.content ? <Text style={styles.bubbleText}>{item.content}</Text> : <View style={styles.thinkingRow}><ActivityIndicator color="#78e3c2" size="small" /><Text style={styles.thinkingText}>{responseStage}</Text></View>}{item.attachmentNames?.map((name) => <Text key={name} style={styles.attachmentText}>Anexo: {name}</Text>)}</View>} />
            {uploadedAttachments.length > 0 ? <View style={styles.attachmentBar}>{uploadedAttachments.map((attachment) => <View key={attachment.id} style={styles.attachmentChip}><Text numberOfLines={1} style={styles.attachmentChipText}>{attachment.contentType.startsWith("video/") ? "Vídeo" : attachment.contentType.startsWith("image/") ? "Imagem" : attachment.contentType.startsWith("audio/") ? "Áudio" : "Arquivo"} · {attachment.filename}</Text>{attachment.contentType.startsWith("audio/") ? <Pressable disabled={busy || !!transcribingAttachmentId} onPress={() => void handleTranscribeAudio(attachment)}><Text style={styles.transcribeText}>{transcribingAttachmentId === attachment.id ? "..." : "Transcrever"}</Text></Pressable> : null}<Pressable disabled={busy} onPress={() => void handleDownloadAttachment(attachment)}><Text style={styles.downloadText}>Baixar</Text></Pressable><Pressable disabled={busy} onPress={() => removeAttachment(attachment.id)}><Text style={styles.removeAttachment}>×</Text></Pressable></View>)}</View> : null}
            {creatingFile ? <View style={styles.filePanel}><Text style={styles.panelTitle}>Criar arquivo</Text><TextInput onChangeText={setNewFileName} placeholder="nome-do-arquivo.txt" placeholderTextColor="#748198" style={styles.historyInput} value={newFileName} /><TextInput multiline onChangeText={setNewFileContent} placeholder="Conteúdo do arquivo" placeholderTextColor="#748198" style={styles.fileContentInput} value={newFileContent} /><View style={styles.projectFormRow}><Pressable onPress={() => void handleCreateFile()} style={[styles.primaryButton, styles.inlineButton]}><Text style={styles.primaryButtonText}>Criar e anexar</Text></Pressable><Pressable onPress={() => setCreatingFile(false)} style={[styles.secondaryButton, styles.inlineButton]}><Text style={styles.logoutText}>Cancelar</Text></Pressable></View></View> : null}
            <View style={styles.composer}><View style={styles.composerHeader}><View><Text style={styles.composerLabel}>COMANDO DO MX</Text><Text style={styles.composerSubtext}>Descreva sujeito, cenário, enquadramento e estilo. O MX mantém seu pedido como referência.</Text></View><Text style={styles.composerModel}>R1 · 14B</Text></View><View style={styles.composerActions}><Text style={styles.composerLabel}>AÇÕES RÁPIDAS</Text><View style={styles.actionGroup}><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online} onPress={handlePickAttachment} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online) && styles.disabled]}><Text style={styles.actionButtonText}>{uploadingAttachment ? "Enviando..." : "Anexar arquivo"}</Text></Pressable><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online} onPress={() => setCreatingFile((current) => !current)} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online) && styles.disabled]}><Text style={styles.actionButtonText}>Novo arquivo</Text></Pressable><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online} onPress={() => void handlePasteFromClipboard()} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online) && styles.disabled]}><Text style={styles.actionButtonText}>Colar texto</Text></Pressable><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online || !prompt.trim()} onPress={() => void handleGenerateImage()} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online || !prompt.trim()) && styles.disabled]}><Text style={styles.actionButtonText}>{generatingImage ? "Gerando imagem..." : "Imagem · qualidade"}</Text></Pressable><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !online || !prompt.trim()} onPress={() => void handleGenerateVideo()} style={({ pressed }) => [styles.actionButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online || !prompt.trim()) && styles.disabled]}><Text style={styles.actionButtonText}>{generatingVideo ? "Criando clipe..." : "Clipe animado"}</Text></Pressable></View></View><View style={styles.composerInputRow}><TextInput editable={!busy && !uploadingAttachment && !generatingVideo && online} multiline onChangeText={(value) => { setPrompt(value); void writeDraftPrompt(value); }} onKeyPress={Platform.OS === "web" ? handlePromptKeyPress : undefined} onSubmitEditing={Platform.OS === "web" ? undefined : () => void handleSend()} placeholder={online ? "Ex.: retrato editorial, luz lateral suave, plano médio, estilo de filme analógico..." : "Offline: seu rascunho será preservado"} placeholderTextColor="#748198" returnKeyType="send" style={styles.promptInput} value={prompt} /><Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || !!transcribingAttachmentId || !online || (!prompt.trim() && uploadedAttachments.length === 0)} onPress={handleSend} style={({ pressed }) => [styles.sendButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || !online || (!prompt.trim() && uploadedAttachments.length === 0)) && styles.disabled]}>{busy ? <ActivityIndicator color="#08111f" /> : <Text style={styles.sendButtonText}>Enviar</Text>}</Pressable></View></View>
          <Pressable disabled={busy || uploadingAttachment || generatingImage || generatingVideo || generatingDocument || !online || !prompt.trim()} onPress={() => void handleGenerateDocument()} style={({ pressed }) => [styles.secondaryButton, styles.inlineButton, pressed && styles.pressed, (busy || uploadingAttachment || generatingImage || generatingVideo || generatingDocument || !online || !prompt.trim()) && styles.disabled]}><Text style={styles.logoutText}>{generatingDocument ? "Gerando documento..." : "Gerar documento PDF"}</Text></Pressable>
          </View>
          <View style={styles.contextColumn} accessibilityLabel="Contexto operacional">
            <View style={styles.contextCard}>
              <View style={styles.contextHeading}><View><Text style={styles.contextKicker}>SISTEMA</Text><Text style={styles.contextTitle}>Pronto para trabalhar</Text></View><View style={[styles.contextStatusDot, online ? styles.statusDotOnline : styles.statusDotOffline]} /></View>
              <View style={styles.contextLine}><Text style={styles.contextLineLabel}>MX Core</Text><Text style={[styles.contextLineValue, online ? styles.contextValueGood : styles.contextValueMuted]}>{online ? "Online" : "Aguardando conexão"}</Text></View>
              <View style={styles.contextLine}><Text style={styles.contextLineLabel}>Modelo</Text><Text style={styles.contextLineValue}>R1 · 14B</Text></View>
              <View style={styles.contextLine}><Text style={styles.contextLineLabel}>Conversas</Text><Text style={styles.contextLineValue}>{conversationIndex.length} ativas</Text></View>
              <Pressable onPress={() => setShowHistory(true)} style={({ pressed }) => [styles.contextAction, pressed && styles.pressed]} accessibilityLabel="Abrir conversas salvas"><Text style={styles.contextActionText}>Abrir histórico</Text><Text style={styles.contextActionArrow}>↗</Text></Pressable>
            </View>
            <View style={styles.contextCard}>
              <Text style={styles.contextKicker}>CONTEXTO ATUAL</Text>
              <Text style={styles.contextTitleSmall}>{activeRun?.status === "AWAITING_APPROVAL" ? "Uma ação precisa de você" : busy ? "O MX está processando" : "Conversa em foco"}</Text>
              <Text style={styles.contextDescription}>{activeRun?.status === "AWAITING_APPROVAL" ? "Revise a solicitação e aprove ou rejeite com o nonce desta run." : uploadedAttachments.length > 0 ? `${uploadedAttachments.length} anexo(s) aguardam sua próxima solicitação.` : "Use arquivos, mídia e conhecimento no mesmo contexto da conversa."}</Text>
              <View style={styles.contextDivider} />
              <Text style={styles.contextFootnote}>LOCAL-FIRST · AÇÕES SENSÍVEIS EXIGEM APROVAÇÃO</Text>
            </View>
            <View style={[styles.contextCard, styles.contextMediaCard]}>
              <Text style={styles.contextKicker}>MÍDIA</Text>
              <Text style={styles.contextTitleSmall}>Criação com contexto</Text>
              <Text style={styles.contextDescription}>Descreva primeiro a cena, o sujeito, o enquadramento e o estilo para obter um resultado mais fiel.</Text>
            </View>
          </View>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: "#07111d", paddingHorizontal: Platform.OS === "web" ? 22 : 14, paddingTop: Platform.OS === "web" ? 18 : 12 },
  appShell: { flex: 1, gap: 14, maxWidth: 1680, width: "100%", alignSelf: "center" },
  topBar: { alignItems: "center", backgroundColor: "#091622", borderBottomColor: "#203343", borderBottomWidth: 1, flexDirection: "row", justifyContent: "space-between", paddingHorizontal: 18, paddingVertical: 13 },
  brandBlock: { alignItems: "center", flexDirection: "row", flexShrink: 1, gap: 12 },
  brandMark: { alignItems: "center", backgroundColor: "#11283a", borderColor: "#2a6470", borderRadius: 17, borderWidth: 1, height: 48, justifyContent: "center", shadowColor: "#020711", shadowOpacity: 0.32, shadowRadius: 16, width: 48 },
  brandMarkText: { color: "#62dab5", fontSize: 19, fontWeight: "900", letterSpacing: 1 },
  brandCopy: { flexShrink: 1, gap: 3 },
  topBarMeta: { alignItems: "flex-end", flex: 1, gap: 2, marginHorizontal: 28 },
  topBarMetaLabel: { color: "#60758e", fontSize: 9, fontWeight: "900", letterSpacing: 1.5 },
  topBarMetaValue: { color: "#e7e2d7", fontFamily: Platform.OS === "ios" ? "Menlo" : "monospace", fontSize: 12, fontWeight: "800" },
  topBarMetaHint: { color: "#7f95aa", fontSize: 10 },
  topBarActions: { alignItems: "center", flexDirection: "row", gap: 10 },
  statusPill: { alignItems: "center", borderRadius: 999, flexDirection: "row", gap: 7, paddingHorizontal: 10, paddingVertical: 7 },
  statusPillOnline: { backgroundColor: "#0d2a27", borderColor: "#1f6655", borderWidth: 1 },
  statusPillOffline: { backgroundColor: "#3b2028", borderColor: "#8d4652", borderWidth: 1 },
  statusDot: { borderRadius: 5, height: 9, width: 9 },
  statusDotOnline: { backgroundColor: "#63e6be" },
  statusDotOffline: { backgroundColor: "#ff8b8b" },
  statusText: { color: "#e3edf6", fontSize: 12, fontWeight: "800" },
  errorBanner: { backgroundColor: "#3b2028", borderColor: "#8d4652", borderRadius: 10, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 9 },
  errorBannerText: { color: "#ffd1d1", fontSize: 13, lineHeight: 18 },
  workspace: { flex: 1, flexDirection: Platform.OS === "web" ? "row" : "column", gap: 14, minHeight: 0 },
  sidebar: { backgroundColor: "#081520", borderColor: "#1a3040", borderRightWidth: Platform.OS === "web" ? 1 : 0, gap: 12, padding: Platform.OS === "web" ? 10 : 0, width: Platform.OS === "web" ? 292 : "100%" },
  sidebarCard: { backgroundColor: "#0b1d2b", borderColor: "#1d3546", borderRadius: 18, borderWidth: 1, gap: 11, padding: 17, shadowColor: "#01050a", shadowOpacity: 0.24, shadowRadius: 16 },
  sidebarTitle: { color: "#f3eee4", fontFamily: Platform.OS === "ios" ? "Georgia" : "serif", fontSize: 21, fontWeight: "700", letterSpacing: -0.45 },
  sidebarDescription: { color: "#a4b2bc", fontSize: 12, lineHeight: 19 },
  sidebarSignal: { alignItems: "center", backgroundColor: "#0c2928", borderColor: "#205752", borderRadius: 999, flexDirection: "row", gap: 8, paddingHorizontal: 10, paddingVertical: 8 },
  sidebarSignalDot: { backgroundColor: "#7ce7c5", borderRadius: 4, height: 8, width: 8 },
  sidebarSignalText: { color: "#9de5cb", fontSize: 11, fontWeight: "800" },
  sidebarKicker: { color: "#718aa6", fontSize: 10, fontWeight: "900", letterSpacing: 1.4 },
  sidebarMainAction: { alignItems: "center", backgroundColor: "#59d4ad", borderRadius: 12, justifyContent: "center", minHeight: 47, paddingHorizontal: 12, shadowColor: "#275f51", shadowOpacity: 0.35, shadowRadius: 12 },
  sidebarMainActionText: { color: "#062019", fontSize: 13, fontWeight: "900" },
  sidebarAction: { backgroundColor: "transparent", borderColor: "transparent", borderRadius: 11, borderWidth: 1, gap: 3, paddingHorizontal: 12, paddingVertical: 11 },
  sidebarActionActive: { backgroundColor: "#132d3e", borderColor: "#254b58" },
  sidebarActionText: { color: "#e3e9e5", fontSize: 13, fontWeight: "800" },
  sidebarActionMeta: { color: "#7f94a4", fontSize: 10 },
  sidebarHint: { color: "#7890a0", fontSize: 10, lineHeight: 16, paddingTop: 8 },
  sidebarPanel: { marginBottom: 0, maxHeight: 360 },
  chatColumn: { backgroundColor: "#0a1825", borderColor: "#203747", borderRadius: 20, borderWidth: 1, flex: 1, gap: 12, minHeight: 0, minWidth: 0, padding: 18, shadowColor: "#02070c", shadowOpacity: 0.22, shadowRadius: 22 },
  contextColumn: { gap: 12, width: Platform.OS === "web" ? 270 : "100%" },
  contextCard: { backgroundColor: "#0c1d2b", borderColor: "#203747", borderRadius: 18, borderWidth: 1, gap: 12, padding: 16, shadowColor: "#02070c", shadowOpacity: 0.2, shadowRadius: 18 },
  contextMediaCard: { backgroundColor: "#102536", borderColor: "#255463" },
  contextHeading: { alignItems: "flex-start", flexDirection: "row", justifyContent: "space-between" },
  contextKicker: { color: "#6f8ba0", fontSize: 9, fontWeight: "900", letterSpacing: 1.4 },
  contextTitle: { color: "#f3eee4", fontFamily: Platform.OS === "ios" ? "Georgia" : "serif", fontSize: 19, fontWeight: "700", marginTop: 5 },
  contextTitleSmall: { color: "#e8f0ed", fontFamily: Platform.OS === "ios" ? "Georgia" : "serif", fontSize: 16, fontWeight: "700", lineHeight: 22, marginTop: 3 },
  contextStatusDot: { borderRadius: 5, height: 9, marginTop: 4, width: 9 },
  contextLine: { alignItems: "center", borderBottomColor: "#1d3343", borderBottomWidth: 1, flexDirection: "row", justifyContent: "space-between", paddingBottom: 9 },
  contextLineLabel: { color: "#8198a8", fontSize: 11 },
  contextLineValue: { color: "#d8e4df", fontFamily: Platform.OS === "ios" ? "Menlo" : "monospace", fontSize: 11, fontWeight: "800" },
  contextValueGood: { color: "#6de2bc" },
  contextValueMuted: { color: "#e1ba7a" },
  contextAction: { alignItems: "center", backgroundColor: "#102a35", borderColor: "#2a6e70", borderRadius: 11, borderWidth: 1, flexDirection: "row", justifyContent: "space-between", paddingHorizontal: 11, paddingVertical: 10 },
  contextActionText: { color: "#9de5cb", fontSize: 11, fontWeight: "900" },
  contextActionArrow: { color: "#58d6b0", fontSize: 15, fontWeight: "800" },
  contextDescription: { color: "#91a5b2", fontSize: 12, lineHeight: 19 },
  contextDivider: { backgroundColor: "#203747", height: 1, width: "100%" },
  contextFootnote: { color: "#698293", fontSize: 9, fontWeight: "800", letterSpacing: 0.8, lineHeight: 14 },
  chatHeader: { alignItems: "center", borderBottomColor: "#203747", borderBottomWidth: 1, flexDirection: "row", justifyContent: "space-between", paddingBottom: 15 },
  chatHeaderIdentity: { alignItems: "center", flexDirection: "row", flexShrink: 1, gap: 11 },
  chatHeaderOrb: { alignItems: "center", backgroundColor: "#102c3a", borderColor: "#2e7580", borderRadius: 13, borderWidth: 1, height: 38, justifyContent: "center", width: 38 },
  chatHeaderOrbText: { color: "#62dab5", fontSize: 18, fontWeight: "800" },
  chatHeaderKicker: { color: "#63e6be", fontSize: 10, fontWeight: "900", letterSpacing: 1.3 },
  chatHeaderTitle: { color: "#f4f7fb", fontSize: 18, fontWeight: "900", marginTop: 3 },
  chatHeaderMeta: { color: "#8197ae", fontSize: 11, marginTop: 3 },
  chatHeaderActions: { alignItems: "center", flexDirection: "row", gap: 6 },
  headerButton: { backgroundColor: "#102334", borderColor: "#2b5068", borderRadius: 10, borderWidth: 1, paddingHorizontal: 11, paddingVertical: 8 },
  headerButtonText: { color: "#c1d4d9", fontSize: 11, fontWeight: "800" },
  centered: { alignItems: "center", backgroundColor: "#08111f", flex: 1, gap: 12, justifyContent: "center" },
  loginCard: { alignSelf: "center", backgroundColor: "#0b1d2b", borderColor: "#274558", borderRadius: 24, borderWidth: 1, gap: 14, marginTop: 70, maxWidth: 480, padding: 28, width: "100%" },
  header: { alignItems: "flex-start", flexDirection: "row", justifyContent: "space-between", paddingBottom: 12 },
  headerText: { flex: 1, paddingRight: 12 },
  headerActions: { alignItems: "flex-end", gap: 8 },
  historyButton: { borderColor: "#2a6e70", borderRadius: 10, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 8 },
  historyButtonText: { color: "#a9e8d2", fontWeight: "800" },
  projectButton: { borderColor: "#4c5f8a", borderRadius: 10, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 8 },
  projectButtonText: { color: "#c7d3ff", fontWeight: "800" },
  eyebrow: { color: "#63e6be", fontSize: 12, fontWeight: "800", letterSpacing: 2 },
  title: { color: "#f5f0e7", fontFamily: Platform.OS === "ios" ? "Georgia" : "serif", fontSize: 32, fontWeight: "800", lineHeight: 38 },
  headerTitle: { color: "#f5f0e7", fontFamily: Platform.OS === "ios" ? "Georgia" : "serif", fontSize: 23, fontWeight: "800" },
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
  historyPanel: { backgroundColor: "#0b1d2b", borderColor: "#1d384b", borderRadius: 14, borderWidth: 1, gap: 8, marginBottom: 10, padding: 10 },
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
  projectPanel: { backgroundColor: "#0b1d2b", borderColor: "#27465f", borderRadius: 14, borderWidth: 1, gap: 8, marginBottom: 10, padding: 12 },
  filePanel: { backgroundColor: "#102334", borderColor: "#33706c", borderRadius: 14, borderWidth: 1, gap: 8, marginBottom: 10, padding: 12 },
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
  runBar: { alignItems: "center", borderBottomColor: "#182b40", borderBottomWidth: 1, flexDirection: "row", justifyContent: "space-between", paddingBottom: 10 },
  runStatus: { alignItems: "center", flexDirection: "row", gap: 8 },
  runDot: { backgroundColor: "#60758e", borderRadius: 4, height: 8, width: 8 },
  runDotBusy: { backgroundColor: "#f2c879" },
  runStatusText: { color: "#8fa4b9", fontSize: 12, fontWeight: "700" },
  refreshText: { color: "#7ce7c5", fontWeight: "800" },
  approvalCard: { backgroundColor: "#332b18", borderColor: "#8a6d2f", borderRadius: 16, borderWidth: 1, gap: 8, marginBottom: 10, padding: 14 },
  approvalTitle: { color: "#ffe7a3", fontSize: 16, fontWeight: "800" },
  approvalArguments: { color: "#e8d8ad", fontFamily: Platform.OS === "ios" ? "Menlo" : "monospace", fontSize: 12 },
  approvalActions: { flexDirection: "row", gap: 8 },
  messageList: { flexGrow: 1, gap: 14, paddingBottom: 20, paddingTop: 16 },
  emptyState: { alignItems: "center", backgroundColor: "#f4efe5", borderRadius: 18, gap: 10, marginTop: 58, paddingHorizontal: 24, paddingVertical: 36 },
  emptyBadge: { backgroundColor: "#e3f1e9", borderColor: "#8ac7b3", borderRadius: 999, borderWidth: 1, paddingHorizontal: 11, paddingVertical: 6 },
  emptyBadgeText: { color: "#83dfc1", fontSize: 9, fontWeight: "900", letterSpacing: 1.2 },
  emptyTitle: { color: "#17303a", fontFamily: Platform.OS === "ios" ? "Georgia" : "serif", fontSize: 25, fontWeight: "900", letterSpacing: -0.4 },
  emptyTips: { alignItems: "center", flexDirection: "row", flexWrap: "wrap", gap: 12, justifyContent: "center", marginTop: 4 },
  emptyTip: { color: "#536a75", fontSize: 11 },
  bubble: { borderRadius: 18, maxWidth: "86%", padding: 16 },
  userBubble: { alignSelf: "flex-end", backgroundColor: "#19595a", borderBottomRightRadius: 5 },
  assistantBubble: { alignSelf: "flex-start", backgroundColor: "#f3eee4", borderColor: "#d9cec0", borderWidth: 1, borderBottomLeftRadius: 5 },
  bubbleRole: { color: "#63e6be", fontSize: 11, fontWeight: "800", letterSpacing: 1, marginBottom: 4, textTransform: "uppercase" },
  bubbleText: { color: "#15303a", fontSize: 15, lineHeight: 23 },
  thinkingRow: { alignItems: "center", flexDirection: "row", gap: 9, paddingVertical: 3 },
  thinkingText: { color: "#91b5b1", fontSize: 13, fontStyle: "italic" },
  attachmentText: { color: "#a9e8d2", fontSize: 12, marginTop: 8 },
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
  composer: { backgroundColor: "#0c1d2b", borderColor: "#2b4b60", borderRadius: 18, borderWidth: 1, gap: 12, marginBottom: 4, padding: 14 },
  composerHeader: { alignItems: "center", flexDirection: "row", justifyContent: "space-between" },
  composerSubtext: { color: "#7189a0", fontSize: 11, marginTop: 3 },
  composerModel: { backgroundColor: "#18383b", borderColor: "#367675", borderRadius: 8, borderWidth: 1, color: "#98e2c8", fontSize: 10, fontWeight: "900", paddingHorizontal: 9, paddingVertical: 6 },
  composerActions: { gap: 7 },
  composerLabel: { color: "#8ea7c4", fontSize: 10, fontWeight: "900", letterSpacing: 1.2, textTransform: "uppercase" },
  actionGroup: { flexDirection: "row", flexWrap: "wrap", gap: 7 },
  actionButton: { alignItems: "center", backgroundColor: "#102237", borderColor: "#2b4b67", borderRadius: 10, borderWidth: 1, justifyContent: "center", minHeight: 38, paddingHorizontal: 11 },
  actionButtonText: { color: "#c5e5ff", fontSize: 12, fontWeight: "800" },
  composerInputRow: { alignItems: "flex-end", flexDirection: "row", gap: 8 },
  promptInput: { backgroundColor: "#07111e", borderColor: "#2a4862", borderRadius: 13, borderWidth: 1, color: "#f4f8fc", flex: 1, fontSize: 15, maxHeight: 120, minHeight: 52, paddingHorizontal: 14, paddingVertical: 11 },
  sendButton: { alignItems: "center", backgroundColor: "#7ce7c5", borderRadius: 12, justifyContent: "center", minHeight: 46, minWidth: 86, paddingHorizontal: 14, shadowColor: "#48c7a0", shadowOpacity: 0.2, shadowRadius: 10 },
  sendButtonText: { color: "#08111f", fontWeight: "800" },
});
