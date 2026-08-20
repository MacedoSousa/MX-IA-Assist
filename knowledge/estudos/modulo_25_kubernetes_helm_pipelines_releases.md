# Módulo 25 — Kubernetes, Helm, pipelines e releases

## Fontes Alura

[Busca autenticada por Kubernetes, Helm e CI/CD](https://cursos.alura.com.br/app/search?query=Kubernetes+Helm+pipelines+CI%2FCD+releases+infraestrutura)

| Curso | Aplicação |
|---|---|
| [Kubernetes: criando e gerenciando charts com o Helm](https://cursos.alura.com.br/course/kubernetes-criando-gerenciando-charts-helm) | Empacotamento, values, templates, dependências, upgrades e rollback |
| [Infraestrutura como código: Terraform e Kubernetes](https://cursos.alura.com.br/course/infraestrutura-codigo-terraform-kubernetes) | Provisionamento declarativo, cluster e aplicação cloud |
| [Infraestrutura como Código e IA](https://cursos.alura.com.br/course/ia-como-copiloto-em-infraestrutura) | Terraform, Helm, Kubernetes, governança e uso controlado de IA |
| [Segurança em Pipelines](https://cursos.alura.com.br/course/seguranca-pipelines-integrando-praticas-seguranca-ci-cd) | SAST, SCA, DAST e deploy seguro |
| [SAST em CI/CD](https://cursos.alura.com.br/course/appsec-analise-estatica-seguranca-sast) | SonarQube, Semgrep e análise estática |
| [Trilha Infraestrutura como código](https://cursos.alura.com.br/formacao-infraestrutura-como-codigo) | Terraform, Ansible, AWS e automação operacional |

## Modelo estrutural

O cluster não deve substituir a arquitetura de domínio. O MX deve permanecer modular, com containers reproduzíveis, configuração externa, secrets fora das imagens, probes separadas, limites de recursos, política de atualização e observabilidade. Kubernetes é uma camada de operação; não deve ser usado para resolver problemas de modelagem, contratos ou governança.

Helm deve ser tratado como pacote versionado. Cada chart precisa possuir `Chart.yaml`, templates revisáveis, `values` por ambiente, validação de schema, labels padronizados, hooks mínimos, política de secrets, probes, requests/limits, estratégia de rollout e procedimento de rollback. Valores sensíveis não devem ser commitados.

## Pipeline recomendado

O pipeline deve executar lint, testes unitários, testes de integração, validação de contrato, SAST, SCA, análise de imagem, build reprodutível, assinatura ou proveniência de artefato, publicação, deploy em ambiente de teste, smoke test, avaliação de qualidade de IA, aprovação e promoção progressiva. Releases devem possuir versão, changelog, evidências, owner, janela, risco e rollback.

## Checklist de release

| Controle | Pergunta |
|---|---|
| Artefato | A imagem e o chart são imutáveis e rastreáveis? |
| Compatibilidade | O schema e os consumidores continuam compatíveis? |
| Segurança | Dependências, imagem, código e configurações foram analisados? |
| Operação | Há health, readiness, métricas, logs e traces? |
| Dados | Migrações são reversíveis ou possuem backup/restauração testados? |
| Rollout | Existe canário, blue/green ou promoção gradual adequada? |
| Rollback | A versão anterior pode ser restaurada com evidência? |
| Governança | A mudança possui aprovação, risco e plano de comunicação? |

## Aplicação no MX

O MX Core pode ser empacotado como deployment Java, workers de ingestão como deployments separados e jobs de reindexação como Jobs/CronJobs. Brokers, PostgreSQL e vector stores devem ter estratégia explícita de persistência; não se deve presumir que um pod efêmero seja um banco. O assistente poderá explicar manifests e detectar inconsistências, mas não deve aplicar mudanças destrutivas no cluster sem autorização.
