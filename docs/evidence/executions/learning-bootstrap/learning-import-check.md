# Evidência de importação do pacote de aprendizagem

**Data da execução:** 2026-08-20 15:14 UTC
**Origem autorizada:** tarefa referenciada “Você pode aprender no Alura com meu acesso?”
**Destino:** projeto MX
**Método:** `scripts/bootstrap_learning.py` sem `--force`

## Resultado

A simulação (`--dry-run`) identificou **49 registros** e nenhum erro. A execução real copiou os 49 arquivos para o MX. A reexecução com rótulos estáveis não alterou os arquivos de conteúdo: reconheceu os 49 registros como idênticos e marcou todos como `skipped`.

| Verificação | Resultado |
|---|---:|
| Registros selecionados | 49 |
| Arquivos de conhecimento | 35 |
| Arquivos de skills | 12 |
| Arquivos de avaliação | 2 |
| Arquivos copiados na primeira execução | 49 |
| Arquivos ignorados na reexecução idempotente | 49 |
| Conflitos de hash | 0 |
| Erros de importação | 0 |
| JSONL válido | Sim |
| Padrões de credenciais detectados nos materiais | Nenhum |
| Código importado executado | Não |

## Destinos

- `knowledge/estudos/`: módulos, currículos, mapas e integração com o MX.
- `skills/estudos/`: skills documentais para RAG, ferramentas, observabilidade, mobile, qualidade, release e demais temas autorizados.
- `evaluation/estudos/`: casos JSONL e modelo de registro de avaliação.
- `knowledge_manifest.json`: SHA-256, tamanho, tipo e seções de cada registro.
- `KNOWLEDGE_INDEX.md`: índice navegável.
- `LEARNING_POLICY.md`: política de uso seguro.
- `learning_audit.jsonl`: auditoria da importação.

## Controles aplicados

O bootstrap aceitou somente extensões documentais autorizadas, ignorou diretórios de build e dependências, não sobrescreveu destinos com hash diferente, validou cada linha JSONL como objeto JSON e calculou SHA-256 antes da cópia. O conteúdo recuperado foi tratado como **dados não privilegiados**; nenhuma instrução encontrada nos documentos foi executada.

O manifesto e a auditoria usam os rótulos estáveis `estudos-autorizados` e `mx`, evitando registrar caminhos absolutos da máquina no repositório.

## Limite do aprendizado

A importação cria memória documental, skills versionadas, contratos e avaliações de regressão. Ela **não altera pesos do modelo**, não produz fine-tuning e não significa que o Ollama tenha aprendido permanentemente o conteúdo. A evolução deve seguir o ciclo `ler → interpretar → aplicar → testar → avaliar → registrar`.
