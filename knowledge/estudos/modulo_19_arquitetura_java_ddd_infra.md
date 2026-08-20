# Módulo 19 — Arquitetura Java, DDD, Clean Architecture e infraestrutura

## Fonte

Formação autenticada da Alura: https://cursos.alura.com.br/app/learning-guide/alura/arquitetura-java-design-codigo-infraestrutura

## Sequência

A formação possui 24 horas e três cursos. O primeiro bloco aborda Clean Architecture e Domain-Driven Design. O segundo aborda Docker, Docker Compose, ambientes de desenvolvimento/produção e deploy em AWS.

| Conceito | Aplicação no MX |
|---|---|
| DDD | Delimitar domínios, entidades, objetos de valor, agregados, serviços e eventos de domínio |
| Clean Architecture | Isolar domínio e casos de uso de frameworks, banco, mensageria e provedores de IA |
| Modularidade | Separar MX Core, memória, RAG, agentes, ferramentas, observabilidade e adapters |
| Docker | Reproduzir ambientes, empacotar dependências e reduzir diferença entre desenvolvimento e produção |
| AWS | Hospedar componentes quando houver necessidade de escala, disponibilidade ou integração gerenciada |

## Decisões arquiteturais

O núcleo deve depender de portas e contratos, enquanto adaptadores concretos implementam PostgreSQL, Redis, brokers, modelos, armazenamento e telemetria. A distribuição em microsserviços só será adotada quando houver fronteira de domínio, escala, isolamento ou ciclo de entrega que compense a complexidade.

Cada decisão será documentada em ADR contendo contexto, alternativas, decisão, consequências, riscos, métricas e plano de reversão. O desenho deve suportar execução local com o mesmo contrato usado em cloud.

## Operação

A infraestrutura deve possuir ambientes separados, configuração externa, secrets fora do código, healthchecks, readiness, logs estruturados, métricas, traces, backup, restauração testada e runbook de incidentes. Docker facilita a reprodução, mas não substitui testes de segurança, observabilidade nem governança de acesso.
