# Base Mintlify local

Esta pasta contém a documentação navegável do MX em formato Mintlify. Ela é **local e não publicada**: nenhum domínio, conta, token, organização ou repositório externo foi presumido nem configurado.

O arquivo `docs.json` usa a configuração atual do Mintlify, que deve permanecer na raiz do diretório de documentação e definir nome, tema, cor primária e navegação.[1] A estrutura adota abas paralelas PT-BR/English, recurso suportado para organizar áreas de navegação independentes.[2]

Quando houver uma conta Mintlify configurada de forma explícita, a validação deve usar a CLI oficial no diretório desta pasta. Não usar `--force` em diretórios com conteúdo existente; executar `mint validate` e `mint broken-links` antes de qualquer push/publicação.[3]

## Referências

[1] [Mintlify — Global settings](https://www.mintlify.com/docs/organize/settings)

[2] [Mintlify — Navigation](https://www.mintlify.com/docs/organize/navigation)

[3] [Mintlify — Quickstart](https://www.mintlify.com/docs/quickstart)
