@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "LFN_INSTALL=0"
set "LFN_NO_PAUSE=0"

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--install" set "LFN_INSTALL=1"
if /I "%~1"=="/install" set "LFN_INSTALL=1"
if /I "%~1"=="--no-pause" set "LFN_NO_PAUSE=1"
if /I "%~1"=="/nopause" set "LFN_NO_PAUSE=1"
shift
goto parse_args

:args_done
echo ============================================================
echo LFN-Scout - automatischer Windows-Vortest
echo ============================================================
echo.
echo Ablauf:
echo   1. PREPARE_PROJECT.bat --no-pause
echo   2. gradlew.bat :app:testDebugUnitTest --stacktrace
echo   3. gradlew.bat assembleDebug --stacktrace
echo   4. APK pruefen und nach Builds kopieren
echo   5. ACTIVE-Projekt-ZIP erzeugen, pruefen und SHA-256 bilden
echo   6. Vollstaendiges Log und LAST_VORTEST_RESULT.txt schreiben
if "%LFN_INSTALL%"=="1" echo   7. Auf genau ein freigegebenes ADB-Geraet warten und APK installieren
if "%LFN_INSTALL%"=="0" echo   Optional: VORTEST.bat --install installiert nach PASS auf dem Handy.
echo.

if "%LFN_INSTALL%"=="1" (
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\run-vortest.ps1" -Install
) else (
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\run-vortest.ps1"
)
set "VORTEST_RC=%ERRORLEVEL%"

echo.
if "%VORTEST_RC%"=="0" (
    echo ============================================================
    echo PASS: Vortest, Debug-Build und ACTIVE-Paket erfolgreich.
    echo Ergebnis: Builds\LAST_VORTEST_RESULT.txt
    echo APK/ACTIVE: siehe Builds\LAST_VORTEST_RESULT.txt
    if "%LFN_INSTALL%"=="1" echo INSTALL:   APK wurde per ADB installiert.
    echo ============================================================
) else (
    echo ============================================================
    echo FAIL: Vortest abgebrochen. Fehlercode %VORTEST_RC%
    echo Details: Builds\LAST_VORTEST_RESULT.txt und VORTEST-Log
    echo ============================================================
)

if "%LFN_NO_PAUSE%"=="0" pause
exit /b %VORTEST_RC%
