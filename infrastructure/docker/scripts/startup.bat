@echo off
REM MX Infrastructure Startup Script (Windows)
REM This script starts all services for the MX system

setlocal enabledelayedexpansion

echo === Starting MX Infrastructure ===
echo.

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0
REM Go up to infrastructure, then to root
for %%A in ("%SCRIPT_DIR:~0,-1%") do set INFRASTRUCTURE_DIR=%%~dpA
for %%A in ("%INFRASTRUCTURE_DIR:~0,-1%") do set PROJECT_DIR=%%~dpA

set COMPOSE_DIR=%INFRASTRUCTURE_DIR%docker\compose
set ENV_FILE=%INFRASTRUCTURE_DIR%docker\env\.env

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker first.
    exit /b 1
)

REM Check if .env file exists
if not exist "%ENV_FILE%" (
    echo [ERROR] .env file not found at %ENV_FILE%
    exit /b 1
)

echo Working directory: %COMPOSE_DIR%
echo Environment file: %ENV_FILE%
echo.

REM Build mx-core image
echo Building mx-core Docker image...
cd /d "%COMPOSE_DIR%"
docker-compose build mx-core
if errorlevel 1 (
    echo [ERROR] Failed to build mx-core image
    exit /b 1
)

echo.
echo Starting services...
docker-compose up -d

echo.
echo Waiting for services to be ready...
timeout /t 10 /nobreak

echo.
echo Infrastructure Status:
echo.

docker ps | find "mx-postgres" >nul 2>&1
if errorlevel 0 (
    echo   [OK] PostgreSQL running on localhost:5432
) else (
    echo   [FAIL] PostgreSQL not running
)

docker ps | find "mx-redis" >nul 2>&1
if errorlevel 0 (
    echo   [OK] Redis running on localhost:6379
) else (
    echo   [FAIL] Redis not running
)

docker ps | find "mx-ollama" >nul 2>&1
if errorlevel 0 (
    echo   [OK] Ollama running on localhost:11434
) else (
    echo   [FAIL] Ollama not running
)

docker ps | find "mx-open-webui" >nul 2>&1
if errorlevel 0 (
    echo   [OK] Open WebUI running on localhost:3000
) else (
    echo   [FAIL] Open WebUI not running
)

docker ps | find "mx-core" >nul 2>&1
if errorlevel 0 (
    echo   [OK] MX-Core running on localhost:8080
) else (
    echo   [FAIL] MX-Core not running
)

echo.
echo Access points:
echo   * API:       http://localhost:8080/api
echo   * Health:    http://localhost:8080/health
echo   * WebUI:     http://localhost:3000
echo   * Ollama:    http://localhost:11434
echo.
echo View logs:
echo   docker-compose -f "%COMPOSE_DIR%\docker-compose.yml" logs -f
echo.
echo Stop services:
echo   docker-compose -f "%COMPOSE_DIR%\docker-compose.yml" down
echo.

endlocal
