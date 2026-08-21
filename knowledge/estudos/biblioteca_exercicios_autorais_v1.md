# Biblioteca multidisciplinar — sínteses e exercícios autorais v1

> Este material é original e foi criado para estudo interno da squad. Ele não reproduz aulas, livros, atividades ou gabaritos da Alura.

## 1. Empreendedorismo e produto

### Síntese
Uma ideia só se torna oportunidade quando existe um problema relevante, um público identificável, uma proposta de valor testável e uma forma plausível de entrega. A análise deve separar hipótese, evidência e decisão. Custos, margem, risco, capacidade operacional e escalabilidade precisam ser avaliados antes de investir em implementação.

### Exercício autoral
Uma squad deseja oferecer um assistente local para pequenas empresas. Defina três hipóteses de problema, dois segmentos de usuários, uma proposta de valor mensurável e quatro indicadores de validação.

### Gabarito orientativo
As hipóteses podem envolver redução do tempo de resposta, organização documental e automação de rotinas repetitivas. Os segmentos podem ser escritórios contábeis e equipes administrativas. A proposta deve indicar benefício e métrica, por exemplo: reduzir em 30% o tempo gasto na localização de documentos. Indicadores adequados incluem taxa de uso recorrente, tempo economizado, taxa de respostas úteis e custo por operação.

## 2. Finanças e custos técnicos

### Síntese
Uma decisão financeira técnica deve distinguir custo fixo, custo variável, investimento inicial, custo operacional e custo de oportunidade. Para comparar arquiteturas, é necessário estimar volume, retenção, latência, disponibilidade, consumo de computação, armazenamento, rede, suporte e risco de indisponibilidade.

### Exercício autoral
Compare uma implantação local e uma implantação em nuvem para um serviço que processa 100.000 requisições mensais. Liste as premissas mínimas antes de calcular o custo total.

### Gabarito orientativo
As premissas devem incluir tamanho médio da requisição, tokens ou tempo de CPU, armazenamento, retenção, tráfego de entrada e saída, número de réplicas, disponibilidade desejada, backup, monitoramento, suporte, licenças e crescimento esperado. Sem essas premissas, um número de custo seria apenas uma estimativa sem rastreabilidade.

## 3. Educação corporativa e aprendizagem

### Síntese
Uma biblioteca de conhecimento é útil quando possui objetivos, pré-requisitos, sequência, exemplos, prática, avaliação e mecanismo de revisão. Ler documentos não equivale a aprender permanentemente; a evolução deve ser registrada por evidências, resultados de testes, correções e versões.

### Exercício autoral
Projete uma trilha de quatro etapas para ensinar RAG a uma pessoa que conhece Python, mas não conhece bancos vetoriais.

### Gabarito orientativo
A sequência pode ser: fundamentos de LLM e embeddings; preparação e particionamento de documentos; indexação e recuperação; geração fundamentada e avaliação. Cada etapa deve ter um projeto pequeno, critérios de conclusão e uma avaliação de falhas, como recuperação irrelevante, ausência de citação e alucinação.

## 4. Comunicação, liderança e cultura

### Síntese
Comunicação técnica eficiente explicita contexto, decisão, alternativas, riscos, responsáveis e próximo passo. Liderança operacional não é apenas distribuir tarefas; envolve remover impedimentos, dar feedback, proteger foco, tornar critérios visíveis e criar um ambiente em que problemas possam ser reportados cedo.

### Exercício autoral
Escreva uma comunicação de release que informe uma mudança de arquitetura sem ocultar risco ou impacto para os usuários.

### Gabarito orientativo
A comunicação deve conter objetivo, escopo, data, impacto esperado, impacto conhecido, plano de monitoramento, estratégia de rollback, responsáveis, canal de incidentes e critério para interromper a liberação. Termos vagos como “sem risco” devem ser evitados quando não houver evidência.

## 5. LGPD, segurança e governança

### Síntese
Privacidade e segurança precisam ser consideradas no desenho do produto, não adicionadas somente ao final. O sistema deve minimizar dados coletados, limitar acesso, proteger segredos, registrar auditoria sem expor conteúdo sensível, definir retenção e permitir resposta a incidentes.

### Exercício autoral
Um assistente local recebe documentos empresariais e gera respostas. Liste cinco controles de proteção e duas evidências de auditoria que não revelem o conteúdo dos documentos.

### Gabarito orientativo
Controles possíveis: criptografia em repouso, criptografia em trânsito, autorização por função, isolamento por tenant, redaction de logs, retenção configurável e exclusão verificável. Evidências podem registrar identificador pseudonimizado do documento, versão do índice, política aplicada, usuário técnico, timestamp e resultado da operação, sem armazenar o texto original.

## 6. Customer Success e melhoria contínua

### Síntese
O sucesso do cliente deve ser medido pelo valor produzido, não apenas pelo número de funcionalidades entregues. Métricas úteis incluem ativação, uso recorrente, tempo até o primeiro valor, taxa de resolução, satisfação, incidentes e custo operacional. Cada métrica precisa de definição, fonte, periodicidade e interpretação.

### Exercício autoral
Defina um painel mínimo para avaliar a adoção do assistente local por uma squad de desenvolvimento.

### Gabarito orientativo
O painel pode conter usuários ativos semanais, tarefas concluídas com apoio do assistente, tempo médio economizado, taxa de respostas aceitas sem correção, taxa de respostas corrigidas, falhas de ferramenta, custo por sessão e incidentes de segurança. A interpretação deve considerar qualidade e segurança, não apenas volume de uso.

## Critérios de qualidade dos exercícios

Cada exercício deve possuir objetivo observável, dados ou premissas explícitos, resposta verificável, justificativa, dificuldade, competência relacionada e possibilidade de execução prática. Gabaritos devem explicar o raciocínio e admitir alternativas tecnicamente válidas quando as premissas forem diferentes.

## Próxima versão

A versão seguinte deverá incluir sínteses e exercícios autorais de gestão de projetos, Kanban, bancos de dados, cloud, infraestrutura, programação, UX/UI, mobile, análise de dados, IA, qualidade e arquitetura, relacionando cada competência aos contratos do MX.
