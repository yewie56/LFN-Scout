@echo off
setlocal EnableExtensions
cd /d "%~dp0"
call VORTEST.bat --install
exit /b %ERRORLEVEL%
