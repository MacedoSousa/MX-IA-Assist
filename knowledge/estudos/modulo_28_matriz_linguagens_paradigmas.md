# Módulo 28 — Matriz de linguagens e paradigmas

## Fonte Alura

[Busca autenticada por linguagens e paradigmas](https://cursos.alura.com.br/app/search?query=linguagens+programa%C3%A7%C3%A3o+Java+C%2B%2B+Python+JavaScript+Go+Rust+Clipper+paradigmas)

A busca apresenta trilhas de Rust, Go, C e C++, além de cursos de JavaScript e Python. O levantamento não confirmou um curso específico de Clipper; essa linguagem deverá ser estudada por documentação legada, compatibilidade xBase/Harbour/AdvPL quando aplicável e análise de código fornecido, sem inventar recursos que não estejam comprovados.

## Matriz de estudo

| Família | Linguagens | Paradigma e foco | Aplicação no MX |
|---|---|---|---|
| Legada/xBase | Clipper, dBase, Harbour, xBase compatível | Procedural, banco local, migração | Leitura, manutenção, extração e modernização de sistemas legados |
| Sistemas | C, C++, Rust | Procedural, OO, genérico, ownership e baixo nível | Runtime, integração nativa, desempenho e componentes críticos |
| Backend | Java puro, Kotlin, C#, Go | OO, funcional parcial, concorrência e serviços | Núcleo Spring/Java, APIs, workers, integrações e serviços |
| Dados/IA | Python, R, SQL | Multiparadigma, estatístico e declarativo | RAG, ML, ETL, avaliação e automação de dados |
| Web | JavaScript, TypeScript | Event-driven, funcional e OO | Frontend, Node.js, contratos e ferramentas web |
| Mobile | Kotlin, Swift, Dart | OO, funcional parcial e reativo | Android, iOS e Flutter |
| Scripting/infra | Bash, PowerShell, HCL, YAML, SQL | Automação, declarativo e configuração | Servidores, Terraform, Kubernetes, pipelines e operação |
| Funcional/concorrente | Scala, Elixir, Erlang, Haskell | Funcional, imutabilidade e concorrência | Processamento de eventos, alta disponibilidade e estudo comparativo |
| JVM adicional | Groovy, Scala, Kotlin | JVM, OO e funcional | Automação Gradle, serviços e interoperabilidade Java |

## Método para “todas as linguagens”

O sistema deve conhecer uma linguagem por camadas. A primeira camada registra sintaxe, tipos, controle de fluxo, funções, módulos, tratamento de erros e entrada/saída. A segunda registra memória, concorrência, runtime, compilação, empacotamento e segurança. A terceira registra bibliotecas, padrões, testes, observabilidade e interoperabilidade. A quarta registra versões, limitações, exemplos executáveis e estratégia de migração.

O assistente deve declarar a linguagem, versão, runtime e grau de confiança antes de gerar código. Para Clipper e linguagens pouco documentadas no contexto disponível, deve pedir amostra ou referência, preservar compatibilidade e evitar converter automaticamente sem testes de comportamento.

## Política de geração de código

Código gerado deve incluir dependências, versão mínima, comando de build, testes, tratamento de erro, observabilidade, riscos e licença quando bibliotecas externas forem usadas. O sistema deve distinguir código demonstrativo de código pronto para produção e nunca executar scripts destrutivos sem autorização explícita.
