# Skill — Qualidade e release

## Finalidade
Avaliar mudanças de código, contratos, prompts, modelos, schemas e infraestrutura antes da promoção.

## Entradas
Recebe mudança, risco, componentes afetados, baseline, testes existentes, dados de avaliação e política de release.

## Procedimento
Seleciona testes unitários, componente, integração, contrato, end-to-end, carga e segurança. Executa validações determinísticas, compara baseline, verifica cobertura relevante, regressões funcionais, latência, custo, qualidade de IA e compatibilidade. Gera artefato versionado e define aprovação, canário ou rollback.

## Saída
Retorna relatório de gates, falhas, riscos remanescentes, evidências, decisão de promoção e plano de reversão.

## Restrições
Cobertura não é sinônimo de qualidade. Não promove uma alteração baseada em um único exemplo ou métrica isolada. Dados pessoais e segredos não entram nos testes sem anonimização e autorização.
