# MX — Auditoria Técnica e Roadmap de Evolução Local

**Autor:** Manus AI  
**Data:** 27 de agosto de 2026  
**Escopo:** execução direta no Windows, workspace de desenvolvimento, skills orientadas ao conhecimento autorizado e WebUI multiplataforma.

## Síntese executiva

O MX possui uma base sólida de orquestração, segurança de ferramentas, persistência conversacional e geração de mídia. A evolução solicitada não deve ser tratada como uma simples remoção do Docker: atualmente o ambiente de contêineres concentra dependências de estado, principalmente PostgreSQL, Ollama e Forge. A migração segura precisa preservar dados por exportação e restauração, manter um mecanismo de inicialização automática e separar claramente o que o modelo pode **propor**, o que o host pode **validar** e o que somente o usuário pode **aprovar**.

O produto também possui dois frontends com papéis diferentes. O cliente Expo em `clients/mx-app` é o canal funcional para Web, Android e iOS; o portal estático em `mx-professional-web` representa uma direção visual, mas contém interações demonstrativas sem comunicação com o MX Core. A prioridade é tornar o cliente Expo a experiência operacional única e usar o portal estático apenas como referência de composição, tipografia e identidade durante a refatoração.

## Evidências da linha de base

| Área | Evidência observada | Impacto no plano |
|---|---|---|
| Execução direta | `scripts/run-backend.bat` e `scripts/run-expo-web.bat` já iniciam Core e Expo fora do Docker. | Há uma base para migração incremental; falta inicializar e verificar todas as dependências nativas. |
| Banco de dados | O perfil `dev` usa PostgreSQL em `localhost:5432`; o Compose mantém dados em `data/postgres`. | A base Linux do contêiner não deve ser reutilizada como diretório de dados de uma instalação nativa; a migração exige dump e restore validados. |
| Cache | Não há uso direto de `RedisTemplate` ou APIs Redis no código Java auditado. | Redis não deve bloquear a primeira execução direta; sua permanência deve ser decidida após teste de integração e remoção controlada de configuração ociosa. |
| Modelo local | Não foi identificado processo ou serviço Ollama nativo no Windows durante a auditoria; o Core aponta para `localhost:11434`. | Instalar, validar e iniciar Ollama no host é pré-requisito antes de desligar `mx-ollama`. |
| Imagem | `ImageGenerationService` usa payload mínimo e URL padrão `host.docker.internal:7860`. | É necessário um launcher nativo do Forge, configuração para `127.0.0.1:7860` e um contrato de geração mais expressivo. |
| Documento | `DocumentGenerationService` usa fonte fixa de caminho Linux para PDF. | A geração de PDF deve localizar fonte por configuração ou distribuir uma fonte compatível com Windows. |
| Workspace | Leitura e escrita já bloqueiam escape de diretório e symlinks; escrita exige aprovação. | Faltam modelo de projeto persistido, diff, prévia, execução de testes e registro de evidência. |
| Automação de código | `mx_evolution_runner.py` valida paths, comandos e worktree antes de gravar e versionar. | Este é o caminho seguro para builds e commits locais; não se deve expor um terminal arbitrário ao modelo. |
| Conversas e contexto | O cliente Expo possui chat, SSE, anexos, CRUD e preferência; projetos são metadados locais. | Projetos precisam de persistência no Core, relação com conversas/anexos e busca documental com citações. |
| WebUI | O cliente Expo é funcional; o portal estático é um protótipo editorial com botões simulados. | Refatorar o cliente Expo, extraindo shell, rotas e telas; não manter dois produtos concorrentes. |

## Decisão de produto recomendada

O MX continuará sendo o único interlocutor. O WebUI não recebe um terminal irrestrito e skills não recebem acesso direto ao sistema. A nova experiência apresenta um **workspace local governado**, em que o usuário escolhe ou registra uma pasta, o MX lê e propõe alterações, o sistema mostra diff, validações e prévia, e a escrita ou publicação sensível depende de aprovação explícita.

> O modelo descreve intenção e propõe um plano. O Core aplica políticas. O executor local só aceita operações declaradas e validadas. A pessoa aprova efeitos de escrita, execução ou publicação conforme o risco.

## Backlog priorizado

| Prioridade | Entrega | Valor | Critério de aceite |
|---|---|---|---|
| P0 | Migração local reversível | Remove dependência de Docker sem risco de perda de conversas, anexos ou modelos. | Backup restaurável, serviços nativos em saúde, launcher único e retorno documentado ao Compose. |
| P0 | Ajustes de portabilidade | Permite que Core, PDF, mídia e logs rodem no Windows. | Perfis `local-windows`, fonte PDF configurável, paths externos e healthchecks nativos. |
| P0 | Workspace seguro | Viabiliza criação de arquivos, builds e prévia com rastreabilidade. | Raiz por projeto, allowlist de comandos, diff, aprovação, timeout e evidência persistida. |
| P1 | Projetos persistidos | Une conversas, arquivos, decisões e workspaces em todos os dispositivos. | CRUD autenticado no Core e sincronização no cliente universal. |
| P1 | Skill RAG documental | Melhora respostas sobre o acervo autorizado com fontes e versões. | Chunking, metadados, recuperação por usuário, citações válidas e casos `rag-001` a `rag-003`. |
| P1 | Skill de criação de jogos | Transforma pedidos em escopo, loop jogável, assets e validação. | GDD breve, estrutura de projeto, preview local e testes de build sem executar comandos livres. |
| P1 | Geração de imagem estruturada | Aumenta coerência visual e controle de resultado. | Brief de assunto/composição/estilo, negativo, seed, dimensões, modelo, artefato e metadados. |
| P1 | Conversa planejada e verificável | Melhora respostas longas e continuidade. | Planejamento explícito para tarefas complexas, contexto com limite por tokens e indicações de evidência/incerteza. |
| P2 | Integração de notas | Permite importar e exportar notas sem transformar arquivos em comandos. | Vault selecionado, leitura indexada, escrita aprovada e sincronização auditável. |
| P2 | Painel operacional | Dá visibilidade a saúde, jobs, builds, artefatos e aprovações. | Estados reais da API; nenhum cartão, métrica ou ação simulada. |
| P2 | Avaliação contínua | Impede regressões de segurança, RAG, UX e performance. | Suite versionada executada em CI/local antes de promoção de skill ou modelo. |

## Sequência de execução

### 1. Preparar a execução direta sem desligar o ambiente atual

Será criado um perfil `local-windows`, um verificador de pré-requisitos e launchers supervisionados. Antes de desligar qualquer serviço em contêiner, o processo exportará PostgreSQL, validará hashes e restaurará a cópia em uma instância local separada. Ollama, Forge e transcrição serão verificados por endpoint ou comando local; cada um terá logs e healthcheck próprios.

### 2. Construir o workspace de desenvolvimento governado

O executor host existente será estendido por contratos declarativos: criar arquivo, editar por patch, listar, ler, rodar validação conhecida, iniciar preview e encerrar preview. A interface receberá preview em painel, saída limitada, links locais, diff e estados de aprovação. Publicação, push e operações fora das raízes cadastradas continuarão bloqueados por padrão.

### 3. Criar skills com base no conhecimento autorizado

As novas skills serão implementadas somente depois de terem contratos, restrições e avaliações. A primeira será a recuperação documental, pois ela aumenta qualidade de conversa e permite citar origem, página e versão. Em seguida, serão implementadas as skills de jogos e mídia, reutilizando o Core, o workspace e os serviços de geração existentes em vez de criar agentes expostos ou shell irrestrito.

### 4. Unificar a experiência Web, Android e iOS

O `clients/mx-app` será dividido em shell, navegação, conversa, projetos, workspace, mídia, conhecimento e estado do sistema. A linguagem **Ateliê de Inteligência** permanece: azul-ink, marfim, verde-mar MX, cobre pontual, tipografia editorial e composição assimétrica. O conteúdo visual do portfólio será tratado como regra de marca; a referência WebDev que falhou ao anexar não será inventada nem tomada como fonte de requisitos.

## Pontos que exigem validação antes da implementação

| Decisão | Motivo | Tratamento proposto |
|---|---|---|
| Ferramenta chamada “Mint” | O nome não identifica com segurança qual produto, runtime ou integração o usuário quer. | Confirmar o produto ou URL antes de habilitar qualquer conector ou instalar dependência. |
| Banco de dados nativo | A cópia de dados do PostgreSQL em contêiner é plataforma-específica. | Fazer exportação e restore; nunca apontar PostgreSQL Windows diretamente para `data/postgres`. |
| Acesso a pastas fora de `D:\MX` | Pode expor dados pessoais ou projetos não relacionados. | Exigir seleção explícita da raiz, registro por projeto e aprovação para escrita/execução. |
| Publicação de projetos | Pode criar efeitos externos ou disponibilizar dados. | Manter separada do preview e exigir aprovação explícita, destino e resumo de mudança. |

## Métricas de sucesso

A primeira versão direta será considerada pronta quando iniciar após reinício do Windows, responder healthchecks, restaurar uma conversa e um anexo, concluir uma mensagem pelo Core, gerar um documento PDF no Windows e iniciar uma prévia de projeto dentro de um workspace aprovado. A evolução de skills será medida pelos casos de avaliação versionados, incluindo recuperação, autorização, idempotência, resiliência, segurança, experiência móvel e acessibilidade.
