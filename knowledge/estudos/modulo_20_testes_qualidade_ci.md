# Módulo 20 — Testes, qualidade e entrega contínua

## Objetivo

Criar uma estratégia de qualidade que valide comportamento, contratos, segurança, desempenho e operabilidade do MX e do assistente local.

## Camadas

| Camada | Verifica | Exemplos no MX |
|---|---|---|
| Unitário | Regra isolada | Autorização, cálculo de custo, roteamento de modelo |
| Componente | Módulo com dependências controladas | Memória, RAG, adapter de modelo |
| Integração | Integração real entre componentes | PostgreSQL, Redis, broker e storage |
| Contrato | Compatibilidade entre consumidor e provedor | REST, eventos, MCP e streaming |
| End-to-end | Fluxo completo | Pergunta, recuperação, geração, citação e auditoria |
| Carga | Comportamento sob volume e concorrência | Chat, embeddings, filas e workers |
| Segurança | Resistência a abuso e exposição | Prompt injection, SSRF, secrets e autorização |

## Pipeline

O pipeline deve executar lint, análise estática, testes unitários, integração em ambiente efêmero, contratos, segurança de dependências, build reprodutível, testes de carga em ambiente controlado e publicação de artefato imutável. A promoção exige critérios objetivos e possibilidade de rollback.

## Qualidade de IA

Além do software tradicional, o assistente será avaliado por fundamentação, precisão de recuperação, validade das citações, taxa de recusa correta, resistência a instruções conflitantes, latência, custo e estabilidade. Os datasets de avaliação devem ser versionados e separados de dados pessoais ou segredos.

## Critérios de aprovação

Nenhum modelo, prompt, adapter ou alteração de schema será promovido apenas porque funciona em um exemplo. A mudança precisa passar por testes representativos, comparação com baseline, inspeção de regressões, análise de custo e aprovação compatível com o risco.

## Integração com Kanban

Defeitos encontrados em produção devem gerar item com impacto, severidade, evidência, reprodução e risco. O fluxo deve limitar WIP, reservar capacidade para correções e acompanhar tempo até detecção, contenção, resolução e recuperação.
