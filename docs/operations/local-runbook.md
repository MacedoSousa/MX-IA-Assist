# Runbook de operação local

## Objetivo

Este runbook descreve como iniciar, verificar, diagnosticar e parar o MX em uma máquina Windows de desenvolvimento. O ambiente é local e não deve ser exposto à internet sem uma camada adicional de autenticação, TLS, firewall e revisão de threat model.

## Pré-requisitos

| Dependência | Uso |
|---|---|
| Docker Desktop + Compose | PostgreSQL, Redis, Ollama, Open WebUI, MX Core e mx-web. |
| PowerShell | Launcher Windows e diagnóstico operacional. |
| Java 21 + Maven Wrapper | Opcionais para build/testes do backend fora do Docker. |
| Node.js + npm | Opcionais para desenvolvimento direto do cliente Expo. |
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

A configuração de desenvolvimento fica em `core-service/src/main/resources/application-dev.yml` e no Compose. Segredos devem ser fornecidos pelo ambiente ou por arquivo local ignorado. O repositório contém apenas `.env.example` com placeholders. `OLLAMA_TIMEOUT_MS` controla o timeout de cada requisição ao modelo e assume 120000 ms por padrão; `OLLAMA_MODEL` permite substituir o modelo local e assume `qwen3:8b`, que é o modelo validado no stack atual.

Quando o `start.bat` é usado, o bundle Expo é construído com `EXPO_PUBLIC_MX_API_URL=http://<IP-LAN>:8080`, e o CORS é configurado para a origem web correspondente. Para desenvolvimento direto do cliente no host, use:

```powershell
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
```

Para acessar pelo navegador do celular, o computador e o celular precisam estar na mesma rede privada, o firewall do Windows deve permitir as portas escolhidas e o serviço web deve ser aberto em `http://<IP-LAN>:8082`. Não exponha o serviço diretamente à internet.

Dentro do Compose, o backend usa o hostname do serviço Ollama definido no arquivo de composição. No cliente executado no host, `localhost` deve apontar para a porta publicada pelo backend; em um dispositivo físico, use o IP LAN da máquina.

## Iniciar

O caminho recomendado para desenvolvimento no Windows é executar `start.bat` na raiz do projeto. O script valida Docker e PowerShell; constrói e sobe todo o stack pelo Compose, incluindo PostgreSQL, Redis, Ollama, Open WebUI, MX Core e mx-web; aguarda os healthchecks; injeta a URL da API e a origem CORS da LAN no bundle Expo; e informa os endereços local e LAN. Java, Maven, Node.js e Ollama instalados no host não são necessários para esse fluxo Docker-only.

```powershell
cd D:\MX
.\start.bat
```

Ao final, o script informa os endereços local e LAN. No navegador do celular, conectado à mesma rede Wi-Fi, abra `http://<IP-LAN>:8082`. Se o Windows tiver mais de uma interface de rede, confirme o IP exibido e ajuste o firewall somente para a rede privada. A porta pode ser alterada com `MX_WEB_PORT` no ambiente do Compose; o launcher padrão usa 8082 porque 8081 já pode estar ocupada por outro serviço local.

Para executar o stack manualmente, use:

```powershell
cd D:\MX\infrastructure\docker\compose
$env:MX_WEB_PORT = "8082"
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
docker compose up -d --build
```

O cliente universal também pode ser executado diretamente no host, apontando a API para o host ou para o IP LAN:

```powershell
cd D:\MX\clients\mx-app
npm install
$env:EXPO_PUBLIC_MX_API_URL = "http://localhost:8080"
npm run web
```

Para executar diretamente pela LAN fora do Docker, use `D:\MX\scripts\run-expo-web.bat <IP-LAN>`; nesse caso, a porta depende do Expo e não deve ser confundida com o mx-web Docker publicado em 8082.

Verifique os serviços:

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose ps
docker compose logs --tail=100 mx-core
docker compose logs --tail=100 mx-ollama
```

## Health e smoke test

O Actuator expõe `GET /actuator/health` no perfil de desenvolvimento, e o Compose usa essa rota pública no healthcheck do `mx-core`. O cliente Nginx expõe `GET http://localhost:8082/health`. O `start.bat` aguarda ambas as rotas antes de concluir; a autenticação e o smoke test do contrato ainda precisam ser executados separadamente.

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

### Tools internas e aprovação humana

A comunicação externa continua centralizada no MX Core. A DevelopmentSkill pode solicitar apenas uma chamada estruturada no protocolo `[MX_TOOL_CALL]...[/MX_TOOL_CALL]`; texto livre, JSON sem marcador, nome fora da allowlist ou argumento ausente são rejeitados. O ToolExecutor aplica a policy independentemente da instrução do modelo. Tools de leitura autorizadas podem ser executadas diretamente quando a autonomia declarada permitir.

Operações sensíveis nunca são executadas automaticamente. Quando uma tool exigir intervenção humana, a resposta HTTP do chat retorna `status=AWAITING_APPROVAL`, `approvalRunId`, `approvalNonce` e `approvalExpiresAt`; o nonce é usado somente no endpoint de aprovação autenticado e expira conforme a configuração do ambiente. No SSE, o evento final é `approval_required` em vez de `completed`. O cliente deve sincronizar os runs e usar o painel de aprovação, sem tentar contornar a policy ou reenviar a tool por conta própria.

Após a aprovação, o estado do run é retomado pelo caso de uso de aprovação. A reexecução automática da operação original ainda é uma evolução separada do backlog; até ela ser implementada, a aprovação representa a autorização registrada e não deve ser interpretada como execução concluída.

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
curl.exe -s http://localhost:8082/health
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
