# Módulo 16 — Databricks, Lakehouse e MLOps em escala

## Fonte
[Databricks: engenharia de Machine Learning e MLOps](https://cursos.alura.com.br/course/databricks-engenharia-ml). A ementa cobre arquitetura Lakehouse, governança de dados estruturados e não estruturados, ETL com Spark SQL e Delta Lake, Feature Store, MLflow, tuning com Hyperopt/Optuna, AutoML, serving batch/streaming/tempo real, GitHub Actions, YAML, IaC com DABs, drift e retreino.

## Arquitetura aplicável
A arquitetura Medallion separa ingestão bruta, dados tratados e dados prontos para consumo. Para o assistente local, esse padrão pode organizar documentos, chunks, embeddings, avaliações e feedbacks, mantendo versões e metadados de origem.

## Features e modelos
Feature Store ajuda a manter features reutilizáveis e governadas. MLflow registra experimentos, métricas, artefatos e versões de modelo. Tuning, AutoML e SparkML devem ser usados quando houver hipótese mensurável; não substituem validação de dados, testes e análise de custo.

## Serving
O conteúdo diferencia batch, streaming e inferência em tempo real. Para o MX, batch atende reindexação e avaliação; streaming atende ingestão e telemetria; endpoint online atende classificadores ou rerankers com baixa latência. Cada modalidade precisa de SLA, política de retry e observabilidade próprios.

## MLOps
Pipeline completo: dados -> qualidade -> features -> treinamento -> avaliação -> registro -> aprovação -> deploy -> monitoramento -> detecção de drift -> retreino. GitHub Actions, YAML e IaC promovem reprodutibilidade; segredos devem permanecer fora do repositório.

## Skill derivada
**Gerenciar ciclo de vida de ML:** versionar dados e features; registrar experimento; comparar métricas; promover artefato aprovado; publicar no modo adequado; monitorar qualidade e drift; disparar retreino sob critérios definidos; permitir rollback.

## Referências
[1]: https://cursos.alura.com.br/course/databricks-engenharia-ml "Curso da Alura — Databricks: engenharia de Machine Learning e MLOps"
