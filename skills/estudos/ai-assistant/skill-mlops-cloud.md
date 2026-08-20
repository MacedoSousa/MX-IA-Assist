# Skill: MLOps e Cloud para serviços de IA

## Objetivo
Operacionalizar modelos, pipelines e serviços de IA com qualidade, segurança, observabilidade e possibilidade de rollback em ambiente local, AWS ou Azure.

## Entrada
Recebe artefato ou pipeline versionado, schema de entrada, versão de código, dependências, ambiente alvo, SLO de latência, orçamento, política de dados, estratégia de serving e critérios de promoção.

## Fluxo
Primeiro valida artefato, schema, dependências, integridade, origem e compatibilidade. Em seguida executa testes unitários, testes de contrato e avaliação de qualidade. Registra experimento, métricas, dados e versão. Compara com o champion e só promove o challenger se qualidade, segurança, custo, latência e estabilidade atenderem aos critérios definidos. Publica em batch, streaming ou endpoint online conforme o caso de uso.

A publicação deve expor `health`, `ready`, `predict` e `metadata`, aplicar autenticação, limitar payload, usar identidade segura e não gravar dados sensíveis. A infraestrutura deve separar rede pública, camada de aplicação, workers e dados; usar subnets, regras de entrada, firewall, balanceamento e probes quando executada em cloud.

Depois do deploy, instrumenta métricas, logs estruturados e traces. Monitora disponibilidade, p95/p99, erros, recursos, lag, DLQ, custo, drift e qualidade. Alarmes precisam de limiar, severidade, runbook, destinatário e janela de avaliação. Falhas devem acionar retry classificado, fallback ou rollback, sem duplicar efeitos.

## Saída
Retorna versão promovida, endpoint ou job, metadados de implantação, métricas iniciais, dashboard, alarmes, plano de rollback e evidências dos testes.

## Restrições
Não registrar prompts completos, documentos sensíveis, tokens, chaves ou dados pessoais sem autorização. Não promover modelo sem baseline e avaliação. Não depender de notebook ou estado não versionado. Não alterar produção sem aprovação e trilha de auditoria.

## Métricas mínimas
Disponibilidade; latência p50/p95/p99; taxa de erro; taxa de timeout; custo por requisição; uso de tokens; drift de entrada; qualidade da saída; taxa de rejeição; lag de consumidor; tamanho da DLQ; e tempo de recuperação.

## Critérios de aceitação
O modelo pode ser carregado em ambiente limpo; o contrato rejeita entradas inválidas; health e readiness distinguem processo vivo de serviço pronto; métricas e logs possuem correlação; alarmes são acionáveis; rollback restaura a versão anterior; e nenhuma credencial ou dado sensível aparece nos artefatos de telemetria.
