# Inventário de pendências do protótipo MX

**Data de consolidação:** 27 de agosto de 2026. Este documento é a fonte de priorização para marcadores não concluídos em `todo.md`, roadmap, arquitetura, operação, segurança, testes e backlog. Marcadores meramente históricos não representam trabalho automaticamente pendente.

## P0 — Caminho de uso verificável

| Item | Evidência atual | Próxima entrega e aceite |
|---|---|---|
| Login pela UI | O bloqueio CORS de `127.0.0.1:8082` foi corrigido; falta uma sessão real confirmada no navegador. | Login válido abre a sessão, restaura conversas e permite logout sem expor token. |
| Topologia central | UI Docker (`8082`), Core Docker (`8080`) e Core nativo shadow (`18080`) coexistem para prototipação. | Uma topologia escolhida explicitamente para a UI, com launcher/status e rollback documentados. |
| Banco central | Docker e Core shadow usam bancos PostgreSQL distintos durante a migração. | Decisão documentada e migração somente após backup/restauração verificável; não haverá cópia manual de diretório de dados. |
| Cliente real | Expo contém os fluxos funcionais, mas ainda é monolítico e a validação de interface não está completa. | Fluxos de login, chat, conversa, anexo e aprovação testados no cliente e apontando para o Core escolhido. |

## P1 — Capacidades locais e segurança operacional

| Item | Evidência atual | Próxima entrega e aceite |
|---|---|---|
| Runtime direto | Ollama, PostgreSQL e Core shadow foram validados; Docker continua reversão de protótipo. | Startup reversível de Core/Expo, healthchecks e plano de retorno, sem desligar o caminho Docker. |
| PostgreSQL/Redis em rede | O PostgreSQL nativo ainda requer revisão de bind; a decisão do Redis direto permanece aberta. | Exposição mínima documentada, validação local e nenhuma alteração às cegas do diretório de dados. |
| Áudio/GPU | Whisper funciona em CPU; `nvidia-smi`/NVML falha. | Transcrição de áudio autorizado ponta a ponta; CUDA somente após driver/NVML saudável. |
| Imagem e vídeo | PDF/FFmpeg foram validados; Forge é transitório em Docker. | Forge nativo validado ou dependência transitória formalizada com healthcheck e limite operacional. |
| Workspace | Bootstrap estático, aprovação, preview loopback e encerramento foram validados. | Recipes separados de validação e build com staging, timeout, allowlist e evidência. |
| Conta de smoke | A credencial foi limpa, mas a conta temporária não possui exclusão administrativa governada. | Desativação/remoção auditável sem SQL ad hoc. |

## P2 — Evolução de produto e qualidade

| Item | Evidência atual | Próxima entrega e aceite |
|---|---|---|
| RAG | Citações rastreáveis e limites de contexto existem; ranking é lexical. | Filtro de proprietário, embeddings/vector store, reranking e avaliação preservando as citações. |
| Skills autorizadas | Conhecimento documental autorizado já é importado e auditável. | Mapa de skills de jogos/documentos/imagem e avaliações, sem treinamento automático ou fonte não autorizada. |
| Expo multiplataforma | Base Web/Android/iOS existe. | Teste via LAN/Tailscale em dispositivo autorizado, com CORS explícito e sem credencial embutida. |
| UI Ateliê de Inteligência | Direção visual existe; rotas e componentes ainda pedem modularização. | App modular com ações realmente conectadas à API, acessibilidade e sem controles simulados. |
| Mintlify | Ainda é documentação versionada local; não há domínio ou credenciais assumidos. | Estrutura Mintlify PT-BR/EN local; publicação só após configuração explícita. |

## Itens documentais a atualizar, não reimplementar

Alguns textos ainda descrevem estados já superados: `docs/approvals-and-tools.md` e `docs/mx-core-skills-architecture.md` mencionam a ausência da ponte de aprovação para `ExecutionRun`, mas a aprovação pós-nonce foi implementada e validada; `docs/product-backlog.md` possui linhas históricas semelhantes para runs e refresh; e `docs/security/threat-model.md` requer atualização de T-08 conforme nonce, expiração e uso único já entregues. Esses arquivos devem ser corrigidos na frente de documentação, sem duplicar a funcionalidade.

## Ordem de execução

O próximo incremento deve concluir o teste de login real e escolher a rota unificada da UI. Depois, a sequência é: áudio e infraestrutura nativa; recipes de workspace e RAG; cliente Expo modular e documentação Mintlify. Cada incremento terá teste, evidência e rollback explícito; Docker, dados, anexos e backups permanecem preservados até que os critérios de migração sejam atendidos.
