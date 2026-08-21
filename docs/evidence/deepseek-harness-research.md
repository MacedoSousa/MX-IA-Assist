# DeepSeek Harness (DSH) — pesquisa oficial

## Fontes

1. DeepSeek Harness developer preview: https://deepseek.com/harness/en/
2. Repositório oficial: https://github.com/deepseek-ai/deepseek-harness
3. Quickstart oficial: https://deepseek-harness.github.io/deepseek-harness/en/guide/quickstart
4. Configuração de providers: https://deepseek-harness.github.io/deepseek-harness/en/guide/providers
5. Adaptador OpenAI-compatible (`dsh-llm-pi-ai`): https://github.com/deepseek-ai/deepseek-harness/blob/master/packages/llm/llm-pi-ai/README.md
6. Adaptador DeepSeek oficial (`dsh-llm-deepseek`): https://github.com/deepseek-ai/deepseek-harness/blob/master/packages/llm/llm-deepseek/README.md

## Conclusões

O DSH é um agent harness open-source da DeepSeek em developer preview, não um modelo de linguagem. Sua arquitetura trata modelos, ferramentas, skills, sessões, sandboxes, armazenamento, loops, scheduling e interface como plugins. O Web UI oficial pode ser iniciado com `npx @deepseek-ai/dsh web` e, por padrão, usa a porta 3080.

A documentação permite cadastrar um provider customizado self-hosted com `baseURL`, protocolo e modelo. O adaptador `dsh-llm-pi-ai` suporta rotas OpenAI-compatible, enquanto a documentação oficial menciona explicitamente que um endpoint Ollama compatível pode ser configurado com a URL `/v1` e o nome exato do modelo. Para o MX, a configuração pretendida é provider local apontando para `http://ollama:11434/v1`, modelo `deepseek-r1:14b`, `api: openai-completions`, e compatibilidade com `supportsDeveloperRole: false` e `maxTokensField: max_tokens` se necessário.

O DSH possui Standard, Code, Minimal e Creator modes; registra sessões em logs append-only rastreáveis e exige uma workspace selecionada. Está em developer preview e pode sofrer mudanças incompatíveis. Por isso, ele deve entrar no MX como serviço opcional de harness/agent workspace, sem substituir o MX Core, a autenticação, a memória de projeto ou os contratos de skills já existentes.

A versão publicada consultada do pacote npm foi `@deepseek-ai/dsh@0.1.1-rc.1`. O DSH não torna automaticamente um modelo mais inteligente: ele fornece o runtime de agentes, ferramentas, planejamento, sessões e plugins. A capacidade cognitiva continua sendo determinada pelo modelo configurado.

## Validação adicional em 2026-08-21

A documentação oficial confirma que providers customizados self-hosted devem ser declarados em `$DSH_HOME/settings.yaml`, sob a seção `llm-pi-ai.providers.<id>`, com `api`, `baseURL`, `models` e, quando necessário, `compat`. Para gateways OpenAI-compatible que não aceitam o papel `developer` ou o campo `max_completion_tokens`, a documentação recomenda `compat.supportsDeveloperRole: false` e `compat.maxTokensField: max_tokens`. A compatibilidade `thinkingFormat: deepseek` é aceita para o protocolo `openai-completions` [7].

A mesma documentação confirma que modelos configurados manualmente são text-only por padrão e que o Web UI precisa de um workspace selecionado antes da execução de tarefas [7] [8].

No ambiente Windows do MX, a imagem `@deepseek-ai/dsh@0.1.0-rc.8` foi construída com sucesso. A interface HTTP respondeu `200 OK` por `http://localhost:3080/`, o endpoint RPC `llm.models` retornou o provider `mx-ollama` com o modelo `deepseek-r1:14b`, e uma sessão foi criada, selecionada explicitamente para esse provider e teve o prompt aceito pelo DSH. A chamada direta ao endpoint OpenAI-compatible do Ollama (`/v1/chat/completions`) também respondeu com `model: deepseek-r1:14b` e raciocínio do modelo. Como o runtime permaneceu com o turno em processamento durante o intervalo de validação, o turno foi cancelado para não manter carga desnecessária; a conectividade e o roteamento de provider foram comprovados, enquanto a conclusão completa pelo loop de agente permanece uma verificação manual recomendada na interface.

O DSH mantém o servidor interno em `127.0.0.1` por segurança. Como a versão instalada rejeita explicitamente `--host 0.0.0.0`, o container usa um encaminhador TCP local baseado em Node: o DSH escuta em `127.0.0.1:3081`, o proxy escuta em `0.0.0.0:3080` dentro do container e o Compose publica somente `127.0.0.1:3080` no Windows. Essa solução preserva o escopo local sem alterar o comportamento HTTP/WebSocket do Web UI.

### Fontes adicionais

7. DeepSeek Harness — Configure models: https://deepseek-harness.github.io/deepseek-harness/en/guide/providers
8. DeepSeek Harness — Use the Web UI: https://deepseek-harness.github.io/deepseek-harness/en/guide/quickstart
9. DeepSeek Harness — `dsh-llm-pi-ai` configuration reference: https://github.com/deepseek-ai/deepseek-harness/blob/master/packages/llm/llm-pi-ai/README.md
