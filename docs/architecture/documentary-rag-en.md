# MX documentary RAG

## Purpose and scope

MX RAG provides **versioned documentary memory**. It does not modify model weights and is not fine-tuning. The authorized corpus is loaded from the classpath, handled as untrusted data, and selectively retrieved as context for internal skills. MX Core remains the single orchestration point: a source cannot execute instructions, expand permissions, or replace policy.

| Element | Current implementation | Safety rule |
|---|---|---|
| Corpus | Authorized, versioned `knowledge/estudos/knowledge_chunks.jsonl` | Only project-included files are loaded |
| Retrieval | Lexical ranking with query expansion and a three-chunk limit | The full corpus is never injected into the prompt |
| Citation | Source, origin, section, chunk index, SHA-256 and page when available | Missing page is rendered as `não informada` |
| Answer | Deterministic source footer in skill results | It does not rely only on model compliance |
| Routing | GeneralSkill, study specialists and QualitySkill | Citations are appended after generation on the server |
| Performance | RAG context capped at 3,800 characters; Windows local context at 4,096 tokens; `think=false` and `num_predict=320` only for grounded RAG | Other requests retain the default model and existing reasoning policy |

## Citation contract

Every selected source follows the structure below. Values come from the chunk manifest; the model does not generate hashes, pages, or origins.

```text
Fonte: <source> | Citação: origem=<destination>; seção=<heading>; trecho=<chunk_index>; versão=sha256:<sha256>; página=<page or não informada>
```

The server retrieves citations again and deterministically appends them to the final result. This preserves traceability even if the model summarizes a source without repeating its reference. For streaming, the same footer is sent after the generated tokens and stored with the final message.

> An answer without a selected chunk does not receive a synthetic source. Missing evidence must lead to an explicit limitation or policy-governed external search, not an invented citation.

## Automated evaluation

| Case | Validated criterion | Code coverage |
|---|---|---|
| `rag-001` | Covered content produces a document citation with a valid hash | `StudyKnowledgeContextTest` |
| `rag-002` | Unknown topic is marked insufficient without a source | `StudyKnowledgeContextTest` |
| `rag-003` | Citation preserves the current chunk SHA-256 hash | `StudyKnowledgeContextTest` |
| General route | GeneralSkill appends sources to grounded output | `GeneralSkillPromptSecurityTest` |
| Specialist route | `StudySpecialistSkill` derivatives append sources | `DomainSpecialistSkillTest` |
| Quality route | QualitySkill receives local context and appends sources | `QualitySkillTest` |

Tests run through `bash ./mvnw -q test` in the Linux clone. On Windows, `verify-mx-native-shadow.ps1 -TestRagCitation` checks login, the real conversation route, and a SHA-256 citation without printing the token, password, or complete answer.

## Limits and next iteration

Current ranking is still lexical. Embeddings, a vector store, explicit owner filters, native PDF pages, and semantic reranking remain pending. The next implementation must preserve this citation contract, `rag-001` through `rag-003`, per-user data isolation, and the treatment of documents as untrusted content. No component may introduce automatic learning, model training, or content publication without evidence, approval, and auditability.
