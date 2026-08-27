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

## Auditoria de obsolescência e desempenho

- [ ] Inventariar versões, dependências e gargalos de tempo, memória e GPU.
- [ ] Classificar componentes obsoletos, riscos técnicos e oportunidades de alto impacto.
- [ ] Publicar o roadmap técnico priorizado, sem aplicar atualizações de versão não validadas.

## Processamento de anexos

- [x] Mapear o fluxo entre upload, extração de conteúdo e mensagens da conversa.
- [x] Implementar extração segura e contexto de anexos por conversa.
- [x] Corrigir a serialização do contrato de mensagem usada na validação integrada.
- [x] Preservar imagens anexadas até o gateway visual, sem descartá-las no roteador de skills.
- [x] Validar PDF, texto e imagem em uma resposta fundamentada do MX.
- [x] Publicar a correção sem versionar anexos nem dados pessoais.

## Auditoria sistêmica de fluxo

- [x] Mapear contratos e regras que descartam contexto, impedem ferramentas ou criam estados inconsistentes.
- [x] Corrigir desconexões de baixo risco entre chat, skills, IA, anexos, tarefas e execução de ferramentas.
- [x] Validar os fluxos críticos e publicar o diagnóstico de regras que exigem evolução arquitetural.

## IA generativa: imagens, vídeos e documentos

- [x] Recuperar e sintetizar os materiais autorizados sobre IA generativa referenciados pelo usuário.
- [x] Mapear os contratos atuais de geração, análise, armazenamento e entrega de imagens, vídeos e documentos.
- [x] Implementar as lacunas priorizadas com limites, validação de entrada, rastreabilidade e mensagens de erro úteis.
- [x] Executar smoke tests locais para cada modalidade sem versionar mídia ou dados pessoais.
- [x] Publicar a documentação de capacidades, restrições técnicas e fluxo de uso no MX.

## Roteamento conversacional de skills e mídia

- [x] Reproduzir a divergência entre o pedido no chat e os botões de geração de mídia.
- [x] Corrigir a identificação de intenção e a delegação segura do chat para as capacidades de imagem, vídeo e documento.
- [x] Validar respostas, políticas e artefatos pelos dois caminhos de interação.
- [x] Publicar o diagnóstico e a correção sem alterar memórias ou anexos existentes.

## Evolução local, skills e ambiente de desenvolvimento

- [ ] Auditar o runtime atual e definir a migração controlada dos serviços MX para execução direta no Windows, com inicialização automática, logs e reversão documentada.
- [x] Validar a autenticação das instâncias PostgreSQL nativas nas portas 15432 e 15433, restaurar o dump mais recente em uma instância paralela e reconciliar o perfil local antes do corte.
- [ ] Projetar um workspace local restrito para arquivos, terminal, execução de builds, prévia de projetos e coleta de evidências, preservando aprovação para operações de escrita ou publicação.
- [ ] Mapear o conhecimento autorizado disponível e definir skills para criação de jogos, documentos, imagens detalhadas e conversas com melhor planejamento e revisão.
- [ ] Avaliar integrações locais complementares, incluindo Obsidian, Mintlify, ferramentas de desenvolvimento e automações compatíveis com o hardware atual.
- [ ] Refatorar o WebUI em módulos e rotas funcionais, tomando o portfólio local como referência visual e conectando as ações à API do MX Core.
- [ ] Validar a execução direta, segurança do workspace, fluxos de criação e experiência responsiva; publicar o roadmap e as correções aprovadas.

### Evidências da etapa shadow nativa

- [x] Corrigir a inicialização Flyway com privilégios mínimos para a conta de aplicação no banco restaurado.
- [x] Confirmar healthcheck, login, conversa, PDF anexado, imagem e documento no Core nativo em `18080`.
- [x] Tornar as portas do PostgreSQL/Ollama explícitas nos launchers e no verificador Windows.
- [ ] Substituir ou formalizar a dependência transitória do Forge em Docker e validar transcrição com fala real; FFmpeg e vídeo nativo já foram verificados.
- [ ] Configurar inicialização persistente para Core/Expo e endurecer a exposição de rede antes do corte da produção Docker; Ollama possui tarefa reversível no logon.
- [ ] Corrigir a inicialização NVML do driver NVIDIA e validar PyTorch CUDA antes de habilitar Whisper acelerado por GPU; o fallback CPU está instalado.

### Evolução RAG documental

- [x] Exibir citações rastreáveis com origem, seção, trecho, hash da versão e indicação honesta de página ausente.
- [x] Cobrir `rag-001`, `rag-002` e `rag-003` com testes automatizados e smoke test no Core nativo.
- [x] Incluir QualitySkill e especialistas de estudos no contrato de contexto e resposta com fontes.
- [ ] Evoluir do ranking lexical para recuperação semântica com embeddings, vector store, reranking e filtro de proprietário, mantendo o contrato de citação.

### Workspace governado

- [x] Retomar uma tool sensível registrada somente após aprovação com nonce, ownership e expiração válidos, persistindo evidência de conclusão ou falha.
- [x] Serializar argumentos pendentes de tools em JSON estruturado, impedindo alteração de intenção no fluxo de aprovação.
- [x] Implementar receita de bootstrap estático e preview em loopback, com fila host-side, portas temporárias reservadas, logs e encerramento explícito, sem shell livre.
- [x] Corrigir a colisão entre a run de conversa e a run de aprovação, garantindo `correlationId` exclusivo na persistência PostgreSQL.
- [x] Cobrir a regressão de correlação da run de aprovação e repetir o smoke autenticado de criação/preview com chave de idempotência inédita.
- [ ] Implementar receitas distintas para validação estática e build de perfis permitidos, com staging, timeout e evidência antes de qualquer publicação.

### Uso centralizado do protótipo

- [x] Diagnosticar a resposta da rota raiz do Core shadow e documentar o endpoint técnico correto, sem apresentá-lo como interface de usuário.
- [x] Criar um ponto de controle local único para o protótipo, com estados, healthchecks e ações reversíveis permitidas.
- [ ] Validar o acesso pelo cliente funcional antes de qualquer decisão sobre promoção ou corte do Docker.

### Login do cliente MX

- [x] Mapear a URL de API e o fluxo de autenticação efetivamente usados pela interface em `8082`.
- [x] Corrigir a divergência entre cliente, Core de referência e Core shadow sem alterar contas, senhas ou dados persistidos.
- [ ] Validar login e sessão pelo cliente funcional com credencial fornecida localmente, sem registrar segredo em scripts ou logs.

### Acesso local de protótipo

- [x] Capturar a causa sanitizada da falha de login relatada pelo usuário e validar o vínculo entre a UI, proxy, Core e banco do caminho Docker.
- [x] Comparar as contas e esquemas dos bancos Docker e nativo sem expor e-mails, hashes ou senhas, corrigindo somente a rota/configuração que divergir.
- [x] Definir e validar um modo CMD local sem cadastro, limitado a conversa efêmera e sem acesso a memória, anexos, runs, arquivos ou ações sensíveis.
