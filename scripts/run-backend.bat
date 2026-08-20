@echo off
setlocal EnableExtensions
chcp 65001 >nul

set "SCRIPT_DIR=%~dp0..\"
set "LAN_IP=%MX_LAN_IP%"
if "%LAN_IP%"=="" set "LAN_IP=127.0.0.1"

set "APP_PROFILE=dev"
set "SERVER_ADDRESS=0.0.0.0"
set "OLLAMA_URL=http://localhost:11434"
set "MX_WORKSPACE_ROOT=%SCRIPT_DIR%"
set "SECURITY_CORS_ALLOWED_ORIGINS=http://localhost:8081,http://127.0.0.1:8081,http://%LAN_IP%:8081,http://localhost:19006,http://%LAN_IP%:19006"
set "SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS="

cd /d "%SCRIPT_DIR%core-service"
if errorlevel 1 (
    echo [ERRO] Nao foi possivel acessar o core-service.
    exit /b 1
)

echo MX Core: iniciando Spring Boot com perfil dev...
echo API: http://localhost:8080
echo CORS LAN: http://%LAN_IP%:8081
call mvnw.cmd -Dmx.build.directory=C:\Windows\Temp\mx-target -Dmaven.repo.local=C:\Windows\Temp\mx-m2 spring-boot:run
set "EXIT_CODE=%errorlevel%"
echo MX Core encerrado com codigo %EXIT_CODE%.
exit /b %EXIT_CODE%
