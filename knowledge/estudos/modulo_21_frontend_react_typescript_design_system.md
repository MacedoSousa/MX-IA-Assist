# Módulo 21 — Frontend, React, TypeScript e Design System

## Fonte

Busca autenticada da Alura: https://cursos.alura.com.br/app/search?query=frontend+React+TypeScript+acessibilidade+design+system+responsivo

## Conteúdos prioritários

| Tema | Conteúdo |
|---|---|
| React e TypeScript | Tipos de props, eventos, estado, componentização, CSS Modules e refatoração |
| Design System | Tokens, componentes, Atomic Design, documentação, handoff e consistência |
| Tailwind/Storybook | Componentes reutilizáveis, variantes, acessibilidade e catálogo visual |
| Turborepo | Monorepo, modularização, publicação e compartilhamento de componentes |
| Versionamento | SemVer, breaking changes, licença, documentação e distribuição |
| Acessibilidade | Navegação por teclado, semântica, contraste, foco, feedback e WCAG |

## Aplicação no MX

O frontend deverá consumir contratos estáveis do MX Core, representar explicitamente estados de carregamento, vazio, erro, parcial e sucesso, e preservar rastreabilidade por `trace_id` sem expor conteúdo sensível. Componentes de chat, documentos, ferramentas e observabilidade devem compartilhar tokens e padrões de acessibilidade.

## Critérios de qualidade

Cada componente deve ter contrato de propriedades, exemplos, teste visual ou de interação, teste de acessibilidade, comportamento responsivo e documentação. Um Design System só será promovido quando reduzir duplicação sem impedir evolução local justificada.

## UX/UI

A implementação deve partir do problema do usuário e não da tecnologia. Fluxos precisam ser prototipados, validados e conectados a critérios de sucesso. Para IA, a interface deve deixar claro o que foi gerado, quais fontes foram usadas, quando a resposta é incerta e quais ações exigem confirmação.
