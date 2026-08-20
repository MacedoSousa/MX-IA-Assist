# Módulo 13 — MLOps, modelos e APIs

## Fonte
[MLOps: Machine Learning e APIs](https://cursos.alura.com.br/course/mlops-machine-learning-e-apis). A ementa cobre MLOps, APIs Flask, serving de modelos, dependências, serialização, POST, autenticação básica, requests e ambientes virtuais.

## Aplicação ao MX
Modelos auxiliares — classificação, reranking, detecção de intenção ou filtro de segurança — devem ser publicados atrás de um contrato de inferência estável. O contrato deve receber versão do modelo, entrada validada, identificador de correlação e contexto mínimo; a saída deve conter classe ou score, versão, latência e indicação de incerteza.

## Governança mínima
Serializar modelo e pré-processamento como uma unidade versionada. Fixar dependências e ambiente. Expor endpoints separados para `/health`, `/ready`, `/predict` e `/metadata`. O endpoint de predição deve validar schema, limitar tamanho, aplicar autenticação e não registrar dados sensíveis. O modelo deve possuir card com dados de treinamento, limitações, métricas, viés conhecido e procedimento de rollback.

## Operação
Separar liveness de readiness: o processo pode estar vivo sem estar pronto para inferência. Monitorar latência, taxa de erro, distribuição da entrada, taxa de rejeição, drift, uso de CPU/memória e qualidade após feedback. Testar compatibilidade entre artefato, versão de biblioteca e infraestrutura antes do deploy.

## Skill derivada
**Servir modelo auxiliar:** carregar artefato validado; verificar integridade e versão; validar request; executar pré-processamento e inferência; devolver score e metadados; registrar métricas redigidas; rejeitar entradas fora do contrato; permitir rollback e auditoria.

## Referências
[1]: https://cursos.alura.com.br/course/mlops-machine-learning-e-apis "Curso da Alura — MLOps: Machine Learning e APIs"
