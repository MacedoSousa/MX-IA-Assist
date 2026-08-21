# Evidências de validação — Fase 8 / Validation evidence — Phase 8

**Projeto / Project:** MX — Sistema integrado IA + Agents
**Ambiente / Environment:** clone de validação em Linux, com alvo operacional Windows + Docker Desktop
**Data da execução / Execution date:** 20 de agosto de 2026, GMT-3

## Resultado executivo / Executive result

A validação automatizada da fase foi concluída sem falhas de teste. O backend foi compilado e testado com JDK 21/Maven Wrapper; o cliente Expo passou no typecheck e foi exportado para web com sucesso. O índice real de estudos foi medido sem chamar modelo externo. O Compose foi validado estruturalmente e preserva os seis serviços MX.

| Área / Area | Evidência / Evidence | Resultado / Result |
|---|---|---|
| Backend TDD | `bash mvnw -q test` | 96 testes, 0 falhas, 0 erros, 0 ignorados |
| Ollama | `OllamaServiceTest` direcionado | Payload multimodal, `keep_alive`, `num_ctx` e `num_thread` verificados |
| Cliente Expo | `npm run typecheck` | Aprovado |
| Export web | `npx expo export --platform web` | Aprovado; bundle web gerado com 446 kB |
| Segurança de anexos | `AttachmentServiceTest` | MIME, tamanho, checksum, ownership e traversal cobertos |
| Índice de estudos | `scripts/benchmark_learning.py` | 161 chunks; carga 2,644 ms; mediana de recuperação 7,069 ms; P95 7,714 ms |
| Compose | `scripts/validate_compose.py` | YAML válido; seis serviços presentes; Ollama publicado somente em localhost |
| Integridade do diff | `git diff --check` | Aprovado |

## Testes e observações / Tests and observations

A suíte Surefire identificou 34 classes de teste e 96 casos executados. Não foram encontrados failures, errors ou skips. O log ainda apresenta avisos conhecidos do ambiente de testes: o Mockito faz auto-attach dinâmico de um agente Byte Buddy, o Spring avisa sobre `open-in-view` e o contexto de teste mostra uma senha gerada pelo fallback padrão. Esses avisos não representam falhas da suíte, mas devem permanecer no backlog de endurecimento do perfil produtivo.

O typecheck do cliente foi executado com TypeScript 5.9 e o export web foi executado pelo Expo 54. O projeto não possui script `build` separado; o comando operacional equivalente é `npx expo export --platform web`, que corresponde ao fluxo usado no Dockerfile do cliente.

## Benchmark de aprendizagem / Learning benchmark

O benchmark utilizou exclusivamente o arquivo real `core-service/src/main/resources/knowledge/estudos/knowledge_chunks.jsonl`, sem dados sintéticos e sem chamada a serviço externo. Foram executadas cinco consultas representativas, repetidas 100 vezes, com limite de quatro resultados por consulta.

| Métrica / Metric | Valor / Value |
|---|---:|
| Chunks indexados / Indexed chunks | 161 |
| Tempo de carga / Load time | 2,644 ms |
| Latência mínima / Minimum latency | 6,855 ms |
| Mediana / Median | 7,069 ms |
| P95 | 7,714 ms |
| Máximo / Maximum | 10,426 ms |

Esses números medem a recuperação lexical local em Python sobre o índice versionado, não o tempo total de resposta do LLM. A latência final também depende do hardware, do modelo carregado, do contexto, do streaming e da presença de GPU. O relatório bruto está em `docs/evidence/learning-benchmark.json` e o benchmark pode ser repetido por `python3 scripts/benchmark_learning.py`.

## Segurança e privacidade / Security and privacy

O upload aceita apenas os MIME types definidos no allowlist e impõe limite de bytes durante a cópia, não somente pelo tamanho declarado pelo cliente. O nome original é sanitizado, o arquivo físico recebe um nome baseado em UUID, o armazenamento utiliza escrita temporária e o checksum SHA-256 é persistido. A resolução para o modelo consulta simultaneamente o ID do anexo e o ID do proprietário. O download utiliza `Content-Disposition: attachment`, `X-Content-Type-Options: nosniff` e `Cache-Control: no-store`.

A porta do Ollama foi limitada a `127.0.0.1:11434` no host, enquanto o `mx-core` continua utilizando `ollama:11434` na rede interna do Compose. O servidor Ollama está configurado com `OLLAMA_NO_CLOUD=1` por padrão. Nenhum token Tailscale, token DuckDNS ou chave privada foi incluído nas alterações verificadas.

## Limitações da execução / Execution limitations

O daemon Docker não estava disponível no sandbox Linux durante a validação, portanto não foram reiniciados containers nem executado um teste de inferência real no Ollama nesta etapa. No computador Windows conectado, o Docker Compose v5.3.1 está instalado e o Compose existente foi localizado, mas o clone de validação separado não foi alterado diretamente. A aplicação continua pronta para validação operacional após o próximo `git pull` no Windows.

## English summary

Phase 8 passed automated validation. The backend executed 96 tests with zero failures, errors, or skips. The Expo client passed TypeScript checking and exported successfully for web. The real 161-chunk study index was benchmarked locally: 2.644 ms load time, 7.069 ms median lexical retrieval, and 7.714 ms P95. Attachment security covers MIME allowlisting, byte limits, SHA-256 checksums, owner isolation, safe paths, and secure download headers. The Compose file is structurally valid and keeps Ollama bound to localhost while the MX core uses the internal network.

The sandbox did not provide a Docker daemon, so no containers were restarted and no live model inference benchmark was claimed. Docker Compose v5.3.1 is available on the connected Windows machine; operational validation should be run there after pulling the published commit.

## Referências / References

[1]: https://docs.ollama.com/faq "Ollama FAQ — context, keep-alive, GPU and Docker"
[2]: https://docs.ollama.com/context-length "Ollama — Context length"
[3]: https://www.mintlify.com/ollama/ollama/advanced/environment-variables "Ollama — Environment variables"
