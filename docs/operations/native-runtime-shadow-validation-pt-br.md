# Validação do runtime nativo Windows — modo shadow

**Data:** 27 de agosto de 2026  
**Escopo:** validação paralela do MX Core executado diretamente no Windows, sem desligar ou recriar contêineres Docker.

## Resultado

O MX Core nativo iniciou com sucesso na porta `18080` depois da restauração do PostgreSQL e da concessão dos privilégios mínimos de schema, tabelas e sequências à conta de aplicação `mx`. O healthcheck retornou `UP`; o log confirma a conexão com `jdbc:postgresql://127.0.0.1:15432/mx`; e o launcher shadow força o Ollama nativo em `http://127.0.0.1:11435`.

| Fluxo validado | Evidência | Resultado |
|---|---|---|
| Banco restaurado | PostgreSQL 16, instância paralela `15432`, conta `mx` autenticada | Aprovado |
| Migrações | Flyway concluiu a inicialização do Core após grants de schema/objetos | Aprovado |
| Saúde do Core | `GET /actuator/health` na porta `18080` | `UP` |
| Conversa | Login autenticado e resposta de conversa pelo Core nativo | Aprovado |
| Anexos | Upload e processamento de um PDF real de validação anexado à conversa | Aprovado |
| Imagem | Geração autenticada pelo Core nativo com Forge temporário em `127.0.0.1:7860` | Aprovado |
| Vídeo | Geração de clipe MP4 curto com quadro Forge e FFmpeg nativo | Aprovado |
| Documento | Geração de PDF com fonte Arial no Windows e armazenamento como anexo | Aprovado |
| Pré-requisitos | Verificador detecta Ollama em LocalAppData, `pg_isready.exe` e PostgreSQL `15432` | Aprovado |
| Workspace governado | Bootstrap HTML estático e solicitação de preview somente após aprovação; servidor temporário apenas em `127.0.0.1:48000–48099` | Aprovado por testes e prova controlada |
| Runner de preview | Prova descartável respondeu em `127.0.0.1:48000` e foi encerrada pelo identificador de solicitação | Aprovado |
| Regressão do Core no Windows | Suíte Maven concluída usando saída temporária limpa, depois de sincronizar fixtures de vídeo e expectativas de auditoria | Aprovado |

## Implementação registrada

O script `initialize-mx-local-db.ps1` mantém o segredo somente no arquivo local ignorado pelo Git e aplica privilégios de menor alcance para o runtime: uso e criação no schema `public`, operações de dados nas tabelas existentes, uso de sequências e privilégios-padrão para objetos futuros criados pelo administrador da base. A aplicação não usa uma conta superusuária.

Os scripts `start-ollama-local.ps1`, `start-mx-local.ps1`, `run-backend.bat`, `verify-mx-local.ps1`, `local/start-mx-core-shadow.ps1` e `verify-mx-native-shadow.ps1` suportam explicitamente o caminho nativo. O modo padrão local usa `11435`, PostgreSQL `15432` e permite substituições explícitas para reversão. O launcher principal só habilita o endpoint de imagem quando `-WithImageGeneration` é informado.

> Nenhuma senha, token, dump de banco, anexo pessoal ou diretório de dados foi incluído no repositório durante a validação.

### Workspace governado e evidência de preview

As tools `workspace.initialize_static_project` e `workspace.preview_static` são operações `WRITE` registradas, com slug restrito, workspace isolado, ausência de links simbólicos e autonomia máxima `EXECUTE_WITH_APPROVAL`. A `DevelopmentSkill` apenas propõe as tools; o `ToolExecutor` persiste os argumentos em JSON e o caso de uso de aprovação reexecuta somente a tool originalmente registrada. O runner host-side aceita exclusivamente a receita declarativa `static-http`, revalida host loopback e a faixa de portas, e não aceita comandos arbitrários, Docker, npm ou publicação.

Os testes Python exercitam JSON inválido, traversal, host remoto, faixa de portas alterada e o ciclo iniciar/parar. A suíte do Core no Windows também foi executada em diretório temporário por existir uma ACL legada em `D:\MX\core-service\target` que impede a limpeza de alguns arquivos gerados pelo usuário não elevado. A correção não alterou essa ACL, nem fonte ou dados: o launcher shadow já usa uma pasta temporária independente, e os testes usaram a mesma estratégia de saída isolada.

O smoke autenticado da UI/API foi concluído em sessão temporária: a `workspace.initialize_static_project` e a `workspace.preview_static` foram propostas pela `DevelopmentSkill`, aprovadas por nonce e concluídas com status `COMPLETED`. O runner serviu o projeto de prova somente em loopback, confirmou HTTP `200` e o encerrou explicitamente. A limpeza posterior confirmou ausência de credencial, projeto de prova, estado associado e listener de preview.

Durante esse teste, a persistência PostgreSQL revelou que a run de aprovação reutilizava a `correlationId` da run de conversa, violando a restrição de unicidade. O `ToolExecutor` foi corrigido para gerar uma correlação independente para cada run de aprovação; a regressão automatizada e o smoke final validaram o comportamento. Credenciais e tokens continuaram apenas em memória e foram removidos ao término.

## Limites antes do corte final

O Docker permanece o caminho de produção e reversão até que os itens abaixo sejam concluídos. O Forge está funcionando apenas como componente transitório no contêiner da porta `7860`; portanto, o runtime ainda não é integralmente livre de Docker para imagens. O FFmpeg 9 foi instalado pelo gerenciador Windows e o vídeo curto foi gerado com sucesso pelo pipeline nativo. O Whisper local foi instalado para uso pelo launcher, mas ainda exige uma validação com gravação de fala real antes de ser promovido para a operação.

Também permanecem pendentes a política de inicialização persistente do Core/Expo, a revisão da exposição de rede do PostgreSQL nativo, a definição de Redis no runtime direto, os testes de Expo em rede local/Tailscale e o plano de corte com retorno documentado. O Ollama nativo já possui uma tarefa de inicialização reversível no logon. O DSH continua isolado em `127.0.0.1:3080` e não é alterado por esta etapa.

### Observação de aceleração de áudio

O adaptador NVIDIA GeForce RTX 2060 SUPER foi reconhecido pelo Windows, mas `nvidia-smi` retornou `Failed to initialize NVML: Unknown Error`. A verificação versionada `verify-whisper-runtime.py` confirmou PyTorch `2.13.0+cpu` e CUDA indisponível. Por isso, a transcrição permanece funcional como fallback de CPU e não foi feita uma instalação CUDA especulativa. A atualização ou correção do driver NVIDIA, seguida da verificação NVML e da instalação oficial de PyTorch CUDA compatível, é pré-requisito para ativar GPU no Whisper.

## Reprodução segura

1. Mantenha os contêineres atuais ativos enquanto a validação ocorre.
2. Confirme os pré-requisitos com `scripts/verify-mx-local.ps1 -OllamaUrl http://127.0.0.1:11435 -PostgresPort 15432`.
3. Para validar o Core isoladamente, execute `scripts/local/start-mx-core-shadow.ps1 -WithVideoGeneration` e consulte `http://127.0.0.1:18080/actuator/health`.
4. Use `scripts/verify-mx-native-shadow.ps1` com credenciais passadas em parâmetros locais para exercitar login, conversa, PDF, anexo, imagem e vídeo. Não registre senhas em arquivos nem no histórico de comandos compartilhado.

O corte para a porta de produção somente deve ocorrer depois que todos os limites desta seção tiverem evidência de aceite.
