@echo off
setlocal EnableExtensions
chcp 65001 >nul

set "LAN_IP=%~1"
if "%LAN_IP%"=="" set "LAN_IP=127.0.0.1"
set "EXPO_PUBLIC_MX_API_URL=http://%LAN_IP%:8080"

cd /d "%~dp0..\clients\mx-app"
if errorlevel 1 (
    echo [ERRO] Nao foi possivel acessar o cliente Expo.
    exit /b 1
)

echo MX Expo Web: iniciando pela LAN...
echo URL esperada: http://%LAN_IP%:8081
call npx expo start --web --lan
set "EXIT_CODE=%errorlevel%"
echo MX Expo Web encerrado com codigo %EXIT_CODE%.
exit /b %EXIT_CODE%
