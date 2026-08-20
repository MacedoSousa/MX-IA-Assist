@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

set "SCRIPT_DIR=%~dp0"
set "COMPOSE_DIR=%SCRIPT_DIR%infrastructure\docker\compose"
set "CLIENT_DIR=%SCRIPT_DIR%clients\mx-app"
set "LAN_IP=127.0.0.1"
set "MX_LAN_IP=%LAN_IP%"

for /f "delims=" %%I in ('powershell -NoProfile -Command "(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254.*' -and $_.PrefixOrigin -ne 'WellKnown' } | Select-Object -First 1 -ExpandProperty IPAddress)"') do set "LAN_IP=%%I"
set "MX_LAN_IP=%LAN_IP%"

call :banner
call :check_command docker "Docker Desktop"
if errorlevel 1 exit /b 1
call :check_command java "Java 21"
if errorlevel 1 exit /b 1
call :check_command node "Node.js"
if errorlevel 1 exit /b 1
call :check_command npm "npm"
if errorlevel 1 exit /b 1
call :check_command powershell "PowerShell"
if errorlevel 1 exit /b 1

if not exist "%COMPOSE_DIR%\docker-compose.yml" (
    echo [ERRO] Compose do MX nao encontrado em "%COMPOSE_DIR%".
    exit /b 1
)
if not exist "%CLIENT_DIR%\package.json" (
    echo [ERRO] Cliente Expo nao encontrado em "%CLIENT_DIR%".
    exit /b 1
)

where ollama >nul 2>&1
if errorlevel 1 (
    echo [AVISO] Ollama nao esta instalado no host; o container Ollama sera usado pelo Compose.
) else (
    echo [OK] Ollama encontrado no host.
)

echo.
echo [1/4] Subindo PostgreSQL, Redis e Ollama pelo Docker Compose...
cd /d "%COMPOSE_DIR%"
docker compose up -d postgres redis ollama open-webui
if errorlevel 1 (
    echo [ERRO] Falha ao subir a infraestrutura Docker.
    exit /b 1
)

call :wait_container postgres
if errorlevel 1 exit /b 1
call :wait_container redis
if errorlevel 1 exit /b 1
call :wait_container ollama
if errorlevel 1 exit /b 1

echo.
echo [2/4] Iniciando o MX Core Spring Boot em uma nova janela...
start "MX Core" cmd /k call "%SCRIPT_DIR%scripts\run-backend.bat"
call :wait_http "http://localhost:8080/actuator/health" "MX Core"
if errorlevel 1 (
    echo [AVISO] O MX Core ainda nao respondeu. Consulte a janela MX Core e logs.
)

echo.
echo [3/4] Preparando o cliente Expo...
if not exist "%CLIENT_DIR%\node_modules\expo\package.json" (
    echo Instalando dependencias do cliente pela primeira vez...
    cd /d "%CLIENT_DIR%"
    call npm install
    if errorlevel 1 (
        echo [ERRO] npm install falhou.
        exit /b 1
    )
)

set "EXPO_PUBLIC_MX_API_URL=http://%LAN_IP%:8080"
echo [4/4] Iniciando Expo Web pela LAN em uma nova janela...
start "MX Expo Web" cmd /k call "%SCRIPT_DIR%scripts\run-expo-web.bat" "%LAN_IP%"

echo.
echo ================================================================
echo MX iniciado para desenvolvimento local
echo ================================================================
echo API local:       http://localhost:8080
echo API pela LAN:    http://%LAN_IP%:8080
echo Expo Web local:  http://localhost:8081
echo Expo Web LAN:    http://%LAN_IP%:8081
echo Open WebUI:      http://localhost:3000
echo.
echo No celular, conectado a mesma rede Wi-Fi, abra:
echo   http://%LAN_IP%:8081
echo.
echo Para parar a infraestrutura:
echo   cd infrastructure\docker\compose
echo   docker compose stop
 echo.
echo As janelas MX Core e MX Expo Web permanecem abertas para logs.
echo ================================================================
exit /b 0

:banner
echo.
echo ================================================================
echo MX AI Assistant - inicializacao local
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

:wait_container
set "SERVICE=%~1"
for /L %%N in (1,1,30) do (
    docker compose ps --status running "%SERVICE%" | findstr /I "%SERVICE%" >nul 2>&1
    if not errorlevel 1 (
        echo [OK] Container %SERVICE% em execucao.
        exit /b 0
    )
    timeout /t 2 /nobreak >nul
)
echo [ERRO] Container %SERVICE% nao ficou em execucao.
exit /b 1

:wait_http
set "HEALTH_URL=%~1"
set "HEALTH_NAME=%~2"
for /L %%N in (1,1,60) do (
    powershell -NoProfile -Command "try { Invoke-WebRequest -UseBasicParsing -Uri '%HEALTH_URL%' -TimeoutSec 2 | Out-Null; exit 0 } catch { exit 1 }" >nul 2>&1
    if not errorlevel 1 (
        echo [OK] %HEALTH_NAME% respondeu em %HEALTH_URL%.
        exit /b 0
    )
    timeout /t 2 /nobreak >nul
)
echo [AVISO] Timeout aguardando %HEALTH_NAME% em %HEALTH_URL%.
exit /b 1
