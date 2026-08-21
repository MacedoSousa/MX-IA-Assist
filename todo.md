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
- [x] Publicar as correções e registrar os resultados da validação.

## Recreação segura dos contêineres

- [x] Registrar os dados persistentes que devem ser preservados antes da recriação.
- [x] Recriar apenas os serviços MX, sem usar remoção de volumes ou diretórios de dados.
- [x] Confirmar a saúde dos serviços e a preservação das memórias após a inicialização.

## Backup local verificável

- [x] Criar um snapshot consistente do PostgreSQL e dos diretórios persistentes do MX.
- [x] Gerar manifesto com hashes SHA-256 e instruções de restauração.
- [x] Registrar e publicar o backup operacional no Git, sem versionar dados sensíveis.

## Investigação de indisponibilidade e confiabilidade

- [x] Coletar eventos Docker, estados de reinicialização e logs dos serviços MX.
- [x] Determinar a causa provável da queda com evidências técnicas.
- [x] Implementar proteções de baixo risco para recuperação automática e observabilidade.
- [x] Validar a recuperação e publicar o diagnóstico priorizado.
