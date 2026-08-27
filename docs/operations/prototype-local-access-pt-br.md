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

O ambiente Docker está preservado somente como referência e reversão de protótipo. Ele não deve ser apresentado como produção, nem removido até que as validações pendentes sejam finalizadas. A futura centralização usará um launcher/painel local único, com healthchecks e ações reversíveis, mas sem iniciar comandos arbitrários ou publicar qualquer conteúdo.

## Controle central do protótipo

O script `scripts/mx-prototype-control.ps1` centraliza a observação e as ações reversíveis explicitamente permitidas. O modo padrão é somente leitura e apresenta, em JSON, o estado da UI, Core shadow, Core Docker de referência, Ollama, PostgreSQL, Forge e DSH.

```powershell
cd D:\MX
.\scripts\mx-prototype-control.ps1
.\scripts\mx-prototype-control.ps1 -Action OpenMx
.\scripts\mx-prototype-control.ps1 -Action StartShadow
.\scripts\mx-prototype-control.ps1 -Action StopShadow
```

`OpenMx` abre somente `http://127.0.0.1:8082`. `StartShadow` e `StopShadow` atuam exclusivamente sobre o processo que escuta a porta `18080`; o primeiro não reinicia um Core já saudável. Nenhuma dessas ações inicia, para, recria ou remove Docker, modifica PostgreSQL, acessa segredos, altera memória/anexos/workspaces ou publica conteúdo.

## Correção de login da interface

A interface em `8082` é atendida por Nginx. O cliente web agora força chamadas relativas para `/api`, usando o proxy do mesmo host até o Core e evitando uma URL de build `localhost:8080`, que falha quando o acesso ocorre por outra origem, dispositivo ou navegador. O fallback do Dockerfile também foi removido.

Falhas de autenticação passaram a retornar `401` com um corpo JSON controlado (`MX_INVALID_CREDENTIALS` e a mensagem genérica `Credenciais inválidas.`). Isso evita erro interno `500`, não revela se um usuário existe ou está inativo e permite ao cliente apresentar uma orientação útil sem expor detalhes internos. O Core e a UI foram reconstruídos sem recriar volumes ou serviços de dados; o teste por conta deliberadamente inexistente confirmou `401` e a mensagem segura através de `8082`.
