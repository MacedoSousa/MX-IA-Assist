# Arquitetura de Execução Direta — Windows

## Objetivo

Executar o MX diretamente no computador Windows, sem depender de Docker para os serviços MX, preservando os dados, a inicialização automática e a separação entre planejamento por IA, validação determinística e aprovação humana.

## Topologia alvo

```text
MX Web / Expo (porta 8081)
        │
        ▼
MX Core / Spring Boot (porta 8080)
   ├── PostgreSQL nativo (porta 5432)
   ├── Ollama nativo (porta 11434)
   ├── Forge nativo (porta 7860, quando habilitado)
   ├── Whisper local (comando configurável, quando habilitado)
   └── workspace registrado em D:\MX\workspaces
         └── executor host com operações declaradas
```

O DSH continua isolado em `127.0.0.1` e será iniciado pelo launcher nativo somente quando o fluxo de agentes que o utiliza estiver habilitado. PostgreSQL, Ollama e Forge serão verificados por healthcheck antes de iniciar o Core. Redis não participa de chamadas de negócio no código Java atualmente auditado; no primeiro perfil direto ele não será requisito de prontidão, evitando manter um serviço nativo ocioso.

## Migração em quatro pontos de restauração

| Etapa | Ação | Critério de continuidade | Retorno seguro |
|---|---|---|---|
| 1. Preservação | Dump lógico PostgreSQL, hashes dos anexos/conhecimento e inventário de modelos. | Backup e manifesto verificados. | O Compose continua intacto. |
| 2. Pré-requisitos | Instalar ou validar Java 21, Node 22, PostgreSQL, Ollama, Forge e dependências opcionais. | Todos os executáveis e endpoints requeridos respondem. | Nenhum contêiner é parado. |
| 3. Sombra | Restaurar dados em PostgreSQL nativo e iniciar Core/Expo com portas dedicadas de teste. | Login, conversa, anexo e healthcheck aprovados. | Encerrar somente os processos nativos de teste. |
| 4. Corte | Tornar o launcher direto a inicialização padrão e parar exclusivamente o stack MX em contêiner. | Healthchecks e smoke tests aprovados. | Religar o Compose, mantendo dados e backup. |

Não é seguro apontar uma instalação PostgreSQL nativa diretamente para os arquivos Linux existentes em `data/postgres`. O movimento obrigatório é **dump lógico, restore e validação**.

## Contrato do workspace local

Um projeto é uma raiz local registrada, com identificação, proprietário, política de escrita, comandos autorizados e artefatos de prévia. O navegador não escolhe livremente qualquer pasta do computador e o modelo não recebe um shell arbitrário.

| Operação | Autorização | Saída visível no WebUI |
|---|---|---|
| Listar e ler arquivos permitidos | Automática, dentro da raiz registrada. | Árvore de arquivos, trecho truncado e origem. |
| Propor criação ou alteração | Permitida como plano, sem escrita. | Diff, arquivos afetados, justificativa e validações sugeridas. |
| Gravar arquivo | Aprovação explícita. | Resultado, hash e caminho relativo. |
| Rodar validação conhecida | Aprovada por política conforme o efeito. | Comando identificado, duração, saída limitada e status. |
| Iniciar preview | Aprovada dentro de uma receita de projeto registrada. | URL local, porta, processo e botão para encerrar. |
| Publicar, enviar para remoto ou alterar ambiente | Sempre exige confirmação explícita. | Destino, resumo de mudança e evidência de execução. |

O executor existente `mx_evolution_runner.py` continua sendo a fronteira do host. A evolução acrescentará receitas de preview e validação à sua allowlist; não aceitará strings de shell geradas pela IA.

## Perfil de configuração

O perfil `local-windows` deve manter configurações fora do código: URLs locais, diretórios de logs e anexos, fonte PDF, diretório de trabalho, URLs de Forge, comando Whisper e variáveis de CORS. Segredos e credenciais de banco ficam em arquivo local ignorado pelo Git ou nas variáveis do Windows, nunca em um job de evolução nem em uma resposta de modelo.

## Experiência de produto

O WebUI apresentará cinco espaços conectados: Conversas, Projetos, Workspace, Conhecimento e Mídia. O card de prévia exibirá somente processos iniciados por receitas registradas. Ações que ainda não têm API não serão simuladas: deverão estar desabilitadas com explicação de pré-requisito ou não aparecer até que o contrato esteja implementado.

## Integrações de notas e ferramentas

Um vault Obsidian pode ser tratado inicialmente como uma raiz Markdown registrada, sem integração externa: leitura indexada, recuperação documental e exportação de nota mediante aprovação. A ferramenta mencionada como “Mint” permanece pendente de identificação precisa, pois o nome pode indicar produtos diferentes e não deve ser instalado ou conectado por suposição.
