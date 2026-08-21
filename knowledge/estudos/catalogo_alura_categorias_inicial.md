# Catálogo autenticado da Alura — levantamento inicial

A interface autenticada exibiu os tipos de conteúdo **Carreiras, Trilhas, Cursos, Livros, Artigos, Podcasts e Vídeos Extras**. As categorias visíveis foram **Back-end, Front-end, Dados, Inteligência Artificial, DevOps, Cibersegurança, Cloud, UX & Design, Mobile e Gestão & Negócios**. Também aparecem filtros de nível **Básico, Intermediário e Avançado**.

A biblioteca da squad será mais ampla que os filtros de tecnologia. Ela incluirá os conteúdos técnicos e, dentro de Gestão & Negócios e dos tipos editoriais, registrará liderança, administração, finanças, empreendedorismo, marketing, vendas, carreira, produtividade, comunicação, educação e criatividade quando encontrados no acesso autorizado.

O inventário deverá manter o tipo original do conteúdo, a URL, título, autor ou instrutor quando disponível, categoria, nível, status de acesso, data de coleta, resumo original, competências e referência para a fonte. A ausência de um resultado em uma busca não será interpretada como inexistência do conteúdo; será registrada como necessidade de nova consulta ou confirmação.

## Busca autenticada de gestão, empreendedorismo e finanças

A busca autenticada retornou **1.708 resultados** para a consulta sobre gestão empresarial, liderança, empreendedorismo, finanças e livros. A interface exibiu cursos, carreiras, podcasts, trilhas Alura e vídeos extras, além dos filtros de conteúdos, categorias e níveis.

Exemplos observados incluem o curso `Gestão financeira: administre suas finanças na prática`, a carreira `Liderança` com 22 cursos e 161 horas, `Liderança: práticas de gestão e melhorias`, o podcast `Mesa de Produto #53 — Empreendedorismo e Gestão de Produto`, as trilhas `Empreendedorismo Digital`, `Empreendedorismo para Devs`, `Excel Finanças`, `Liderança Comercial 4.0` e cursos de perfis de liderança, gestão da mudança, produtos e plano de negócios.

Esse número é o resultado de uma consulta textual específica, não a contagem total do catálogo. A biblioteca deve registrar a consulta, a data e a paginação para evitar tratar o resultado como inventário completo.

## Verificação do filtro de livros

Ao selecionar o filtro `Livros` para a consulta composta, a plataforma exibiu “nenhum resultado encontrado”. Ao refinar o campo de busca para `livros`, a interface manteve a consulta anterior nos resultados e mostrou a sugestão de pesquisa com IA. Isso indica que o filtro editorial pode depender de indexação, consulta específica ou rota própria; não é evidência suficiente de que não existam livros. O inventário deverá usar o filtro e o tipo de conteúdo separadamente, registrar a URL observada e confirmar resultados com consultas mais específicas.

A busca textual continuou exibindo cursos, carreiras, trilhas, podcasts e vídeos extras, mas não livros explícitos nos primeiros resultados. A biblioteca registrará essa limitação como `needs_confirmation` em vez de inferir ausência do acervo.

## Busca autenticada por livros e conteúdos relacionados

A consulta textual `livros` retornou **788 resultados**. Entre os primeiros itens observados estavam o podcast `Grandes livros de Tecnologia — Hipsters #113`, os artigos `Livros: escolhendo a trindade do desenvolvedor Java`, `Conheça livros de cabeceira dos Devs (parte 1 e parte 2)`, `Conheça os criadores do Decifre o livro`, `Livros de tecnologia que amamos — Hipsters Ponto Tech #495` e `7 livros imperdíveis sobre Marketing e Marketing Digital`.

A mesma busca também retornou carreiras técnicas, como `Desenvolvimento Back-End Node.js` com 51 cursos e 542 horas, `Especialista em IA` com 25 cursos e 203 horas, `Engenharia de Agentes de IA` com 27 cursos e 234 horas e `UI Design` com 25 cursos e 184 horas. Esses resultados demonstram que a busca textual combina livros, artigos, podcasts, carreiras e cursos; por isso, cada item deverá ser classificado pelo seu tipo original.

Fonte consultada: https://cursos.alura.com.br/app/search?query=livros
