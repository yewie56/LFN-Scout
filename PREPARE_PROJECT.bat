@echo off
setlocal EnableExtensions

set "LFN_PREPARE_NO_PAUSE=0"
if /I "%~1"=="--no-pause" set "LFN_PREPARE_NO_PAUSE=1"
if /I "%~1"=="/nopause" set "LFN_PREPARE_NO_PAUSE=1"

echo ============================================================
echo LFN-Scout - Projekt vorbereiten
echo ============================================================
echo.
echo Vor einem Download wird nach einem vorhandenen JDK 17 gesucht.
echo Andere Projekte werden erst nach systemweiten JDKs durchsucht.
echo Das globale Java 8 wird NICHT veraendert.
echo.

rem 1) Java 17 suchen / nur notfalls herunterladen.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\prepare-java17.ps1"
if errorlevel 1 (
    echo.
    echo ============================================================
    echo FAIL: Java-17-Vorbereitung fehlgeschlagen.
    echo ============================================================
    call :maybe_pause
    exit /b 1
)

if not exist ".tools\java17.path" (
    echo FAIL: .tools\java17.path fehlt.
    call :maybe_pause
    exit /b 1
)

set "JAVA17_HOME="
set /p JAVA17_HOME=<".tools\java17.path"

if not defined JAVA17_HOME (
    echo FAIL: Java-17-Pfad ist leer.
    call :maybe_pause
    exit /b 1
)

echo.
echo Verwendetes Java 17:
echo   %JAVA17_HOME%
echo.

rem Java 17 nur fuer diese laufende BAT-Sitzung setzen.
rem Durch SETLOCAL bleibt das globale Java 8 unveraendert.
set "JAVA_HOME=%JAVA17_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Pruefung Java fuer PREPARE_PROJECT:
"%JAVA_HOME%\bin\java.exe" -version
if errorlevel 1 (
    echo.
    echo FAIL: Das gefundene Java 17 konnte nicht gestartet werden.
    call :maybe_pause
    exit /b 1
)
echo.

rem 2) Both Gradle JVM and analyzer-core toolchain must find the SAME JDK 17.
rem    This is needed for Android Studio Gradle Sync, not only CLI builds.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\configure-gradle-jdk17.ps1"
if errorlevel 1 (
    echo.
    echo ============================================================
    echo FAIL: JDK-17-Konfiguration fuer Gradle und Analyzer fehlgeschlagen.
    echo ============================================================
    call :maybe_pause
    exit /b 1
)
echo.

rem 3) Configure machine-local Android SDK for CLI and Android Studio.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "tools\configure-android-sdk.ps1"
if errorlevel 1 (
    echo.
    echo FAIL: Lokale Android-SDK-Konfiguration fehlgeschlagen.
    call :maybe_pause
    exit /b 1
)
echo.

rem 4) Bootstrap-Gradle sicherstellen, OHNE gradlew.bat waehrend
rem    eines Wrapper-Laufs selbst zu ueberschreiben.
rem    --version kann den Bootstrap laden, veraendert gradlew.bat aber nicht.
call gradlew.bat --version >nul

if errorlevel 1 (
    echo.
    echo ============================================================
    echo FAIL: Gradle-Bootstrap konnte nicht vorbereitet werden.
    echo ============================================================
    call :maybe_pause
    exit /b 1
)

if "%GRADLE_USER_HOME%"=="" set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "BB_BOOTSTRAP=%GRADLE_USER_HOME%\brummbeobachter-bootstrap"
set "GRADLE_EXE=%BB_BOOTSTRAP%\gradle-8.13\bin\gradle.bat"

if not exist "%GRADLE_EXE%" (
    echo.
    echo FAIL: Bootstrap-Gradle wurde nicht gefunden:
    echo   %GRADLE_EXE%
    call :maybe_pause
    exit /b 1
)

rem 5) Wrapper direkt ueber Bootstrap-Gradle erzeugen.
rem    PREPARE_PROJECT.bat und der Bootstrap-Prozess werden dabei NICHT ersetzt.
call "%GRADLE_EXE%" wrapper --gradle-version 8.13

if errorlevel 1 (
    echo.
    echo ============================================================
    echo FAIL: Gradle Wrapper konnte nicht vorbereitet werden.
    echo ============================================================
    call :maybe_pause
    exit /b 1
)

rem 6) Wrapper-Task erzeugt eine Standard-gradlew.bat.
rem    Danach unseren Java-17-Launcher bewusst wiederherstellen.
copy /Y "tools\gradlew-local-template.bat" "gradlew.bat" >nul

echo.
echo ============================================================
echo PASS: Projekt vorbereitet.
echo ============================================================
echo Java 17:
echo   %JAVA17_HOME%
echo.
echo Falls Java 17 bereits in einem anderen AndroidStudioProjects-
echo Projekt vorhanden war, wurde es nur referenziert - nicht kopiert.
echo Gradle-Daemon und Analyzer-Toolchain verwenden dasselbe JDK 17.
echo Das globale Java bleibt unveraendert.
echo.
echo Jetzt den Projektordner in Android Studio oeffnen.
call :maybe_pause
exit /b 0

:maybe_pause
if "%LFN_PREPARE_NO_PAUSE%"=="1" exit /b 0
if "%LFN_PREPARE_NO_PAUSE%"=="true" exit /b 0
pause
exit /b 0
