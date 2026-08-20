# Evidência — Login web e síntese documental do MX

**Data da execução:** 2026-08-20 13:00–13:45 (UTC-03)

## Resultado do diagnóstico

A conta `pedro@mx.local` existe no ambiente PostgreSQL do MX, está ativa e possui a função `owner`. O teste direto no backend retornou `HTTP 200`. A causa da falha no navegador remoto era o fallback do cliente web: a aplicação podia construir a API com `localhost:8080`, endereço que aponta para o dispositivo que abriu o navegador, e não para o computador que executa o MX.

## Correção aplicada

O cliente web agora usa o mesmo origin no navegador quando `EXPO_PUBLIC_MX_API_URL` não é fornecida. O Nginx do `mx-web` encaminha `/api/` para `mx-core:8080`, mantém buffering desativado para SSE e preserva os cabeçalhos de encaminhamento. O Compose deixou de embutir `http://localhost:8080` como configuração padrão do bundle.

O login foi validado pelo endpoint do cliente web em `http://localhost:8082/api/auth/login` com `HTTP 200` e payload autenticado. O mesmo fluxo foi validado pelo endpoint privado `https://mx-ai.taila61bd3.ts.net/api/auth/login` com `HTTP 200`. Tokens não foram registrados nesta evidência.

## Síntese documental

Foi criada a síntese temática em `knowledge/estudos/sintese-assistente.md` e uma cópia de runtime em `core-service/src/main/resources/knowledge/estudos/sintese-assistente.md`. A `GeneralSkill` carrega o recurso do classpath e o adiciona ao prompt como contexto documental não privilegiado. O texto instrui o modelo a consultar somente quando pertinente, combinar a orientação com evidências e contexto, declarar incerteza e não tratar a síntese como resposta fixa ou autorização operacional.

A síntese cobre fundamentos de IA generativa, APIs e modelos locais, RAG, avaliação, agentes, LangGraph, MCP, mensageria, observabilidade, MLOps, bancos de dados, Java, Clean Architecture, testes, frontend, mobile, UX, governança, infraestrutura, redes, criptografia e paradigmas de programação.

## Validação automatizada

O build de teste em container `maven:3.9-eclipse-temurin-21` foi concluído com sucesso para os testes direcionados `GeneralSkillPromptSecurityTest` e `StudyKnowledgeContextTest`. O build completo do conjunto TDD existente também foi concluído com sucesso no mesmo ambiente. As imagens e Dockerfiles temporários foram removidos após a validação.

## Limitações explícitas

A síntese é memória documental versionada. Ela não altera pesos do Ollama, não constitui fine-tuning e não garante que uma resposta futura seja idêntica ao texto dos estudos. O MX ainda deve utilizar políticas, skills especializadas, evidências atuais, testes e aprovação humana para decisões e ações que tenham efeito externo.
