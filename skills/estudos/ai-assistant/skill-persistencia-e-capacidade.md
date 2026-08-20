# Skill — Persistência e capacidade

## Finalidade
Selecionar e dimensionar mecanismos de persistência para o MX com base em requisitos verificáveis.

## Entradas
Recebe entidades, padrões de leitura/escrita, volume médio e de pico, latência, consistência, retenção, sensibilidade, disponibilidade, crescimento, orçamento e capacidade operacional.

## Procedimento
Compara PostgreSQL, JSONB, MongoDB, Redis, Kafka e Cassandra. Calcula volume, replicação, overhead, backlog, throughput e margem. Verifica índices, TTL, schema, migração, backup, restauração, criptografia, acesso mínimo e observabilidade.

## Saída
Retorna recomendação, alternativas descartadas, fórmula, unidades, premissas, riscos, plano de teste e ADR sugerida.

## Restrições
Não escolhe tecnologia apenas por popularidade, não mascara incerteza e não executa migração destrutiva sem backup comprovado e confirmação humana.
