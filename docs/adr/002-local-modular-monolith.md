# ADR-002 — Monólito modular local antes de distribuição

- **Status:** accepted
- **Data:** 2026-08-20
- **Decisores:** maintainers do MX

## Contexto

O MX roda na máquina do usuário e precisa de baixa latência, depuração simples, consistência de estado e evolução rápida. Microserviços adicionariam rede, deployment, observabilidade e recuperação distribuída antes de existir uma necessidade comprovada.

## Decisão

O backend será um monólito modular Spring Boot, organizado por feature e protegido por Clean Architecture. PostgreSQL será a fonte persistente principal; Ollama será acessado por porta de gateway; tools e skills permanecerão em módulos internos com contratos explícitos.

## Alternativas consideradas

Microserviços por skill foram rejeitados na fundação por aumentarem a superfície operacional e dificultarem transações, testes e execução local. Um script único sem fronteiras foi rejeitado por acoplar domínio a framework, banco e modelo.

## Consequências

A operação local é mais simples e o ciclo TDD é mais rápido. O código precisa respeitar dependências para dentro e manter adapters substituíveis. A extração futura só será considerada quando houver necessidade de runtime diferente, isolamento forte, escala ou ciclo de implantação independente.

## Validação

Testar casos de uso sem Spring, JPA, Docker ou Ollama; executar adapters em testes de integração; verificar que controllers são finos; e confirmar que o Compose inicia as dependências sem exigir serviços cloud.
