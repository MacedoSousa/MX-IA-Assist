# Módulo 27 — Criptografia e segurança operacional

## Fontes Alura

[Busca autenticada por criptografia, chaves, hashes e certificados](https://cursos.alura.com.br/app/search?query=criptografia+chaves+hash+certificados+TLS+seguran%C3%A7a+aplicada)

| Conteúdo | Aplicação |
|---|---|
| [Criptografia: fundamentos, algoritmos e tecnologias emergentes](https://cursos.alura.com.br/course/criptografia-fundamentos-algoritmos-tecnologias-emergentes) | Princípios, algoritmos, ameaças e tecnologias |
| [Node.js: criptografia e tokens JWT](https://cursos.alura.com.br/course/nodejs-criptografia-tokens-jwt) | Hashing, tokens, autenticação e integração de APIs |
| [Governança em segurança da informação](https://cursos.alura.com.br/formacao-governanca-seguranca-informacao) | Risco, controles, LGPD e governança |
| [Cloud Security](https://cursos.alura.com.br/career/path/cloud-security) | Identidade, redes, dados e workloads em cloud |
| [Segurança em Pipelines](https://cursos.alura.com.br/course/seguranca-pipelines-integrando-praticas-seguranca-ci-cd) | SAST, SCA, DAST e segurança de entrega |

## Princípios para o MX

Senhas não devem ser criptografadas para posterior recuperação; devem ser armazenadas com função de derivação adequada, salt único e parâmetros atualizados. Dados em trânsito exigem TLS validado; dados em repouso exigem gestão de chaves, rotação, controle de acesso, backup e auditoria. Hash é usado para integridade ou derivação conforme o algoritmo; não substitui criptografia quando o conteúdo precisa ser recuperado.

JWT deve ser validado por algoritmo permitido, emissor, audiência, expiração, nonce quando aplicável e escopo mínimo. O token não deve conter segredo nem autorização implícita sem verificação no servidor. Revogação, rotação e exposição em logs precisam ser tratados explicitamente.

## Análise crítica estrutural

A segurança não pode ser adicionada apenas no endpoint. Ela atravessa identidade, autorização, rede, sistema operacional, dependências, pipeline, imagem, secrets, banco, logs, backups, clientes móveis e procedimentos humanos. Uma arquitetura que possui criptografia, mas não possui rotação de chaves, menor privilégio, recuperação testada ou telemetria redigida, permanece estruturalmente frágil.

## Controles mínimos

| Domínio | Controle esperado |
|---|---|
| Identidade | MFA, menor privilégio, sessões curtas e revisão de acesso |
| Chaves | Cofre, rotação, segregação, auditoria e plano de comprometimento |
| Transporte | TLS, validação de certificados e desativação de protocolos inseguros |
| Aplicação | Validação, autorização por recurso, proteção contra replay e rate limit |
| Pipeline | SAST, SCA, DAST, assinatura/proveniência e bloqueio por severidade |
| Dados | Classificação, retenção, criptografia, backup e restauração testada |
| Operação | Logs redigidos, alertas, resposta a incidentes e runbooks |
