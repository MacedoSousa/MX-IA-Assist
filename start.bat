@echo off
REM MX Quick Start - Windows PowerShell

echo.
echo 🚀 Starting MX AI Assistant System
echo.

REM Check Docker
echo Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not installed. Please install Docker Desktop.
    pause
    exit /b 1
)

REM Get script directory
set SCRIPT_DIR=%~dp0

REM Start infrastructure
echo Starting infrastructure (PostgreSQL, Redis, Ollama, MX-Core)...
cd /d "%SCRIPT_DIR%infrastructure\docker\compose"
call docker-compose up -d --build

echo ✓ Infrastructure starting...
echo.

REM Wait for services
echo ⏳ Waiting for services to be ready (30 seconds)...
timeout /t 30 /nobreak

REM Check health
echo.
echo Checking service health...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/health' -ErrorAction Stop; Write-Host '✓ API is ready' -ForegroundColor Green } catch { Write-Host '⚠ API not ready yet' -ForegroundColor Yellow }"

REM Start frontend
echo.
echo Starting frontend server on port 3001...
cd /d "%SCRIPT_DIR%frontend"

REM Try Node.js first
where node >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Starting with Node.js
    start npx http-server -p 3001
) else (
    echo ❌ Node.js not found. Install or use:
    echo    cd %SCRIPT_DIR%frontend
    echo    python -m http.server 3001
)

echo.
echo ================================================================
echo ✅ MX AI Assistant is Ready!
echo ================================================================
echo.
echo 🌐 Access Points:
echo    Application:  http://localhost:3001
echo    API:          http://localhost:8080
echo    WebUI:        http://localhost:3000
echo    Database:     localhost:5432
echo.
echo 📊 Services Running:
echo    ✓ PostgreSQL (port 5432)
echo    ✓ Redis (port 6379)
echo    ✓ Ollama (port 11434)
echo    ✓ Open WebUI (port 3000)
echo    ✓ MX-Core API (port 8080)
echo.
echo 🛑 To stop all services:
echo    cd infrastructure\docker\compose
echo    docker-compose down
echo.
echo 📖 Documentation:
echo    - Full Setup: see IMPLEMENTATION_SUMMARY.md
echo    - API Docs: infrastructure\docker\QUICKSTART.md
echo    - Frontend: frontend\README.md
echo.
echo 🔗 Quick Test (in PowerShell):
echo    curl http://localhost:8080/health
echo.
echo ================================================================
echo.
pause
