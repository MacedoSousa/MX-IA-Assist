@echo off
setlocal EnableExtensions
set "PROJECT_ROOT=%~dp0.."
set "PYTHON_EXE="

where py >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  for /f "delims=" %%P in ('py -3.12 -c "import sys; print(sys.executable)" 2^>nul') do set "PYTHON_EXE=%%P"
)
if not defined PYTHON_EXE (
  for /f "delims=" %%P in ('py -3.11 -c "import sys; print(sys.executable)" 2^>nul') do set "PYTHON_EXE=%%P"
)
if not defined PYTHON_EXE (
  for /f "delims=" %%P in ('where python 2^>nul') do if not defined PYTHON_EXE set "PYTHON_EXE=%%P"
)
if not defined PYTHON_EXE (
  echo Python 3.11+ nao encontrado.
  exit /b 2
)

if not defined MX_MAINTENANCE_INTERVAL_SECONDS set "MX_MAINTENANCE_INTERVAL_SECONDS=21600"
cd /d "%PROJECT_ROOT%"
"%PYTHON_EXE%" "%PROJECT_ROOT%\scripts\mx_maintenance_agent.py" --watch --interval-seconds %MX_MAINTENANCE_INTERVAL_SECONDS%
exit /b %ERRORLEVEL%
