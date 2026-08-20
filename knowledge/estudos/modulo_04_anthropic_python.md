# Módulo 04 — Anthropic, Python e chatbots multimodais

## Fonte
Formação da Alura: [Anthropic e Python: desenvolva assistentes e chatbots personalizados](https://cursos.alura.com.br/formacao-anthropic-python-desenvolva-assistentes-chatbots-personalizados). A página informa 2 cursos e 16 horas de carga horária.

## Conteúdo observado
A formação aborda fundamentos da API da Anthropic e do modelo Claude, integração com Python, gerenciamento de chaves, prompt engineering, prompt templates, seleção de modelos, custos, tokens, processamento em lote e tratamento de erros. O segundo módulo usa Flask para construir o backend de um chatbot e trabalha com prompts de texto e imagem.

## Conhecimento aplicado
O conteúdo reforça que o assistente local deve possuir uma camada de abstração de provedores. OpenAI-compatible, Anthropic/Claude e modelos locais podem implementar o mesmo contrato de geração, enquanto detalhes de autenticação, limites, formatos, tokens, streaming e erros ficam nos adaptadores. Isso permite comparar qualidade, custo, privacidade e latência sem reescrever o domínio do assistente.

## Skill proposta: adaptador multimodelo

**Entrada:** solicitação normalizada, mensagens, arquivos ou imagem opcional, modelo lógico, limite de tokens, timeout e política de privacidade.

**Processo:** resolver o provedor permitido; transformar o contrato interno no formato do provedor; aplicar limites; chamar a API ou runtime local; normalizar texto, metadados e tool calls; registrar métricas e remover segredos.

**Saída:** resposta normalizada com conteúdo, uso de tokens quando disponível, modelo efetivo, provedor, latência, referências e status de segurança.

**Limites:** a escolha de provedor não pode ser controlada exclusivamente pelo modelo; chaves devem permanecer em variáveis de ambiente ou cofre; processamento em lote exige limite de concorrência, idempotência e acompanhamento de falhas; conteúdo multimodal requer validação de tipo e tamanho.

## Aplicação no assistente local
O Flask apresentado pela formação pode servir como referência conceitual para um serviço Python de experimentação, mas a integração com o projeto MX deve respeitar a arquitetura existente. O backend Java continua responsável por autenticação, autorização, sessões, refresh token e contratos de negócio. O serviço de IA deve ser stateless quando possível, usar correlação de requisição e não persistir arquivos sem política de retenção.

## Relação com machine learning
O curso não equivale a treinamento de modelo. Seu principal valor para ML aplicado é fornecer uma camada de experimentação e avaliação com diferentes modelos. Para treinamento ou ajuste fino, serão necessários dataset versionado, critérios de qualidade, divisão entre treino/validação/teste, controle de dados sensíveis e comparação contra um baseline.

## Referências
[1]: https://cursos.alura.com.br/formacao-anthropic-python-desenvolva-assistentes-chatbots-personalizados "Formação da Alura — Anthropic e Python: desenvolva assistentes e chatbots personalizados"
