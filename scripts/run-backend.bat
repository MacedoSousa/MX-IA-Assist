@echo off
setlocal EnableExtensions
chcp 65001 >nul

set "SCRIPT_DIR=%~dp0..\"
set "LAN_IP=%MX_LAN_IP%"
if "%LAN_IP%"=="" set "LAN_IP=127.0.0.1"

set "APP_PROFILE=dev"
if not defined SERVER_ADDRESS set "SERVER_ADDRESS=0.0.0.0"
if not defined SERVER_PORT set "SERVER_PORT=8080"
if not defined OLLAMA_URL set "OLLAMA_URL=http://127.0.0.1:11435"
if not defined MX_WORKSPACE_ROOT set "MX_WORKSPACE_ROOT=%SCRIPT_DIR%workspaces"
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=dev,local-windows"
if not defined MX_LOG_FILE set "MX_LOG_FILE=%SCRIPT_DIR%logs\mx-core.log"

set "LOCAL_ENV=%SCRIPT_DIR%scripts\local\mx-local.env"
if not exist "%LOCAL_ENV%" (
  echo Arquivo de configuracao local ausente: %LOCAL_ENV%
  echo Execute scripts\initialize-mx-local-db.ps1 antes de iniciar o MX Core.
  exit /b 2
)
for /f "usebackq tokens=1,* delims==" %%A in ("%LOCAL_ENV%") do set "%%A=%%B"
set "SECURITY_CORS_ALLOWED_ORIGINS=http://localhost:8081,http://127.0.0.1:8081,http://%LAN_IP%:8081,http://localhost:19006,http://%LAN_IP%:19006"
set "SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS="

cd /d "%SCRIPT_DIR%core-service"
if errorlevel 1 (
    echo [ERRO] Nao foi possivel acessar o core-service.
    exit /b 1
)

echo MX Core: iniciando Spring Boot com perfil %SPRING_PROFILES_ACTIVE%...
echo API: http://localhost:%SERVER_PORT%
echo CORS LAN: http://%LAN_IP%:8081
call mvnw.cmd -Dmx.build.directory=C:\Windows\Temp\mx-target -Dmaven.repo.local=C:\Windows\Temp\mx-m2 spring-boot:run
set "EXIT_CODE=%errorlevel%"
echo MX Core encerrado com codigo %EXIT_CODE%.
exit /b %EXIT_CODE%
