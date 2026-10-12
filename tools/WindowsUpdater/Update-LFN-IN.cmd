@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Update-LFN-IN.ps1" %*
set ERR=%ERRORLEVEL%
echo.
if not "%ERR%"=="0" echo Der Updater wurde mit Fehlercode %ERR% beendet.
pause
exit /b %ERR%
