# Skill — Mobile offline-first

## Finalidade
Planejar funcionalidades móveis do MX para conectividade instável, privacidade e experiência consistente em Android, iOS e Flutter/React Native.

## Entradas
Recebe fluxo, plataforma, permissões, dados locais, endpoints, eventos, política de retenção e requisitos de sincronização.

## Procedimento
Define fonte de verdade, cache local, fila de operações, idempotency key, conflitos, retry com backoff, expiração, sincronização e feedback de estado. Verifica permissões de câmera, localização, áudio e notificações; separa segredo de configuração pública; testa offline, retomada, atualização e logout.

## Saída
Retorna contrato mobile, estados de rede, política de sincronização, matriz de permissões, testes e plano de CI/CD.

## Restrições
Não coleta dados além da finalidade autorizada, não armazena tokens de forma insegura e não executa operações irreversíveis sem confirmação e possibilidade de recuperação.
