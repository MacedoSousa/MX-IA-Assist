# Biblioteca técnica da squad — taxonomia e modelo de conhecimento

## Objetivo
Construir uma biblioteca própria baseada em estudo autorizado, com redação original, referências de origem, exercícios autorais, projetos práticos, skills e avaliações. A biblioteca não reproduz aulas, apostilas, questões ou gabaritos da plataforma.

## Áreas principais

| Código | Área | Escopo |
|---|---|---|
| PROG | Programação e linguagens | Lógica, paradigmas, Clipper/xBase, C, C++, Rust, Go, Java, Kotlin, C#, Python, R, SQL, JavaScript, TypeScript, Swift, Dart, Bash, PowerShell e linguagens declarativas |
| DATA | Dados e computação | SQL, PostgreSQL, NoSQL, modelagem, Big Data, Spark, Lakehouse, streaming, mensageria, computação paralela e distribuída |
| IA | Inteligência artificial | IA generativa, LLMs, prompting, RAG, embeddings, agentes, LangChain, LangGraph, MCP, MLOps e avaliação |
| CLOUD | Cloud, DevOps e infraestrutura | AWS, Azure, Kubernetes, Helm, Terraform, Docker, pipelines, releases, Linux, Windows, DNS, rotas e observabilidade |
| SEC | Segurança | Criptografia, TLS, JWT, identidade, autorização, secrets, AppSec, redes, WAF, SIEM, LGPD e resposta a incidentes |
| PROD | Produto e interfaces | HTML, CSS, React, TypeScript, Design System, Flutter, Android, iOS, UX, UI, acessibilidade e pesquisa |
| QUAL | Qualidade e engenharia | Testes, TDD, integração, contrato, carga, segurança, CI/CD, arquitetura, DDD, Clean Architecture e análise estrutural |
| GEST | Gestão e negócios | Kanban, Scrum, governança, administração, finanças técnicas, custos, capacidade, riscos e planejamento |

## Estrutura de cada módulo

Cada módulo deve conter objetivo, pré-requisitos, conceitos fundamentais, relação entre conceitos, exemplos originais, erros frequentes, exercícios autorais, gabarito explicado, projeto prático, critérios de conclusão, referências de origem, versão e data de revisão.

## Metadados mínimos

| Campo | Finalidade |
|---|---|
| `module_id` | Identificador estável da biblioteca |
| `area` | Área taxonômica |
| `title` | Título original |
| `source_refs` | Links e cursos usados como referência |
| `language_or_stack` | Linguagem, runtime ou tecnologia |
| `version` | Versão do conteúdo |
| `prerequisites` | Dependências de aprendizagem |
| `concepts` | Conceitos cobertos |
| `skills` | Capacidades operacionais geradas |
| `exercises` | Exercícios autorais associados |
| `evaluation` | Métricas e critérios de avaliação |
| `project_ref` | Projeto prático |
| `confidence` | Grau de confiança e evidências |
| `last_reviewed` | Data da revisão |

## Regra de autoria e evidência

A fonte informa o caminho de estudo; a biblioteca registra síntese original, interpretação técnica e aplicação independente. Toda afirmação operacional deve possuir exemplo verificável, teste, referência oficial ou indicação explícita de incerteza. Questões autorais devem avaliar entendimento e não reproduzir perguntas da plataforma.

## Estados do conhecimento

`discovered` indica curso catalogado; `studied` indica conteúdo analisado; `synthesized` indica módulo próprio produzido; `tested` indica exercícios executados; `validated` indica revisão técnica e avaliação; `integrated` indica skill ou contrato incorporado ao assistente.

## Ordem de evolução

A biblioteca será construída por área, começando pelo catálogo, depois pelos fundamentos, exercícios autorais, projetos, skills, avaliações e integração. Um módulo só pode ser promovido a `validated` quando possuir evidência, revisão e critério de regressão.

## Expansão multidisciplinar

| Código | Área ampliada | Escopo |
|---|---|---|
| LIV | Livros e conteúdos editoriais | Título, autor, edição, tema, disponibilidade, resumo original e aplicação; sem reprodução integral |
| GEMP | Gestão empresarial e liderança | Estratégia, liderança, pessoas, cultura, processos, governança e tomada de decisão |
| NEG | Negócios e mercado | Empreendedorismo, marketing, vendas, produto, negociação, atendimento e modelos de negócio |
| FIN | Finanças e administração | Contabilidade, orçamento, fluxo de caixa, custos, indicadores, compras, contratos e planejamento |
| CAR | Carreira e produtividade | Comunicação, apresentação, currículo, produtividade, colaboração, carreira e desenvolvimento pessoal |
| CRI | Educação, criatividade e design | Aprendizagem, criatividade, design, escrita, produção de conteúdo e métodos educacionais |

Para livros e conteúdos editoriais, a biblioteca armazenará metadados e sínteses autorais. O texto integral permanece na plataforma ou na fonte licenciada, e a squad poderá registrar apenas anotações permitidas, referências e aplicações próprias.

Os exercícios e gabaritos da biblioteca serão autorais. Questões da plataforma poderão ser discutidas durante o estudo autorizado, mas não serão coletadas em massa nem reproduzidas como banco de respostas.
