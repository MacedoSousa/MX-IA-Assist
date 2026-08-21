import AsyncStorage from "@react-native-async-storage/async-storage";
import * as SecureStore from "expo-secure-store";
import { Platform } from "react-native";

const ACCESS_TOKEN_KEY = "mx.access-token";
const REFRESH_TOKEN_KEY = "mx.refresh-token";
const CONVERSATION_ID_KEY = "mx.conversation-id";
const RUN_CURSOR_KEY = "mx.run-cursor";
const PROJECTS_KEY = "mx.projects";

export type LocalProject = {
  id: string;
  name: string;
  path: string;
  createdAt: string;
};

async function readValue(key: string): Promise<string | null> {
  if (Platform.OS === "web") {
    return globalThis.localStorage?.getItem(key) ?? null;
  }

  return SecureStore.getItemAsync(key);
}

async function writeValue(key: string, value: string): Promise<void> {
  if (Platform.OS === "web") {
    globalThis.localStorage?.setItem(key, value);
    return;
  }

  await SecureStore.setItemAsync(key, value, {
    keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
  });
}

async function clearValue(key: string): Promise<void> {
  if (Platform.OS === "web") {
    globalThis.localStorage?.removeItem(key);
    return;
  }

  await SecureStore.deleteItemAsync(key);
}

export async function readAccessToken(): Promise<string | null> {
  return readValue(ACCESS_TOKEN_KEY);
}

export async function readRefreshToken(): Promise<string | null> {
  return readValue(REFRESH_TOKEN_KEY);
}

export async function writeAccessToken(token: string): Promise<void> {
  await writeValue(ACCESS_TOKEN_KEY, token);
}

export async function writeSession(accessToken: string, refreshToken: string): Promise<void> {
  await writeValue(ACCESS_TOKEN_KEY, accessToken);
  await writeValue(REFRESH_TOKEN_KEY, refreshToken);
}

export async function clearAccessToken(): Promise<void> {
  await clearValue(ACCESS_TOKEN_KEY);
}

export async function clearSession(): Promise<void> {
  await Promise.all([
    clearValue(ACCESS_TOKEN_KEY),
    clearValue(REFRESH_TOKEN_KEY),
    AsyncStorage.removeItem(CONVERSATION_ID_KEY),
    AsyncStorage.removeItem(RUN_CURSOR_KEY),
    AsyncStorage.removeItem("mx.draft-prompt"),
  ]);
}

export async function readDraftPrompt(): Promise<string> {
  return (await AsyncStorage.getItem("mx.draft-prompt")) ?? "";
}

export async function writeDraftPrompt(prompt: string): Promise<void> {
  await AsyncStorage.setItem("mx.draft-prompt", prompt);
}

export async function readConversationId(): Promise<string | null> {
  return AsyncStorage.getItem(CONVERSATION_ID_KEY);
}

export async function writeConversationId(conversationId: string | null): Promise<void> {
  if (conversationId) {
    await AsyncStorage.setItem(CONVERSATION_ID_KEY, conversationId);
  } else {
    await AsyncStorage.removeItem(CONVERSATION_ID_KEY);
  }
}

export async function readRunCursor(): Promise<string | null> {
  return AsyncStorage.getItem(RUN_CURSOR_KEY);
}

export async function writeRunCursor(cursor: string | null): Promise<void> {
  if (cursor) {
    await AsyncStorage.setItem(RUN_CURSOR_KEY, cursor);
  } else {
    await AsyncStorage.removeItem(RUN_CURSOR_KEY);
  }
}

export async function readProjects(): Promise<LocalProject[]> {
  const raw = await AsyncStorage.getItem(PROJECTS_KEY);
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw) as unknown;
    return Array.isArray(parsed) ? parsed as LocalProject[] : [];
  } catch {
    return [];
  }
}

export async function writeProjects(projects: LocalProject[]): Promise<void> {
  await AsyncStorage.setItem(PROJECTS_KEY, JSON.stringify(projects));
}
