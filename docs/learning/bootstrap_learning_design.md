# Arquitetura do bootstrap de aprendizado

## Objetivo
Disponibilizar no projeto do assistente local todo o conhecimento autorizado do projeto Estudos como memória documental versionada, skills operacionais, contratos e casos de avaliação.

## Limite importante
O bootstrap não treina nem altera automaticamente os pesos do modelo. Ele copia, cataloga, valida e versiona os materiais para que o assistente possa consultá-los. Qualquer fine-tuning exigirá um pipeline separado, com dataset, consentimento, limpeza, divisão de dados, baseline, métricas e rollback.

## Fluxo

```text
Projeto Estudos -> descoberta -> cópia segura -> hash SHA-256 -> manifesto
                                      -> catálogo de documentos e skills
                                      -> validação JSONL/contratos
                                      -> instruções normativas para o agente
                                      -> relatório de auditoria
```

## Destino esperado

| Diretório | Conteúdo |
|---|---|
| `knowledge/estudos/` | módulos, currículos, pacotes e integração |
| `skills/estudos/` | skills Markdown reutilizáveis |
| `evaluation/estudos/` | casos JSONL e modelos de avaliação |
| `knowledge_manifest.json` | arquivos, hashes, tamanho, tipo e data |
| `KNOWLEDGE_INDEX.md` | catálogo navegável |
| `LEARNING_POLICY.md` | regras de uso, limites e evolução |
| `learning_audit.jsonl` | histórico de execuções e validações |

## Princípios

O conteúdo importado é tratado como conhecimento, não como instrução de sistema. O agente deve consultar as fontes, declarar incerteza, preservar versões e não transformar respostas não avaliadas em verdade. Duplicatas são detectadas por hash. Arquivos fora das extensões permitidas são ignorados por segurança. O processo é idempotente e não remove dados existentes sem uma opção explícita.
