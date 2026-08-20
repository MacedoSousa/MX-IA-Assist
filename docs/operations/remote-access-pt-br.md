# Acesso remoto seguro do MX

**Estado:** configurado e validado em 20 de agosto de 2026.

## Resultado aplicado

O computador Windows do MX foi conectado ao tailnet do usuário com o hostname interno `mx-ai.taila61bd3.ts.net`. O Tailscale Serve foi habilitado somente para o tailnet e está encaminhando HTTPS para o cliente web local:

```text
https://mx-ai.taila61bd3.ts.net/
    -> http://127.0.0.1:8082
```

O endpoint respondeu `HTTP 200` no próprio computador, com conteúdo HTML do cliente Expo servido pelo Nginx. O Tailscale informou explicitamente `tailnet only`, portanto o endpoint não é público para toda a Internet.

O hostname gratuito solicitado pelo usuário, `mx-ai.duckdns.org`, resolve atualmente para um registro IPv4 público. Ele foi mantido como DNS de referência, mas **não foi usado como endpoint público do MX**, porque um registro DuckDNS sozinho não fornece túnel privado, autenticação adicional ou proteção contra exposição direta de portas. Assim, a URL segura para uso remoto é a URL HTTPS do Tailscale.

## Como acessar pelo celular

Instale o aplicativo Tailscale no Android ou iOS, entre com a mesma conta usada no computador e mantenha o Tailscale conectado. Em seguida, abra no navegador do celular:

```text
https://mx-ai.taila61bd3.ts.net/
```

O celular precisa estar autorizado no mesmo tailnet. Se o dispositivo aparecer como pendente no painel do Tailscale, autorize-o antes do primeiro acesso.

## Modelo de segurança

O MX não publica diretamente as portas 8080, 8081, 8082, 3000, 5432, 6379 ou 11434. PostgreSQL, Redis, Ollama, Open WebUI e servidores de jogos continuam fora do endpoint remoto. O Tailscale Serve cria HTTPS dentro do tailnet e faz o encaminhamento para `127.0.0.1:8082`; nenhum port forwarding do roteador é necessário nesse modelo [1] [2].

> O Tailscale documenta que o Serve disponibiliza conteúdo de um nó para o próprio tailnet via HTTPS, enquanto o Funnel é o recurso separado para disponibilizar um serviço à Internet [1].

O Funnel foi deliberadamente deixado desativado. Isso impede que uma alteração acidental transforme o MX em um serviço público sem autenticação de rede. A autenticação própria do MX continua sendo exigida pelo sistema, e o controle de pertencimento ao tailnet funciona como uma segunda camada de acesso.

## Comandos de verificação

No Windows, execute:

```powershell
$ts = 'C:\Program Files\Tailscale\tailscale.exe'
& $ts status
& $ts serve status
Invoke-WebRequest https://mx-ai.taila61bd3.ts.net/ -UseBasicParsing
```

O resultado esperado do Serve é semelhante a:

```text
https://mx-ai.taila61bd3.ts.net (tailnet only)
|-- / proxy http://127.0.0.1:8082
```

O resultado esperado da requisição HTTPS é `HTTP 200`.

## DuckDNS e limitações importantes

`mx-ai.duckdns.org` pode continuar sendo usado para acompanhar o endereço público residencial, caso um atualizador DDNS seja configurado com o token do usuário. Entretanto, apontar o hostname para o computador e abrir uma porta no roteador mudaria o modelo de ameaça. Essa opção não foi ativada.

Para que o hostname DuckDNS seja a URL pública do MX mantendo uma camada de proxy, seria necessário usar um serviço de túnel que aceite domínio externo ou registrar um domínio próprio em um provedor compatível. Com os recursos gratuitos atuais, o desenho mais seguro e imediatamente utilizável é o hostname privado do Tailscale.

## Operação e reinício

O cliente Tailscale é instalado como serviço do Windows e o MX continua sendo iniciado pelo launcher Always-On existente. O Serve fica associado à configuração do nó; a verificação deve ser executada após reinicializações ou atualizações do Tailscale. Nenhuma configuração remota deve incluir tokens no Git.

## Referências

[1]: https://tailscale.com/docs/reference/tailscale-cli/serve "Tailscale Serve CLI"
[2]: https://tailscale.com/docs/features/magicdns "Tailscale MagicDNS"
[3]: https://www.duckdns.org/ "Duck DNS"
