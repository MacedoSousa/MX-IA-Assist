# Acesso local central do protótipo MX

## Regra de uso

Todo o ambiente MX é um **protótipo local**. Nenhuma porta, contêiner, script ou serviço está caracterizado como produção nesta etapa. As portas técnicas não devem ser usadas como interface principal.

| Endereço | Papel | Como utilizar |
|---|---|---|
| `http://127.0.0.1:8082` | **Interface visual do MX** | Ponto de entrada recomendado para testar o produto. |
| `http://127.0.0.1:18080/actuator/health` | Healthcheck do Core nativo shadow | Diagnóstico técnico; deve responder `200`/`UP`. |
| `http://127.0.0.1:18080/` | Raiz do Core nativo shadow | Não é uma UI; uma resposta `403`/Whitelabel é esperada pela segurança da API. |
| `http://127.0.0.1:8080/` | Core do caminho Docker de referência | Também é uma API técnica, não uma página de produto. |
| `http://127.0.0.1:3000` | Open WebUI auxiliar | Interface independente de experimentação; não substitui a UI do MX. |
| `http://127.0.0.1:3080` | DSH | Serviço de desenvolvimento restrito a loopback. |

> Abra o MX em `http://127.0.0.1:8082`. Não abra a raiz da API `18080` esperando uma tela de login ou chat.

## Estado e limites

O Core shadow nativo em `18080` é o ambiente de validação integrado ao PostgreSQL e Ollama nativos. A API não possui rota pública raiz e continua protegida por autenticação, por isso o navegador mostra uma página padrão de erro ao acessar `/`. Isso não representa queda do serviço quando `/actuator/health` responde `UP`.

O caminho primário de uso do protótipo é a **UI em `8082` conectada ao Core Docker de referência em `8080`**, com persistência no PostgreSQL Docker preservado. O Core nativo em `18080` permanece exclusivamente como shadow para validação. Isso evita misturar os dois bancos durante a migração. O ambiente Docker não deve ser apresentado como produção nem removido até que as validações pendentes sejam finalizadas.

## Controle central do protótipo

O script `scripts/mx-prototype-control.ps1` centraliza a observação e as ações reversíveis explicitamente permitidas. O modo padrão é somente leitura e apresenta, em JSON, o estado da UI, Core shadow, Core Docker de referência, Ollama, PostgreSQL, Forge e DSH.

```powershell
cd D:\MX
.\scripts\mx-prototype-control.ps1
.\scripts\mx-prototype-control.ps1 -Action OpenMx
.\scripts\mx-prototype-control.ps1 -Action OpenGuestConsole
.\scripts\mx-prototype-control.ps1 -Action StartShadow
.\scripts\mx-prototype-control.ps1 -Action StopShadow
```

`OpenMx` abre somente `http://127.0.0.1:8082`, que é a rota primária do protótipo. `OpenGuestConsole` abre uma conversa local efêmera com `qwen3:8b`, sem cadastro e sem acesso ao Core, dados, anexos, RAG ou tools. `StartShadow` e `StopShadow` atuam exclusivamente sobre o processo que escuta a porta `18080`; o primeiro não reinicia um Core já saudável. Nenhuma dessas ações inicia, para, recria ou remove Docker, modifica PostgreSQL, acessa segredos, altera memória/anexos/workspaces ou publica conteúdo.

## Correção de login da interface

A interface em `8082` é atendida por Nginx. O cliente web agora força chamadas relativas para `/api`, usando o proxy do mesmo host até o Core e evitando uma URL de build `localhost:8080`, que falha quando o acesso ocorre por outra origem, dispositivo ou navegador. O fallback do Dockerfile também foi removido.

Falhas de autenticação passaram a retornar `401` com um corpo JSON controlado (`MX_INVALID_CREDENTIALS` e a mensagem genérica `Credenciais inválidas.`). Isso evita erro interno `500`, não revela se um usuário existe ou está inativo e permite ao cliente apresentar uma orientação útil sem expor detalhes internos. O Core e a UI foram reconstruídos sem recriar volumes ou serviços de dados; o teste por conta deliberadamente inexistente confirmou `401` e a mensagem segura através de `8082`.

Na reprodução pelo navegador, a tentativa real ainda chegava ao proxy como `POST /api/auth/login`, mas recebia `403`. A causa estava no perfil `application-dev.yml`: ele sobrescrevia a allowlist padrão de CORS e não incluía `http://127.0.0.1:8082`. Como Nginx encaminha o cabeçalho `Origin` até o Core, o filtro de CORS o recusava. A origem loopback exata foi adicionada à allowlist, sem coringa de rede. Após a reconstrução isolada do Core, o preflight passou a retornar `Access-Control-Allow-Origin: http://127.0.0.1:8082` e o healthcheck permaneceu em `200`.

Também foram verificados, sem expor identificadores, hashes ou senhas, o Core e PostgreSQL Docker saudáveis, a presença de contas ativas e a compatibilidade Argon2 da conta local esperada. Não foi detectado bloqueio de banco, schema ou permissão no caminho Docker; a confirmação de uma sessão real continua dependendo da tentativa pela interface atualizada.
