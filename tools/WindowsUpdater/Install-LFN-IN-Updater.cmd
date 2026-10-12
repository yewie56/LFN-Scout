@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Install-LFN-IN-Updater.ps1"
set ERR=%ERRORLEVEL%
echo.
if not "%ERR%"=="0" echo Installation wurde mit Fehlercode %ERR% beendet.
pause
exit /b %ERR%
