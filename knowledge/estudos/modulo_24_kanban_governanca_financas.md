# Módulo 24 — Kanban, governança, administração, finanças e custos

## Objetivo

Organizar o trabalho técnico do MX com fluxo previsível, governança proporcional ao risco e controle transparente de custos.

## Kanban operacional

O quadro deve representar estados reais: descoberta, especificação, pronto para desenvolvimento, em desenvolvimento, revisão, testes, validação, pronto para entrega e concluído. Bloqueios precisam possuir motivo, responsável pela resolução e data de reavaliação. O limite de WIP deve ser ajustado com base em dados, não em preferência.

| Métrica | Fórmula ou leitura | Uso |
|---|---|---|
| Lead time | Tempo entre solicitação e entrega | Avaliar previsibilidade do serviço |
| Cycle time | Tempo em execução até conclusão | Detectar gargalos de execução |
| Throughput | Itens concluídos por período | Planejar capacidade com cautela |
| WIP | Itens em andamento | Limitar multitarefa e filas ocultas |
| Aging | Idade de item ainda aberto | Priorizar bloqueios e riscos |
| Retrabalho | Trabalho repetido por defeito ou mudança | Melhorar qualidade e descoberta |

## Governança técnica

Decisões arquiteturais, mudanças de modelo, novos provedores, alterações de schema, permissões e integrações externas devem possuir responsável, objetivo, risco, evidência, prazo de revisão e plano de reversão. Governança não deve criar burocracia para mudanças de baixo risco, mas precisa impedir alterações irreversíveis sem avaliação.

## Finanças e custos técnicos

O controle mínimo deve separar investimento inicial, operação recorrente e custo variável. Para IA, o custo variável pode ser estimado por:

`custo_IA = requisições × (tokens_entrada × preço_entrada + tokens_saída × preço_saída) + embeddings + armazenamento + telemetria`

Para cloud:

`custo_total = computação + armazenamento + rede + serviços gerenciados + observabilidade + suporte + contingência`

Toda estimativa precisa indicar moeda, período, unidade, volume, faixa de incerteza, impostos ou encargos quando aplicáveis e origem dos preços. Não se deve apresentar uma estimativa como valor contábil ou compromisso financeiro.

## Administração operacional

O MX deverá manter inventário de ativos, fornecedores, contratos, secrets, ambientes, permissões, backups, dependências, licenças e responsáveis. O ciclo de vida inclui aquisição, uso, monitoramento, revisão, renovação e descarte seguro.

## Indicadores de decisão

A gestão combinará valor entregue, risco reduzido, qualidade, custo, latência, disponibilidade, segurança, dívida técnica e aprendizado. Nenhum indicador isolado deverá orientar cortes, promoções, seleção de modelo ou decisão de arquitetura.

## Aplicação no assistente local

O assistente poderá gerar relatórios de fluxo, custos e riscos a partir de dados autorizados, mas deverá declarar fonte, período e incerteza. Ações administrativas ou financeiras que criem obrigação, pagamento ou alteração irreversível exigem confirmação explícita do usuário.
