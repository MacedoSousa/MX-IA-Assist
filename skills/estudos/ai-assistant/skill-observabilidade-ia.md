# Skill: Observabilidade de IA

## Objetivo
Tornar chamadas de modelos, pipelines RAG e ferramentas diagnosticáveis, mensuráveis e auditáveis.

## Eventos
Registrar `trace_id`, `conversation_id`, operação, provedor, modelo, status, latência, tamanho de entrada e saída, retries, tipo de erro, uso de tokens quando disponível e decisão de validação.

## Métricas
Acompanhar p50/p95 de latência, taxa de erro, timeouts, retries, tamanho de contexto, custo estimado, qualidade de recuperação, fidelidade, rejeições e sucesso de ferramentas.

## Procedimento
Instrumente entrada e saída sem capturar conteúdo sensível integral. Propague correlação entre MX Core, serviços Python, mensageria e banco. Classifique erros transitórios e permanentes. Configure alertas para SLOs técnicos e de qualidade. Preserve amostras anonimizadas para avaliação reproduzível.

## Restrições
Nunca registrar chaves, tokens de autenticação ou dados pessoais sem política explícita. Não usar velocidade como único indicador de qualidade. Reter telemetria pelo período necessário e permitir exclusão.

## Avaliação
Simular falhas de provedor, timeout, retry, fila atrasada, índice indisponível e resposta sem evidência. Verificar se o diagnóstico é possível apenas com os metadados registrados.
