# Módulo 22 — Mobile Android, iOS, Flutter e React Native

## Fonte

Busca autenticada da Alura: https://cursos.alura.com.br/app/search?query=Android+Kotlin+iOS+Swift+Flutter+React+Native+mobile

## Estratégia de estudo

| Caminho | Uso no currículo | Tecnologias |
|---|---|---|
| Flutter | Base multiplataforma com grande cobertura de carreira | Dart, widgets, estado, persistência, testes e offline-first |
| Android nativo | Domínio da plataforma e integração profunda | Kotlin, ciclo de vida, APIs, persistência e testes |
| iOS nativo | Domínio da plataforma Apple | Swift, SwiftUI, estado, navegação, APIs e testes |
| React Native | Reuso de conhecimento React com módulos nativos | TypeScript, Expo, câmera, localização, push e áudio |
| CI/CD mobile | Entrega repetível para Android/iOS | GitHub Actions, Fastlane, Firebase Test Lab e distribuição |

## Aplicação no MX

A primeira entrega mobile deve consumir APIs e eventos versionados do MX, sem duplicar regras de negócio críticas. O cliente deve tolerar rede intermitente, aplicar armazenamento local mínimo, sincronização idempotente, expiração de tokens e tratamento explícito de permissões. Recursos de câmera, localização, áudio e notificações serão ativados por consentimento e finalidade.

## UX móvel

A interface deve respeitar padrões nativos de navegação, acessibilidade, tamanhos de toque, feedback de progresso e estados offline. Respostas de IA devem indicar processamento, permitir cancelamento quando possível e informar fonte, incerteza e impacto de ações.

## Qualidade e publicação

Cada plataforma terá testes unitários, de UI, contrato e regressão em dispositivos ou simuladores representativos. O pipeline deve assinar artefatos, separar ambientes, proteger credenciais, registrar versão e permitir distribuição controlada e rollback quando a loja ou o mecanismo de distribuição permitir.

## Nota

A trilha SwiftUI consultada anteriormente estava temporariamente indisponível; por isso, o estudo iOS será complementado pelos cursos individuais de Swift, testes e desenvolvimento iOS quando localizados na conta.
