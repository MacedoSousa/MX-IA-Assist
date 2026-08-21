@echo off
setlocal EnableExtensions
chcp 65001 >nul
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

set "PYTHON="
if exist "%LOCALAPPDATA%\Programs\Python\Python312\python.exe" set "PYTHON=%LOCALAPPDATA%\Programs\Python\Python312\python.exe"
if not defined PYTHON if exist "%LOCALAPPDATA%\Programs\Python\Python311\python.exe" set "PYTHON=%LOCALAPPDATA%\Programs\Python\Python311\python.exe"
if not defined PYTHON (
    py -3.12 --version >nul 2>&1
    if not errorlevel 1 set "PYTHON=py -3.12"
)
if not defined PYTHON (
    python --version >nul 2>&1
    if not errorlevel 1 set "PYTHON=python"
)
if not defined PYTHON (
    echo [ERRO] Python 3.11+ nao encontrado. Instale Python 3.12 ou ajuste o PATH.
    exit /b 1
)

cd /d "%PROJECT_ROOT%"
echo [MX] Runner de autoextensao iniciado. Ctrl+C encerra o processo.
if /I "%~1"=="--once" (
    shift
    %PYTHON% "%PROJECT_ROOT%\scripts\mx_evolution_runner.py" --once --root "%PROJECT_ROOT%" %*
) else (
    %PYTHON% "%PROJECT_ROOT%\scripts\mx_evolution_runner.py" --watch --root "%PROJECT_ROOT%" %*
)
