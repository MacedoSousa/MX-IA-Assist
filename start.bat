@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

set "SCRIPT_DIR=%~dp0"
set "COMPOSE_DIR=%SCRIPT_DIR%infrastructure\docker\compose"
set "LAN_IP=127.0.0.1"
set "COMPOSE_BIN=docker-compose"
set "BUILD_ARGS="

for /f "delims=" %%I in ('powershell -NoProfile -Command "(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254.*' -and $_.PrefixOrigin -ne 'WellKnown' } | Select-Object -First 1 -ExpandProperty IPAddress)"') do set "LAN_IP=%%I"

if /I "%~1"=="/build" set "BUILD_ARGS=--build"
if /I "%~1"=="--build" set "BUILD_ARGS=--build"

call :banner
call :check_command docker "Docker CLI"
if errorlevel 1 exit /b 1
call :select_compose
if errorlevel 1 exit /b 1
call :wait_docker
if errorlevel 1 exit /b 1

if not exist "%COMPOSE_DIR%\docker-compose.yml" (
    echo [ERRO] Compose do MX nao encontrado em "%COMPOSE_DIR%".
    exit /b 1
)

if not exist "%SCRIPT_DIR%clients\mx-app\Dockerfile" (
    echo [ERRO] Dockerfile do cliente Expo Web nao encontrado.
    exit /b 1
)

cd /d "%COMPOSE_DIR%"
set "MX_WEB_PORT=8082"
set "EXPO_PUBLIC_MX_API_URL=http://%LAN_IP%:8080"
set "SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS=http://%LAN_IP%:8082"

echo.
echo [1/3] Garantindo que todo o stack MX esteja em execucao...
%COMPOSE_BIN% up -d %BUILD_ARGS%
if errorlevel 1 (
    echo [ERRO] Falha ao subir o stack Docker do MX.
    exit /b 1
)

echo.
echo [2/3] Aguardando PostgreSQL, Redis, Ollama e MX Core...
call :wait_container mx-postgres
if errorlevel 1 exit /b 1
call :wait_container mx-redis
if errorlevel 1 exit /b 1
call :wait_container mx-ollama
if errorlevel 1 exit /b 1
call :wait_http "http://localhost:8080/actuator/health" "MX Core"
if errorlevel 1 exit /b 1

echo.
echo [3/3] Aguardando a interface web oficial do MX...
call :wait_container mx-web
if errorlevel 1 exit /b 1
call :wait_http "http://localhost:8082/health" "MX Web"
if errorlevel 1 exit /b 1

echo.
echo ================================================================
echo MX iniciado integralmente pelo Docker
echo ================================================================
echo MX Web local:    http://localhost:8082
echo MX Web LAN:      http://%LAN_IP%:8082
echo API local:       http://localhost:8080
echo API LAN:         http://%LAN_IP%:8080
echo Open WebUI:      http://localhost:3000
echo Ollama local:    http://localhost:11434
echo.
echo No celular, conectado a mesma rede Wi-Fi, abra:
echo   http://%LAN_IP%:8082
echo.
echo Para reconstruir as imagens explicitamente:
echo   start.bat /build
echo.
echo Para parar somente o stack MX:
echo   cd infrastructure\docker\compose
echo   %COMPOSE_BIN% stop
echo ================================================================
exit /b 0

:banner
echo.
echo ================================================================
echo MX AI Assistant - inicializacao Docker always-on
echo ================================================================
exit /b 0

:check_command
where %~1 >nul 2>&1
if errorlevel 1 (
    echo [ERRO] %~2 nao encontrado no PATH.
    exit /b 1
)
echo [OK] %~2 encontrado.
exit /b 0

:select_compose
docker compose version >nul 2>&1
if not errorlevel 1 (
    set "COMPOSE_BIN=docker compose"
    echo [OK] Docker Compose plugin selecionado.
    exit /b 0
)
where docker-compose >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Nem "docker compose" nem "docker-compose" estao disponiveis.
    exit /b 1
)
set "COMPOSE_BIN=docker-compose"
echo [OK] Docker Compose legado selecionado.
exit /b 0

:wait_docker
for /L %%N in (1,1,90) do (
    docker info >nul 2>&1
    if not errorlevel 1 (
        echo [OK] Docker Desktop respondeu.
        exit /b 0
    )
    if %%N==1 if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
        echo [INFO] Iniciando Docker Desktop...
        start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
    )
    timeout /t 2 /nobreak >nul
)
echo [ERRO] Docker Desktop nao ficou pronto no tempo esperado.
exit /b 1

:wait_container
set "CONTAINER=%~1"
for /L %%N in (1,1,60) do (
    docker ps --filter "name=^%CONTAINER%$" --filter "status=running" | findstr /I "%CONTAINER%" >nul 2>&1
    if not errorlevel 1 (
        echo [OK] %CONTAINER% em execucao.
        exit /b 0
    )
    timeout /t 2 /nobreak >nul
)
echo [ERRO] %CONTAINER% nao ficou em execucao.
exit /b 1

:wait_http
set "HEALTH_URL=%~1"
set "HEALTH_NAME=%~2"
for /L %%N in (1,1,90) do (
    powershell -NoProfile -Command "try { $response = Invoke-WebRequest -UseBasicParsing -Uri '%HEALTH_URL%' -TimeoutSec 3; if ($response.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
    if not errorlevel 1 (
        echo [OK] %HEALTH_NAME% respondeu em %HEALTH_URL%.
        exit /b 0
    )
    timeout /t 2 /nobreak >nul
)
echo [ERRO] Timeout aguardando %HEALTH_NAME% em %HEALTH_URL%.
exit /b 1
