@echo off
setlocal

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-cs.ps1" %*
set EXIT_CODE=%ERRORLEVEL%

if not "%EXIT_CODE%"=="0" (
  echo.
  echo Stop failed. Please review the message above.
  pause
)

exit /b %EXIT_CODE%
