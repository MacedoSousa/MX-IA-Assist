# Módulo 17 — PostgreSQL, SQL, PL/pgSQL e administração

## Fonte

Formação autenticada da Alura: https://cursos.alura.com.br/app/learning-guide/alura/postgresql

## Sequência observada

A formação possui 46 horas e seis cursos. A sequência começa com PostgreSQL e funções SQL, avança por DML/DDL, PL/pgSQL, triggers, transações, erros e cursores, e termina em administração e otimização.

| Etapa | Curso | Aplicação no MX |
|---|---|---|
| Consultas | PostgreSQL; Views, subconsultas e funções | Consultas de conversas, documentos, memória e relatórios |
| Manipulação | DML e DDL | Migrations, constraints e evolução do schema |
| Programação no banco | PL/pgSQL | Rotinas controladas quando a lógica pertence ao banco |
| Integridade | Triggers, transações, erros e cursores | Auditoria, consistência e operações atômicas |
| Operação | Administração e otimização | Índices, planner, backups, plano de consulta e monitoramento |

## Aplicação técnica

O MX deve privilegiar migrations versionadas, constraints explícitas, transações curtas e consultas analisadas pelo plano de execução. Triggers devem ser reservadas a invariantes de dados e auditoria, evitando regras de negócio ocultas. Backups precisam ser testados por restauração, e índices devem ser justificados por consultas observadas.

## Cálculos associados

O estudo registrará cardinalidade, seletividade, custo estimado de consulta, tamanho de índices, crescimento de armazenamento, retenção e capacidade de conexões. Toda estimativa deverá informar dados de entrada, unidade, período, fórmula e margem de incerteza.
