@echo off
setlocal EnableExtensions
chcp 65001 >nul
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

where python >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Python nao encontrado no PATH.
    exit /b 1
)

cd /d "%PROJECT_ROOT%"
echo [MX] Runner de autoextensao iniciado. Ctrl+C encerra o processo.
python "%PROJECT_ROOT%\scripts\mx_evolution_runner.py" --watch --root "%PROJECT_ROOT%"
