# Módulo 26 — Servidores Linux/Windows, DNS, rotas e redes

## Fontes Alura

[Busca autenticada por Linux, Windows, DNS, rotas e firewall](https://cursos.alura.com.br/app/search?query=Linux+Windows+servidores+DNS+rotas+redes+firewall+administra%C3%A7%C3%A3o)

| Curso | Conteúdo aplicado |
|---|---|
| [Redes: implementando roteamento, DNS e IPv6](https://cursos.alura.com.br/course/redes-implementando-roteamento-dns-ipv6) | Roteamento, DNS, IPv6 e diagnóstico |
| [Sistema Operacional Linux](https://cursos.alura.com.br/course/linux-operacoes-fundamentos-comandos) | Terminal, arquivos, permissões, APT, shell, Cron, monitoramento e segurança |
| [Linux para cibersegurança](https://cursos.alura.com.br/course/linux-ciberseguranca) | Administração, shell scripting e ferramentas de segurança |
| [Segurança de rede: proxy reverso, SSH e DNS](https://cursos.alura.com.br/course/seguranca-rede-proxy-reverso-ssh-dns) | Proxy reverso, SSH, DNS e iptables |
| [Segurança de rede: firewall, WAF e SIEM](https://cursos.alura.com.br/course/seguranca-rede-firewall-waf-siem) | Firewall, WAF, SIEM, pfSense e Graylog |
| [Formação Redes de computadores](https://cursos.alura.com.br/formacao-redes) | Fundamentos de redes e configuração |
| [WSL](https://cursos.alura.com.br/extra/alura-mais/windows-subsystem-for-linux-wsl--c238) | Integração prática entre Windows e Linux |

## Modelo operacional

Linux será a base preferencial para containers, workers, brokers e automação; Windows será tratado como ambiente relevante para Active Directory, serviços corporativos, PowerShell, IIS, integração legada e execução de aplicações específicas. WSL é uma ponte de desenvolvimento, não substitui automaticamente uma política de produção.

DNS deve ser analisado por zona, autoridade, resolução recursiva, TTL, registros, split-horizon e segurança. Rotas devem registrar origem, destino, gateway, interface, métrica, estado e regra de retorno. Diagnóstico deve separar camada de aplicação, resolução, transporte, roteamento, firewall, proxy e serviço.

## Operação segura

A administração exige contas individuais, menor privilégio, SSH com chaves, MFA quando disponível, atualização, backup, centralização de logs, sincronização de tempo, inventário, mudança aprovada e procedimento de recuperação. Firewall e WAF não substituem autenticação, autorização, validação de entrada ou segmentação.

## Aplicação no MX

O runbook do MX deve conter comandos de diagnóstico equivalentes para Linux e Windows, sem executar alterações destrutivas automaticamente. O assistente pode explicar `ip`, `ss`, `dig`, `nslookup`, `traceroute`, `Test-NetConnection`, `Get-DnsClientServerAddress`, `Get-NetRoute`, `journalctl`, PowerShell e logs do serviço, sempre distinguindo observação de ação.
