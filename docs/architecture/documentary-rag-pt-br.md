# RAG documental do MX

## Objetivo e escopo

O RAG do MX fornece **memória documental versionada**, não altera pesos de modelos e não constitui fine-tuning. O corpus autorizado é carregado do classpath, tratado sempre como dado não confiável e recuperado seletivamente para dar contexto a skills internas. A aplicação mantém o MX Core como ponto único de orquestração: a fonte não pode executar instruções, ampliar permissões ou substituir políticas.

| Elemento | Implementação atual | Regra de segurança |
|---|---|---|
| Corpus | `knowledge/estudos/knowledge_chunks.jsonl` autorizado e versionado | Apenas arquivos incluídos no projeto são carregados |
| Recuperação | Ranking lexical com expansão de consulta e limite de três chunks | Não injeta o corpus inteiro no prompt |
| Citação | Fonte, origem, seção, índice do trecho, SHA-256 e página quando existente | Página ausente é exibida como `não informada` |
| Resposta | Rodapé determinístico de fontes no resultado de skills | Não depende apenas de o modelo obedecer a instruções |
| Roteamento | GeneralSkill, especialistas de estudos e QualitySkill | A citação é anexada depois da geração, no servidor |
| Desempenho | Contexto RAG limitado a 3.800 caracteres; contexto local Windows em 4.096 tokens; `think=false` e `num_predict=320` apenas em RAG fundamentado | Outros pedidos preservam o modelo padrão e a política de raciocínio existente |

## Contrato de citação

Cada fonte selecionada utiliza a estrutura abaixo. Os valores vêm do manifesto de chunks; nenhum hash, página ou origem é criado pelo modelo.

```text
Fonte: <source> | Citação: origem=<destination>; seção=<heading>; trecho=<chunk_index>; versão=sha256:<sha256>; página=<page ou não informada>
```

As citações são recuperadas novamente no servidor e anexadas ao resultado final de maneira determinística. Isso preserva rastreabilidade mesmo quando o modelo resume uma fonte sem repetir sua referência. No streaming, o mesmo rodapé é enviado depois dos tokens de resposta e armazenado junto com a mensagem final.

> Uma resposta sem chunk selecionado não recebe fonte sintética. A ausência de evidência precisa resultar em limitação declarada ou em busca externa sujeita à política, e não em citação inventada.

## Avaliação automatizada

| Caso | Critério validado | Cobertura no código |
|---|---|---|
| `rag-001` | Conteúdo coberto produz citação documental com hash válido | `StudyKnowledgeContextTest` |
| `rag-002` | Tema desconhecido é classificado com cobertura insuficiente e sem fonte | `StudyKnowledgeContextTest` |
| `rag-003` | A citação preserva o hash SHA-256 do chunk vigente | `StudyKnowledgeContextTest` |
| Rota geral | A GeneralSkill acrescenta fontes ao resultado fundamentado | `GeneralSkillPromptSecurityTest` |
| Rota especialista | Especialistas derivados de `StudySpecialistSkill` acrescentam fontes | `DomainSpecialistSkillTest` |
| Rota de qualidade | QualitySkill recebe contexto local e acrescenta fontes | `QualitySkillTest` |

Os testes são executados com `bash ./mvnw -q test` no clone Linux. No Windows, o smoke test `verify-mx-native-shadow.ps1 -TestRagCitation` confirma login, rota de conversa e presença de uma citação com SHA-256 sem imprimir token, senha ou a resposta completa.

## Limites e próxima evolução

O ranking atual ainda é lexical; embeddings, vector store, filtros explícitos de proprietário, páginas nativas de PDFs e reranking semântico continuam pendentes. A evolução deve preservar o contrato de citação, a avaliação `rag-001` a `rag-003`, o isolamento de dados por usuário e o tratamento de documentos como conteúdo não confiável. Nenhum mecanismo pode promover aprendizagem automática, treinamento de modelo ou publicação de conteúdo sem evidência, aprovação e auditoria.
