# Currículo ampliado priorizado — MX e assistente pessoal local

## Objetivo

Ampliar o estudo AI Engineer para formar uma base completa de produto digital, dados, engenharia de software, mobile, experiência do usuário, operação e gestão. A ordem abaixo privilegia dependências técnicas e aplicação direta no MX.

## Fase A — Fundamentos de dados e software

A primeira etapa cobre SQL, PostgreSQL, modelagem relacional, normalização, álgebra relacional, transações, índices, backup e administração. Em paralelo, entram testes unitários, integração, contratos, qualidade, cobertura e CI. O resultado esperado é um núcleo confiável, versionado e verificável.

## Fase B — Arquitetura e operação

A segunda etapa aborda DDD, Clean Architecture, modularização, microsserviços, Docker, redes, Nginx, Terraform, Kubernetes, segurança, secrets, observabilidade e recuperação. A regra será preferir modularidade dentro do MX antes de distribuir componentes sem necessidade operacional.

## Fase C — Interfaces e experiência

A terceira etapa cobre HTML/CSS/JavaScript, framework frontend, contratos de API, estados de carregamento e erro, acessibilidade WCAG, UX research, arquitetura da informação, wireframes, design system, Figma, responsividade, mobile first e prototipação. Cada fluxo será validado por objetivo, usuário, hipótese e critério de sucesso.

## Fase D — Mobile

A quarta etapa compara Android nativo, iOS com Swift/SwiftUI e Flutter. Serão estudados ciclo de vida, navegação, estado, offline-first, sincronização, notificações, segurança local, testes, CI/CD, distribuição e integração com APIs do MX. A escolha da tecnologia será feita por desempenho, acesso a recursos nativos, manutenção, acessibilidade e custo total.

## Fase E — Gestão e finanças técnicas

A quinta etapa cobre Kanban, Scrum, fluxo, WIP, lead time, throughput, bloqueios, cadência, governança, métricas, portfólio, riscos, custos de cloud, custo por inferência, capacidade, orçamento e retorno. Cálculos devem registrar fórmula, premissas, unidade, fonte dos dados, intervalo e sensibilidade; não serão usados para aconselhamento financeiro pessoal.

## Fase F — Integração ao MX

O MX Core permanece responsável por identidade, autorização, domínio, sessões e transações. Serviços de IA e dados são adaptadores com contratos versionados. Frontend e mobile consomem APIs e eventos sem armazenar credenciais de provedores. Retrieval, agentes, mensageria, pipelines e telemetria devem possuir `trace_id`, política de retenção e casos de avaliação.

## Matriz de prioridade

| Prioridade | Tema | Motivo | Entrega esperada |
|---|---|---|---|
| P0 | PostgreSQL, modelagem, testes e CI | Base imediata do MX | Schema, migrations, testes e pipeline |
| P0 | Arquitetura Java e observabilidade | Reduz risco estrutural e operacional | Módulos, contratos, logs, métricas e traces |
| P1 | RAG, agentes, mensageria e MLOps | Capacidades centrais do assistente | Skills, workers, avaliação e rollback |
| P1 | Frontend, UX/UI e acessibilidade | Torna a solução utilizável e inclusiva | Fluxos, componentes e critérios de usabilidade |
| P1 | Flutter, Android e iOS | Expande canais de acesso | Cliente mobile, offline, testes e CI/CD |
| P2 | Kubernetes, Terraform e multi-cloud | Escala e portabilidade operacional | IaC, ambientes e runbooks |
| P2 | Kanban, governança e finanças técnicas | Sustenta execução e decisão | Métricas de fluxo, custos e capacidade |

## Regra de estudo

Cada curso será convertido em uma ficha com conceitos, exemplos, pré-requisitos, aplicação no MX, riscos, testes, skill reutilizável e referência de origem. Conteúdo de curso será tratado como material de estudo; não será transformado automaticamente em dataset de treinamento.


## Módulos já consolidados

| Módulo | Área | Artefato |
|---|---|---|
| 17 | PostgreSQL, SQL, PL/pgSQL e DBA | `modulo_17_postgresql_sql_plpgsql_dba.md` |
| 18 | Bancos, cálculos e decisões de persistência | `modulo_18_bancos_calculos_e_decisoes.md` |
| 19 | Arquitetura Java, DDD, Clean Architecture e infraestrutura | `modulo_19_arquitetura_java_ddd_infra.md` |
| 20 | Testes, qualidade, CI/CD e rollback | `modulo_20_testes_qualidade_ci.md` |
| 21 | Frontend React, TypeScript e Design System | `modulo_21_frontend_react_typescript_design_system.md` |
| 22 | Flutter, Android, iOS, React Native e CI/CD mobile | `modulo_22_mobile_flutter_android_ios.md` |
| 23 | UX/UI, pesquisa, prototipação e acessibilidade | `modulo_23_ux_ui_pesquisa_acessibilidade.md` |
| 24 | Kanban, governança, administração, finanças e custos | `modulo_24_kanban_governanca_financas.md` |

## Skills criadas

`skill-persistencia-e-capacidade`, `skill-qualidade-e-release`, `skill-interface-acessivel`, `skill-mobile-offline-first` e `skill-fluxo-kanban-custos`.

## Dependências de estudo

A sequência recomendada é: dados e cálculos; qualidade e arquitetura; interfaces e mobile; UX e acessibilidade; gestão e custos; integração das skills; implementação incremental no MX. Cada etapa deve preservar contratos, testes, observabilidade e rollback.
