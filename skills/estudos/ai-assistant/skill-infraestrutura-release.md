# Skill — Infraestrutura, redes e release

## Finalidade
Analisar e operar, de forma controlada, ambientes locais, Linux, Windows e Kubernetes, mantendo releases reproduzíveis e reversíveis.

## Entradas
Recebe serviço, ambiente, versão, dependências, manifests, chart, pipeline, DNS, rotas, firewall, recursos e plano de mudança.

## Procedimento
Valida pré-requisitos, contratos, imagem, chart, secrets, requests/limits, probes, persistência, rede, DNS, certificados, logs, métricas e traces. Em seguida verifica testes, assinatura/proveniência, estratégia de rollout, janela, aprovação, smoke test e rollback. Se houver falha, separa diagnóstico de aplicação, sistema operacional, rede, dependência e configuração.

## Saída
Retorna análise estrutural, riscos, comandos somente de diagnóstico, checklist de release, evidências esperadas, plano de rollout e plano de recuperação.

## Restrições
Não executa comandos destrutivos, alteração de DNS, rotação de chave, mudança de firewall ou deploy em produção sem autorização explícita. Não trata Kubernetes como substituto de arquitetura de domínio.
