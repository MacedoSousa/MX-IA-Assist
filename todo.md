# Migração do portal profissional para o MX

- [x] Mapear os componentes, estados e integrações existentes no cliente Expo.
- [x] Adaptar o shell profissional, a navegação e a visão geral ao fluxo real de chat.
- [x] Preservar os fluxos existentes de anexos, geração de imagem, geração de vídeo, histórico e autenticação durante a migração visual.
- [x] Sincronizar o novo cliente para o Windows e reconstruir somente o serviço `mx-web`.
- [x] Validar o Web UI local e remoto, executar o typecheck e publicar o commit no GitHub.

## Correção de anexos e geração de imagens

- [x] Inspecionar logs do cliente, MX Core e Forge para identificar os erros dos dois fluxos.
- [x] Corrigir o envio multipart de arquivos e o acionamento da geração de imagens.
- [x] Reconstruir os serviços afetados e realizar smoke tests autenticados.
- [ ] Publicar as correções e registrar os resultados da validação.
