# MX App

O `mx-app` é o cliente universal do MX Core para **web, Android e iOS**, construído com Expo, React Native e TypeScript. O aplicativo conversa exclusivamente com a API do MX Core; nenhuma skill, tool ou Ollama é chamada diretamente pelo cliente.

## Desenvolvimento local

Na pasta `clients/mx-app`, instale dependências e execute:

```powershell
npm install
npm run start
```

Comandos por plataforma:

```powershell
npm run web
npm run android
npm run ios
npm run typecheck
npx expo export --platform web
```

Configure o backend com:

```powershell
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
```

`localhost` funciona para web na mesma máquina e deve ser substituído pelo endereço acessível do host ao usar dispositivo físico. No Android Emulator, use a convenção de host adequada ao ambiente quando necessário.

## Contrato consumido

O cliente usa os seguintes recursos:

| Recurso | Finalidade |
|---|---|
| `POST /api/auth/login` | Criar sessão e receber access/refresh token. |
| `POST /api/auth/refresh` | Rotacionar refresh token e renovar access token. |
| `POST /api/auth/logout` | Revogar a sessão atual. |
| `POST /api/v1/conversations/messages` | Enviar mensagem de forma síncrona. |
| `POST /api/v1/conversations/messages/stream` | Receber resposta incremental por SSE. |
| `GET /api/v1/runs/{runId}` | Consultar estado persistido do run. |
| `POST /api/v1/runs/{runId}/approve` | Aprovar um run aguardando decisão. |
| `POST /api/v1/runs/{runId}/reject` | Rejeitar um run com motivo. |

O cliente possui `sendMessageStream()`, parser SSE, atualização token a token, `getRun()`, `approveRun()` e `rejectRun()`. Os tipos de status incluem `RECEIVED`, `ROUTED`, `EXECUTING`, `VERIFYING`, `AWAITING_APPROVAL`, `COMPLETED`, `FAILED` e `CANCELLED`.

## Sessão e segurança

No navegador, a sessão usa `localStorage` como adapter compatível com web local. Em Android e iOS, access token e refresh token usam `expo-secure-store`. O refresh automático é deduplicado para evitar rotações concorrentes; se a renovação falhar, a sessão local é limpa.

Tokens não são colocados em URL. O cliente não decide autonomia, não seleciona skills, não executa tools e não recebe credenciais do Ollama. A autorização final permanece no backend.

## Estado atual da experiência

O shell atual cobre login, restauração de sessão, chat, streaming e logout. APIs de runs e aprovação já estão disponíveis no cliente, mas o painel visual completo de histórico, reconexão cross-channel e aprovação contextual ainda está em evolução.
