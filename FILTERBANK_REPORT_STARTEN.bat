@echo off
setlocal EnableExtensions
cd /d "%~dp0"
set "REPORT=FilterbankReport_v0.5.40"
set "SCRIPT=tools\filterbank_validation_report.py"
if not exist "%SCRIPT%" (
  echo FEHLER: Report-Skript fehlt: %SCRIPT%
  goto :failed
)
echo LFN-Scout v0.5.40 - Filterbank-Report
 echo Suche eine funktionierende Python-Installation ...

rem Bestehende Anaconda/Miniconda-Umgebung bevorzugen.
if defined CONDA_PREFIX if exist "%CONDA_PREFIX%\python.exe" call :try "%CONDA_PREFIX%\python.exe"
if defined FOUND goto :execute
if exist "%USERPROFILE%\anaconda3\python.exe" call :try "%USERPROFILE%\anaconda3\python.exe"
if defined FOUND goto :execute
if exist "%USERPROFILE%\miniconda3\python.exe" call :try "%USERPROFILE%\miniconda3\python.exe"
if defined FOUND goto :execute
if exist "%LOCALAPPDATA%\anaconda3\python.exe" call :try "%LOCALAPPDATA%\anaconda3\python.exe"
if defined FOUND goto :execute
if exist "%LOCALAPPDATA%\miniconda3\python.exe" call :try "%LOCALAPPDATA%\miniconda3\python.exe"
if defined FOUND goto :execute
if exist "%ProgramData%\anaconda3\python.exe" call :try "%ProgramData%\anaconda3\python.exe"
if defined FOUND goto :execute
if exist "%ProgramData%\miniconda3\python.exe" call :try "%ProgramData%\miniconda3\python.exe"
if defined FOUND goto :execute
if exist "C:\Anaconda3\python.exe" call :try "C:\Anaconda3\python.exe"
if defined FOUND goto :execute
if exist "C:\Miniconda3\python.exe" call :try "C:\Miniconda3\python.exe"
if defined FOUND goto :execute

rem Windows Python Launcher falls vorhanden.
where py.exe >nul 2>nul
if not errorlevel 1 (
  py -3 -c "import sys; assert sys.version_info.major == 3" >nul 2>nul
  if not errorlevel 1 (
    set "RUN_PY=py -3"
    set "FOUND=1"
  )
)
if defined FOUND goto :execute

rem PATH-Python nur nach erfolgreichem Starttest akzeptieren (kein Store-Alias).
where python.exe >nul 2>nul
if not errorlevel 1 (
  python -c "import sys; assert sys.version_info.major == 3" >nul 2>nul
  if not errorlevel 1 (
    set "RUN_PY=python"
    set "FOUND=1"
  )
)
if defined FOUND goto :execute

echo.
echo FEHLER: Kein funktionsfaehiges Python 3 gefunden.
echo Falls Anaconda/Spyder installiert ist: Bitte den Installationspfad nennen.
echo Beispiel: C:\Users\BENUTZER\anaconda3\python.exe
 goto :failed

:try
"%~1" -c "import sys; assert sys.version_info.major == 3" >nul 2>nul
if not errorlevel 1 (
  set "RUN_PY=%~1"
  set "PY_ABSOLUTE=1"
  set "FOUND=1"
)
exit /b 0

:execute
echo Python: %RUN_PY%
if defined PY_ABSOLUTE (
  "%RUN_PY%" "%SCRIPT%" --output "%REPORT%"
) else (
  %RUN_PY% "%SCRIPT%" --output "%REPORT%"
)
if errorlevel 1 (
 echo FEHLER: Reportgenerator ist fehlgeschlagen.
 goto :failed
)
if not exist "%REPORT%\VALIDIERUNGSBERICHT.md" (
 echo FEHLER: Reportdatei wurde nicht erstellt.
 goto :failed
)
echo.
echo ERFOLG: Report erstellt:
echo %CD%\%REPORT%\VALIDIERUNGSBERICHT.md
pause
exit /b 0

:failed
echo.
echo Der Report ist NICHT erfolgreich erstellt worden.
pause
exit /b 1
