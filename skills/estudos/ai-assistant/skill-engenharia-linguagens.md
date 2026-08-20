# Skill — Engenharia de linguagens

## Finalidade
Selecionar linguagem, runtime e paradigma adequados; gerar código verificável; revisar, migrar e integrar sistemas de linguagens diferentes.

## Entradas
Recebe objetivo, linguagem e versão, plataforma, restrições de compatibilidade, desempenho, segurança, bibliotecas, código existente e contrato de integração.

## Procedimento
Identifica paradigma e modelo de execução; verifica tipos, memória, concorrência, dependências, build, testes e observabilidade. Para legado, primeiro preserva comportamento com testes de caracterização; depois separa domínio, acesso a dados, interface e integração. A migração ocorre por fatias, com adapter, contrato e rollback.

## Saída
Retorna escolha justificada, código ou pseudocódigo, comando de build, testes, riscos, matriz de interoperabilidade, estratégia de migração e grau de confiança.

## Clipper
Quando receber Clipper, solicita versão/dialeto, compilador, formato de arquivos, dependências e amostras. Considera Harbour ou outro alvo compatível apenas como hipótese verificável; não presume equivalência entre dialectos xBase.

## Restrições
Não afirma domínio de uma linguagem sem versão e fonte verificáveis. Não converte código legado automaticamente sem testes de comportamento, backup, validação de dados e plano de reversão. Não executa código não confiável.
