# Infraestrutura Docker local

Esta pasta contém a composição local do MX. Ela fornece PostgreSQL, Redis, Ollama e o backend Spring Boot conforme o arquivo `compose/docker-compose.yml`. O ambiente é de desenvolvimento local; não publique essas portas diretamente na internet.

## Estrutura

```text
infrastructure/docker/
├── compose/docker-compose.yml
├── env/.env.example
└── scripts/
    ├── startup.bat
    ├── startup.sh
    └── prepare-volumes.ps1
```

O arquivo `infrastructure/docker/env/.env` é local e ignorado pelo Git. Use `.env.example` como referência e substitua valores de desenvolvimento conforme a configuração da máquina. Nunca commite senhas, tokens ou chaves reais.

## Serviços

| Serviço | Uso | Porta local típica |
|---|---|---:|
| PostgreSQL | Persistência de identidade, conversas e runs | 5432 |
| Redis | Cache ou capacidades auxiliares quando habilitadas | 6379 |
| Ollama | Gateway de modelo local | 11434 |
| MX Core | API Spring Boot e Actuator | 8080 |
| Open WebUI | Interface opcional para inspeção do Ollama | 3000 |

As portas efetivas são as do Compose. O backend é a única entrada da aplicação MX; o cliente Expo não acessa PostgreSQL, Redis ou Ollama diretamente.

## Iniciar no Windows

```powershell
cd D:\MX\infrastructure\docker\compose
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 mx-core
```

O script `scripts\startup.bat` pode ser utilizado quando o ambiente local estiver configurado para ele. Para desenvolvimento iterativo, também é possível subir somente as dependências e executar o Spring Boot pelo Maven Wrapper, conforme [`docs/operations/local-runbook.md`](../../docs/operations/local-runbook.md).

## Iniciar em Linux/macOS

```bash
cd /path/to/MX/infrastructure/docker/compose
docker compose up -d --build
docker compose ps
```

## Diagnóstico

Use `docker compose ps` para o estado dos containers e `docker compose logs --tail=200 <serviço>` para logs limitados. Verifique PostgreSQL, Ollama e configuração do perfil antes de atribuir uma falha ao cliente. O endpoint Actuator não deve ser exposto publicamente.

Para parar sem destruir dados:

```powershell
docker compose stop
# ou
# docker compose down
```

Não use `docker compose down -v` como diagnóstico padrão, pois isso remove volumes. Backup e restauração ainda são itens de operação em evolução e estão descritos no runbook local.

## Build do MX Core

O container do MX Core deve ser construído a partir do código-fonte do `core-service`. Para validar localmente sem Docker:

```powershell
cd D:\MX\core-service
.\mvnw.cmd -Dmx.build.directory=C:\Windows\Temp\mx-target `
  -Dmaven.repo.local=C:\Windows\Temp\mx-m2 test
```

Builds, caches, bancos, modelos e arquivos `.env` não pertencem ao commit.
