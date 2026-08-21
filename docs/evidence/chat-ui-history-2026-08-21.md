# Evidência — interface de chat, arquivos, projetos e histórico

**Data:** 2026-08-21

## Diagnóstico

A interface acessada pelo usuário estava servindo um bundle antigo do `mx-web`. O código atualizado no clone sandbox já continha parte da experiência multimodal, mas o container web não havia sido reconstruído com esse bundle.

## Correções aplicadas

O cliente Expo universal foi atualizado para exibir controles de histórico, projetos, criação de arquivo local, seleção de pasta, anexação de documentos, colagem de texto e ações multimodais. O compositor aceita `Enter` para enviar e `Shift+Enter` para inserir nova linha. Os controles foram ajustados para quebra de linha e telas menores.

Projetos e pastas são persistidos localmente no dispositivo para organização contínua. Arquivos criados pelo usuário são transformados em anexos locais e enviados pelo mesmo fluxo seguro de checksum e allowlist MIME.

O login passou a restaurar automaticamente a conversa anteriormente selecionada e carregar o índice de conversas. Depois do primeiro envio, o `conversationId` retornado pelo MX Core é salvo localmente, o histórico da conversa é recarregado e o índice é atualizado.

## Validações

| Verificação | Resultado |
|---|---|
| TypeScript no sandbox | Aprovado com `npm run typecheck` |
| Exportação web no sandbox | Aprovada com `npx expo export --platform web` |
| TypeScript no Windows | Aprovado com `npm run typecheck` |
| Exportação web no Windows | Aprovada; bundle gerado |
| Container web | `mx-web` recriado sem dependências |
| Bundle servido | Bundle final contém os rótulos `Projetos`, `Criar`, `Colar` e `Enter` |
| API de histórico | Login válido; `12` conversas retornadas, `1` página |
| MX Core | Reconstruído e ativo com migrations de histórico/anexos |

O navegador sandbox não resolveu o hostname Tailscale, portanto a validação visual remota foi substituída por inspeção do bundle efetivamente servido dentro do nginx e por validação da API no Docker Desktop do Windows.
