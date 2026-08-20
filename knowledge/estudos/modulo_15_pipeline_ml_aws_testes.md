# Módulo 15 — Pipeline de ML, testes e integração com AWS

## Fonte
[Engenharia de pipelines: machine learning e integração com AWS](https://cursos.alura.com.br/course/teste-garantia-qualidade). A ementa aborda fundamentos de ML, EDA, pipelines de pré-processamento e modelagem, testes unitários com PyTest, integração com AWS, champion/challenger e inferência.

## Aplicação ao assistente local
Qualquer componente de ML deve ser tratado como pipeline reprodutível: entrada versionada; validação; EDA; transformação; treinamento; avaliação; empacotamento; publicação; inferência; monitoramento. O pipeline não deve depender de estado implícito de notebook.

## Qualidade
Testar cada etapa isoladamente e o contrato entre etapas. Casos mínimos: schema inválido, coluna ausente, dados vazios, distribuição inesperada, modelo incompatível, falha de armazenamento e timeout. Testes devem cobrir determinismo, limites e regressão de métricas.

## Champion e challenger
O modelo champion permanece servindo enquanto o challenger é avaliado em dados controlados. A promoção deve depender de critérios explícitos de qualidade, latência, custo, segurança e estabilidade. A troca precisa ser reversível e registrada com versão de artefato e dados.

## Integração AWS
O material apresenta integração de plataforma com AWS e gestão de modelos. Para o MX, separar armazenamento de artefatos, execução de pipeline, endpoint de inferência e monitoramento. Credenciais devem ser obtidas por identidade/role e nunca codificadas nos projetos.

## Skill derivada
**Operacionalizar pipeline de ML:** validar dados; executar transformações versionadas; treinar ou carregar modelo; executar testes; comparar champion/challenger; publicar apenas se critérios forem atendidos; registrar artefatos e métricas; habilitar rollback.

## Referências
[1]: https://cursos.alura.com.br/course/teste-garantia-qualidade "Curso da Alura — Engenharia de pipelines: machine learning e integração com AWS"
