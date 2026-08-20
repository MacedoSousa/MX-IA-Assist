# Módulo 02 — Python, GPT e chatbots

## Fonte
Curso da Alura: [Python e GPT: crie seu chatbot com IA](https://cursos.alura.com.br/course/python-gpt-crie-chatbot-com-ia). A página informa nível intermediário, carga horária de 8 horas, transcrição integral e atualização em 30/07/2024.

## Conteúdo observado
O curso propõe integrar um chatbot a APIs compatíveis com OpenAI usando Python. A sequência aborda integração com front-end, preparação de ambiente virtual, criação do bot, contexto de domínio, seleção de documentos, personas, histórico de conversação, threads, arquivos associados, Functions Calling, alertas e interpretação de imagens com visão computacional.

## Conhecimento aplicado
O curso oferece uma referência direta para separar o assistente em camadas. A interface recebe a solicitação; o orquestrador identifica contexto, persona e histórico; o adaptador chama o modelo; e as ferramentas executam operações externas mediante contratos explícitos. O histórico não deve ser enviado integralmente sem controle: é necessário limitar contexto, resumir conversas antigas e aplicar políticas de retenção.

## Skill proposta: orquestrar uma conversa com contexto

**Entrada:** mensagem do usuário, identificador da sessão, contexto de domínio, documentos autorizados e ferramentas disponíveis.

**Processo:** validar a sessão; classificar a intenção; selecionar contexto mínimo; recuperar documentos quando necessário; montar mensagens com papéis e instruções; solicitar resposta estruturada; validar a saída; persistir apenas o histórico permitido.

**Saída:** resposta para o usuário, eventos de auditoria, referências recuperadas e atualização controlada da memória.

**Limites:** ferramentas precisam de esquema, validação de argumentos, autorização, timeout, idempotência e tratamento de erro. Imagens, arquivos e identificadores de usuário devem ter armazenamento temporário, expiração e controle de acesso.

## Arquitetura sugerida para o projeto MX
A implementação deve manter um contrato Java para `ConversationRequest`, `ConversationResponse`, `ToolDefinition`, `ToolCall` e `RetrievedContext`. Um serviço Python pode ser usado para experimentos de RAG ou visão, mas não deve receber credenciais do usuário nem assumir responsabilidades de autenticação. A comunicação entre componentes deve usar HTTP ou mensageria com correlação, timeout e logs estruturados.

## Testes mínimos
Os casos de teste devem cobrir: pergunta sem contexto; pergunta dependente de documento; histórico longo; ferramenta com argumento inválido; indisponibilidade do provedor; imagem incompatível; tentativa de acesso fora da permissão; repetição da mesma solicitação; e resposta sem evidência suficiente. A avaliação deve medir precisão funcional, latência, taxa de erro, segurança da ferramenta e qualidade da resposta.

## Relação com o curso de fundamentos
O curso anterior apresenta modelos e modalidades. Este curso transforma esses fundamentos em sistema: contexto, histórico, ferramentas e visão. A próxima camada necessária é RAG formal, agentes com estado e observabilidade, evitando que o chatbot seja apenas uma chamada direta a uma API.

## Referências
[1]: https://cursos.alura.com.br/course/python-gpt-crie-chatbot-com-ia "Curso da Alura — Python e GPT: crie seu chatbot com IA"
