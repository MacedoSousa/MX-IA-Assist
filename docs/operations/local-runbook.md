# Runbook de operação local

## Objetivo

Este runbook descreve como iniciar, verificar, diagnosticar e parar o MX em uma máquina Windows de desenvolvimento. O ambiente é local e não deve ser exposto à internet sem uma camada adicional de autenticação, TLS, firewall e revisão de threat model.

## Pré-requisitos

| Dependência | Uso |
|---|---|
| Java 21 | Compilar e executar o backend. |
| Maven ou Maven Wrapper | Build e testes Java. |
| Docker Desktop + Compose v2 | PostgreSQL, Redis, Ollama, Open WebUI e MX Core. |
| Node.js + npm | Cliente Expo e exportação web. |
| Ollama | Modelo local conversacional. |
| Git | Versionamento e revisão. |

Confirme versões antes de investigar a aplicação:

```powershell
java -version
docker --version
docker compose version
node --version
npm --version
git --version
ollama --version
```

## Configuração

A configuração de desenvolvimento fica em `core-service/src/main/resources/application-dev.yml` e no Compose. Segredos devem ser fornecidos pelo ambiente ou por arquivo local ignorado. O repositório contém apenas `.env.example` com placeholders. `OLLAMA_TIMEOUT_MS` controla o timeout de cada requisição ao modelo e assume 120000 ms por padrão.

O cliente Expo usa:

```powershell
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
```

Para acessar pelo navegador do celular, substitua `localhost` pelo IPv4 privado da máquina Windows, por exemplo `http://192.168.0.10:8080`. O computador e o celular precisam estar na mesma rede privada, e o firewall do Windows deve permitir as portas escolhidas. Não exponha o serviço diretamente à internet.

Dentro do Compose, o backend usa o hostname do serviço Ollama definido no arquivo de composição. No cliente executado no host, `localhost` deve apontar para a porta publicada pelo backend; em um dispositivo físico, use o IP LAN da máquina.

## Iniciar

O caminho recomendado para desenvolvimento no Windows é executar `start.bat` na raiz do projeto. O script valida Docker, Java, Node.js, npm e PowerShell; sobe PostgreSQL, Redis, Ollama e Open WebUI pelo Compose; inicia o MX Core diretamente com o Maven Wrapper; aguarda o Actuator; e abre o Expo Web em uma nova janela usando o IP LAN detectado.

```powershell
cd D:\MX
.\start.bat
```

Ao final, o script informa os endereços local e LAN. No navegador do celular, conectado à mesma rede Wi-Fi, abra `http://<IP-LAN>:8081`. Se o Windows tiver mais de uma interface de rede, confirme o IP exibido e ajuste o firewall somente para a rede privada.

Para executar as etapas manualmente, inicie as dependências:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose up -d postgres redis ollama open-webui
```

Em seguida, execute o backend com o launcher equivalente ao usado pelo script:

```powershell
cd D:\MX
.\scripts\run-backend.bat
```

O cliente universal pode ser iniciado separadamente, apontando a API para o host ou para o IP LAN:

```powershell
cd D:\MX\clients\mx-app
npm install
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
npm run web
```

Para executar diretamente pela LAN, use `D:\MX\scripts\run-expo-web.bat <IP-LAN>`.

Verifique os serviços:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose ps
docker compose logs --tail=100 mx-ollama
```

## Health e smoke test

O Actuator expõe `GET /actuator/health` no perfil de desenvolvimento. O `start.bat` aguarda essa rota antes de iniciar o cliente; um status HTTP de resposta confirma que o processo está atendendo, mas a autenticação e o smoke test do contrato ainda precisam ser executados separadamente.

Depois de autenticar, faça um smoke test do contrato atual:

```powershell
# Login: use o payload definido pelo AuthController
curl.exe -i -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"usuario@example.com","password":"senha-local"}'

# Conversa síncrona: substitua TOKEN pelo access token retornado
curl.exe -i -X POST http://localhost:8080/api/v1/conversations/messages `
  -H "Authorization: Bearer TOKEN" `
  -H "Content-Type: application/json" `
  -d '{"prompt":"Responda com uma frase"}'
```

Não coloque tokens em histórico de terminal compartilhado. Em diagnóstico, redija o valor antes de anexar saída.

## Diagnóstico por sintoma

| Sintoma | Verificações iniciais | Ação segura |
|---|---|---|
| MX Core não inicia | `docker compose ps`, logs do container, Java e configuração | Corrigir causa; não apagar volumes automaticamente. |
| PostgreSQL recusando conexão | Health do container, porta 5432 e credenciais do perfil | Confirmar `.env` local e migrations; preservar `data/postgres`. |
| Ollama indisponível | `ollama list`, logs do serviço, URL configurada e modelo | Iniciar modelo correto; não marcar run como concluído manualmente. |
| Cliente recebe `401` | Expiração, sessão revogada e refresh | Limpar sessão somente após falha do refresh; não copiar tokens para tickets. |
| SSE interrompido | `runId`, `correlationId`, logs redigidos e estado do run | Consultar `GET /api/v1/runs/{runId}`; não reenviar tool automaticamente. |
| Tool rejeitada | Nome, policy outcome, workspace e motivo | Corrigir configuração ou solicitar aprovação explícita; não contornar a policy. |
| Porta ocupada | `Get-NetTCPConnection -LocalPort <porta>` | Parar somente o processo/serviço identificado ou alterar a porta documentada. |

## Logs e observabilidade

Use logs do serviço específico, limitando a quantidade:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose logs --tail=200 mx-core
docker compose logs --tail=100 ollama
```

O MX Core registra métricas Micrometer para o ciclo das skills. Os contadores atuais são `mx.skill.route{skill, outcome=selected}`, `mx.skill.execution{skill, outcome=completed}` e `mx.skill.execution{skill, outcome=failed}`. Consulte-as pelo Actuator:

```powershell
curl.exe -s http://localhost:8080/actuator/metrics/mx.skill.execution
curl.exe -s http://localhost:8080/actuator/metrics/mx.skill.route
```

Use `runId` e `correlationId` para correlacionar uma ocorrência. Não inclua prompt completo, resposta completa, access token, refresh token, senha, chave de API ou conteúdo privado em logs operacionais. Falhas de telemetria não devem interromper a execução principal. Métricas são indicadores operacionais: não devem ser apresentadas como cobertura, conformidade ou confiabilidade estatística sem período, denominador e método de medição.

Para medir latência do Ollama com amostras controladas e obter p50/p95, execute o benchmark sem registrar prompts ou respostas:

```powershell
cd D:\MX
.\scripts\benchmark-ollama.ps1 -Samples 20 -Warmup 2 -TimeoutSec 120 -OutputFile .\logs\ollama-benchmark.json
```

Compare p50, p95, taxa de falha e limite de timeout por modelo e por alteração de infraestrutura. Não compare resultados de máquinas ou modelos diferentes sem registrar hardware, versão do Ollama, modelo carregado e tamanho das amostras.

## Parar e preservar dados

Para parar os containers sem destruir volumes:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose stop
```

Para remover containers mantendo os diretórios `data/`:

```powershell
docker compose down
```

Não use `docker compose down -v`, `Remove-Item data`, comandos de prune ou scripts destrutivos como primeira tentativa de diagnóstico. A exclusão de dados exige backup validado e uma decisão explícita.

## Backup e restauração — estado planejado

O backup local deve incluir banco e configuração não secreta, com manifesto e checksum, sem copiar segredos em texto puro. A restauração deve ocorrer em diretório temporário, validar checksum e executar um smoke test antes de substituir o ambiente ativo.

Enquanto o fluxo automatizado não estiver implementado, preserve ao menos uma cópia segura dos diretórios de dados e documente a versão do Compose, migrations e modelos utilizados. O item MX-112 permanece aberto no backlog.

## Build reprodutível

O ambiente usado para as validações do backend separa build e cache Maven da árvore do projeto:

```powershell
cd D:\MX\core-service
.\mvnw.cmd -Dmx.build.directory=C:\Windows\Temp\mx-target `
  -Dmaven.repo.local=C:\Windows\Temp\mx-m2 test
```

O resultado de `target/`, a distribuição Maven local, arquivos zip, bundles Expo, logs e caches não devem ser adicionados ao Git.
