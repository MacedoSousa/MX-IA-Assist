# Módulo 01 — Fundamentos de IA generativa

## Fonte
Curso da Alura: [IA: explorando o potencial da inteligência artificial generativa](https://cursos.alura.com.br/course/ia-explorando-potencial-inteligencia-artificial-generativa). A página informa nível básico, carga horária de 8 horas, transcrição integral e atualização em 08/05/2026.

## Ementa observada
O curso está dividido em quatro aulas: IAs de texto; modelos de linguagem; IAs para análises; e geradores de imagens. A ementa inclui uso de ChatGPT, Google Gemini, Google AI Studio e Maritaca AI; criação, manipulação, revisão e resumo de textos; análise de documentos, imagens, vídeos, áudios e dados; tokens; engenharia de prompt; e geração de imagens.

## Registro da primeira aula
A aula de apresentação posiciona a IA generativa como ferramenta de apoio ao trabalho humano, e não como substituta automática da atividade profissional. O conteúdo enfatiza o uso eficiente de diferentes modelos e a natureza multimodal das aplicações, abrangendo texto, imagem, vídeo e áudio.

## Conhecimento operacional derivado
Para o assistente pessoal local, o módulo deve resultar em um adaptador de modelo capaz de receber uma tarefa, contexto e restrições; selecionar o modo adequado — texto, documento, imagem, áudio ou dados —; produzir uma resposta estruturada; e registrar metadados de execução. A resposta deve ser tratada como saída probabilística que exige validação, especialmente quando alimentar ações, memória ou código.

## Skill proposta: selecionar e usar um modelo generativo

**Entrada:** objetivo da tarefa, contexto autorizado, modalidade dos dados, requisitos de privacidade, limite de latência e formato de saída.

**Processo:** classificar a tarefa; reduzir o contexto ao necessário; definir instruções e critérios de sucesso; escolher modelo local ou provedor externo conforme privacidade e capacidade; solicitar saída estruturada; validar conteúdo e registrar observabilidade.

**Saída:** resposta estruturada, justificativa de seleção, nível de confiança, referências utilizadas quando houver recuperação documental e registro de erros ou limitações.

**Limites:** não executar ações externas sem autorização; não guardar dados sensíveis sem política explícita; não tratar texto gerado como fato sem verificação; não converter automaticamente o conteúdo do curso em treinamento do modelo.

## Aplicação ao projeto MX
O backend Java deve expor uma interface de alto nível para tarefas de IA, enquanto adaptadores isolam OpenAI-compatible APIs, modelos locais e serviços de nuvem. A camada de orquestração deve decidir quando usar geração direta, RAG, ferramenta ou fluxo humano. Métricas mínimas: latência, tokens ou tamanho de contexto, taxa de erro, custo quando aplicável, taxa de respostas rejeitadas na validação e avaliação de qualidade por conjunto de casos.

## Próximos conceitos a consolidar
A próxima análise deve aprofundar o funcionamento de modelos de linguagem, tokens, engenharia de prompt e contexto. Depois, o conteúdo deve ser conectado a RAG, agentes, avaliação, logs e monitoramento, que são necessários para transformar o assistente em um sistema confiável.

## Referências
[1]: https://cursos.alura.com.br/course/ia-explorando-potencial-inteligencia-artificial-generativa "Curso da Alura — IA: explorando o potencial da inteligência artificial generativa"
