import { StatusBar } from "expo-status-bar";
import { useEffect, useMemo, useState } from "react";
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

import { MxApiError, mxApi, type ChatMessage } from "./src/api/client";
import {
  clearSession,
  readAccessToken,
  readRefreshToken,
  writeSession,
} from "./src/session/session";

type ViewState = "checking" | "login" | "chat";

type RenderMessage = ChatMessage & {
  id: string;
};

export default function App() {
  const [viewState, setViewState] = useState<ViewState>("checking");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [prompt, setPrompt] = useState("");
  const [messages, setMessages] = useState<RenderMessage[]>([]);
  const [conversationId, setConversationId] = useState<string | undefined>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function restoreSession() {
      try {
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

  const subtitle = useMemo(() => {
    if (viewState === "checking") return "Preparando sua sessão local";
    if (viewState === "login") return "Seu assistente pessoal, em todos os seus dispositivos";
    return "Converse com o MX Core e deixe as skills especialistas trabalharem por você";
  }, [viewState]);

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
    if (!value || busy) return;

    const requestId = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
    const assistantId = `${requestId}-assistant`;
    setPrompt("");
    setError(null);
    setMessages((current) => [
      ...current,
      { id: `${requestId}-user`, role: "USER", content: value },
      { id: assistantId, role: "ASSISTANT", content: "" },
    ]);
    setBusy(true);

    try {
      const response = await mxApi.sendMessageStream(value, conversationId, requestId, {
        onToken: ({ delta }) => {
          setMessages((current) => current.map((message) =>
            message.id === assistantId
              ? { ...message, content: `${message.content}${delta}` }
              : message,
          ));
        },
      });
      setConversationId(response.conversationId);
      setMessages((current) => current.map((message) =>
        message.id === assistantId
          ? { ...message, content: response.answer }
          : message,
      ));
    } catch (cause) {
      setMessages((current) => current.filter((message) => message.id !== assistantId));
      if (cause instanceof MxApiError && cause.status === 401) {
        await clearSession();
        setViewState("login");
      }
      setError(cause instanceof MxApiError ? cause.message : "O MX não conseguiu concluir a mensagem.");
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
      setConversationId(undefined);
      setViewState("login");
    }
  }

  if (viewState === "checking") {
    return (
      <View style={styles.centered}>
        <ActivityIndicator color="#63e6be" />
        <Text style={styles.muted}>Conectando ao MX Core...</Text>
      </View>
    );
  }

  if (viewState === "login") {
    return (
      <KeyboardAvoidingView
        style={styles.screen}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <StatusBar style="light" />
        <View style={styles.loginCard}>
          <Text style={styles.eyebrow}>MX CORE</Text>
          <Text style={styles.title}>Seu assistente central.</Text>
          <Text style={styles.subtitle}>{subtitle}</Text>
          <TextInput
            autoCapitalize="none"
            autoComplete="email"
            keyboardType="email-address"
            onChangeText={setEmail}
            placeholder="seu@email.com"
            placeholderTextColor="#748198"
            style={styles.input}
            value={email}
          />
          <TextInput
            autoCapitalize="none"
            autoComplete="password"
            onChangeText={setPassword}
            placeholder="Senha"
            placeholderTextColor="#748198"
            secureTextEntry
            style={styles.input}
            value={password}
          />
          {error ? <Text style={styles.error}>{error}</Text> : null}
          <Pressable
            disabled={busy}
            onPress={handleLogin}
            style={({ pressed }) => [styles.primaryButton, pressed && styles.pressed, busy && styles.disabled]}
          >
            {busy ? <ActivityIndicator color="#08111f" /> : <Text style={styles.primaryButtonText}>Entrar no MX</Text>}
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.screen}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <StatusBar style="light" />
      <View style={styles.header}>
        <View>
          <Text style={styles.eyebrow}>MX CORE</Text>
          <Text style={styles.headerTitle}>Olá. Como posso ajudar?</Text>
          <Text style={styles.subtitle}>{subtitle}</Text>
        </View>
        <Pressable onPress={handleLogout} style={({ pressed }) => [styles.logout, pressed && styles.pressed]}>
          <Text style={styles.logoutText}>Sair</Text>
        </Pressable>
      </View>

      <FlatList
        contentContainerStyle={styles.messageList}
        data={messages}
        keyExtractor={(item) => item.id}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <Text style={styles.emptyTitle}>O MX está pronto.</Text>
            <Text style={styles.muted}>Comece uma conversa. O núcleo escolherá a skill especialista adequada.</Text>
          </View>
        }
        renderItem={({ item }) => (
          <View style={[styles.bubble, item.role === "USER" ? styles.userBubble : styles.assistantBubble]}>
            <Text style={styles.bubbleRole}>{item.role === "USER" ? "Você" : "MX"}</Text>
            <Text style={styles.bubbleText}>{item.content}</Text>
          </View>
        )}
      />

      {error ? <Text style={styles.error}>{error}</Text> : null}
      <View style={styles.composer}>
        <TextInput
          editable={!busy}
          multiline
          onChangeText={setPrompt}
          onSubmitEditing={handleSend}
          placeholder="Escreva sua solicitação..."
          placeholderTextColor="#748198"
          returnKeyType="send"
          style={styles.promptInput}
          value={prompt}
        />
        <Pressable
          disabled={busy || !prompt.trim()}
          onPress={handleSend}
          style={({ pressed }) => [styles.sendButton, pressed && styles.pressed, (busy || !prompt.trim()) && styles.disabled]}
        >
          {busy ? <ActivityIndicator color="#08111f" /> : <Text style={styles.sendButtonText}>Enviar</Text>}
        </Pressable>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: "#08111f",
    paddingHorizontal: 18,
    paddingTop: 36,
  },
  centered: {
    alignItems: "center",
    backgroundColor: "#08111f",
    flex: 1,
    gap: 12,
    justifyContent: "center",
  },
  loginCard: {
    alignSelf: "center",
    backgroundColor: "#101c2d",
    borderColor: "#1e3048",
    borderRadius: 24,
    borderWidth: 1,
    gap: 14,
    marginTop: 70,
    maxWidth: 480,
    padding: 28,
    width: "100%",
  },
  header: {
    alignItems: "flex-start",
    flexDirection: "row",
    justifyContent: "space-between",
    paddingBottom: 18,
  },
  eyebrow: {
    color: "#63e6be",
    fontSize: 12,
    fontWeight: "800",
    letterSpacing: 2,
  },
  title: {
    color: "#f4f7fb",
    fontSize: 32,
    fontWeight: "800",
    lineHeight: 38,
  },
  headerTitle: {
    color: "#f4f7fb",
    fontSize: 24,
    fontWeight: "800",
  },
  subtitle: {
    color: "#9cabc0",
    fontSize: 15,
    lineHeight: 22,
  },
  muted: {
    color: "#9cabc0",
    fontSize: 14,
    lineHeight: 21,
    textAlign: "center",
  },
  input: {
    backgroundColor: "#0b1728",
    borderColor: "#29405d",
    borderRadius: 12,
    borderWidth: 1,
    color: "#f4f7fb",
    fontSize: 16,
    paddingHorizontal: 14,
    paddingVertical: 13,
  },
  primaryButton: {
    alignItems: "center",
    backgroundColor: "#63e6be",
    borderRadius: 12,
    minHeight: 50,
    justifyContent: "center",
    marginTop: 4,
  },
  primaryButtonText: {
    color: "#08111f",
    fontSize: 16,
    fontWeight: "800",
  },
  disabled: {
    opacity: 0.55,
  },
  pressed: {
    opacity: 0.82,
    transform: [{ scale: 0.98 }],
  },
  error: {
    color: "#ff8b8b",
    fontSize: 13,
    lineHeight: 18,
  },
  logout: {
    borderColor: "#29405d",
    borderRadius: 10,
    borderWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  logoutText: {
    color: "#c4d1e3",
    fontWeight: "700",
  },
  messageList: {
    flexGrow: 1,
    gap: 12,
    paddingBottom: 16,
    paddingTop: 12,
  },
  emptyState: {
    alignItems: "center",
    gap: 8,
    marginTop: 110,
    paddingHorizontal: 24,
  },
  emptyTitle: {
    color: "#f4f7fb",
    fontSize: 20,
    fontWeight: "800",
  },
  bubble: {
    borderRadius: 16,
    maxWidth: "88%",
    padding: 14,
  },
  userBubble: {
    alignSelf: "flex-end",
    backgroundColor: "#164f52",
  },
  assistantBubble: {
    alignSelf: "flex-start",
    backgroundColor: "#101c2d",
    borderColor: "#1e3048",
    borderWidth: 1,
  },
  bubbleRole: {
    color: "#63e6be",
    fontSize: 11,
    fontWeight: "800",
    letterSpacing: 1,
    marginBottom: 4,
    textTransform: "uppercase",
  },
  bubbleText: {
    color: "#f4f7fb",
    fontSize: 16,
    lineHeight: 23,
  },
  composer: {
    alignItems: "flex-end",
    backgroundColor: "#101c2d",
    borderColor: "#1e3048",
    borderRadius: 16,
    borderWidth: 1,
    flexDirection: "row",
    gap: 10,
    marginBottom: 18,
    padding: 10,
  },
  promptInput: {
    color: "#f4f7fb",
    flex: 1,
    fontSize: 16,
    maxHeight: 120,
    minHeight: 42,
    paddingHorizontal: 6,
    paddingVertical: 9,
  },
  sendButton: {
    alignItems: "center",
    backgroundColor: "#63e6be",
    borderRadius: 10,
    justifyContent: "center",
    minHeight: 42,
    minWidth: 76,
    paddingHorizontal: 12,
  },
  sendButtonText: {
    color: "#08111f",
    fontWeight: "800",
  },
});
