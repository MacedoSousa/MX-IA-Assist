# RAG semântico local do protótipo MX

## Escopo entregue

O contexto de estudos passou a combinar o ranking lexical já existente com **embeddings locais** gerados pelo Ollama. A implementação usa `nomic-embed-text` no endpoint local `/api/embed`, mantém um índice vetorial em memória por processo e soma um score de similaridade por cosseno ao score lexical para reordenar os chunks elegíveis. A API do Ollama suporta gerar vetores em lote e orienta a busca semântica por similaridade de cosseno.[1]

| Camada | Comportamento |
|---|---|
| Fonte autorizada compartilhada | Todos os chunks importados sem campo `owner` recebem o escopo `shared-authorized`. |
| Evidência externa em runtime | Recebe o UUID do usuário proprietário; outro usuário não a recupera nem a cita. |
| Ranking | Lexical controlado + score semântico; falha de embeddings retorna ao ranking lexical sem interromper a conversa. |
| Vetores | Cache em memória por ID do chunk. Vetores e texto da consulta não são registrados em log. |
| Citações | Mantêm fonte, origem, seção, trecho, SHA-256 e indicação honesta de página. |
| Modelo de embeddings | `nomic-embed-text`, instalado localmente no Ollama Docker e validado com um vetor de 768 dimensões. |

## Limites explícitos

Este incremento não cria um banco vetorial persistente e não altera o corpus autorizado. O índice é reconstruído quando o Core reinicia; isso preserva a simplicidade e evita gravar vetores de perguntas/evidências de usuário sem uma política de retenção aprovada. A próxima etapa deve avaliar persistência por proprietário, versão do embedding, reindexação auditável e métricas de relevância antes de introduzir uma base vetorial.

O fallback lexical permanece obrigatório: indisponibilidade do endpoint local, resposta inválida ou dimensão incompatível não derrubam o chat nem provocam busca externa sem as regras já existentes de cobertura.

## Evidências

`scripts/validate-ollama-embeddings.py` confirmou o endpoint local e o vetor de 768 dimensões sem imprimir o vetor. A suíte do Core cobre recuperação por similaridade quando não há sobreposição lexical e nega evidência externa de um proprietário a outro.

## Referências

[1] [Ollama — Embeddings](https://docs.ollama.com/capabilities/embeddings)
