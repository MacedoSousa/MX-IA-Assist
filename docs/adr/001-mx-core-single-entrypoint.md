# ADR-001 — MX Core como único ponto de comunicação

- **Status:** accepted
- **Data:** 2026-08-20
- **Decisores:** maintainers do MX

## Contexto

O produto precisa apoiar programação, assuntos pessoais e consultas gerais sem obrigar o usuário a escolher manualmente um agent. Ao mesmo tempo, as capacidades precisam ser especializadas, testáveis e limitadas por política.

## Decisão

O usuário conversa somente com o **MX Core**. Skills são registradas, roteadas e executadas internamente. Controllers e clientes não chamam skills diretamente. O MX Core é responsável por identidade, contexto, seleção, política, lifecycle, supervisão, resposta e observabilidade.

## Alternativas consideradas

A alternativa de expor um endpoint por agent foi rejeitada por duplicar autenticação, contexto, contratos e regras de autonomia. A alternativa de deixar o modelo escolher e executar skills livremente foi rejeitada por tornar a política implícita e difícil de auditar.

## Consequências

O contrato de cliente permanece simples e estável. Novas skills exigem contrato, registry, testes e política, mas não alteram todos os canais. O Core concentra responsabilidade de orquestração e precisa manter limites claros para não virar uma classe monolítica; portas e casos de uso devem preservar a separação interna.

## Validação

Testar que web, Android e iOS enviam somente mensagens ao Core; que uma skill não consegue acessar banco ou filesystem diretamente; que o `runId` e o `correlationId` atravessam a cadeia; e que uma tool nunca é executada fora do Policy Engine.
