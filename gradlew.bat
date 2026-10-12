@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "GRADLE_VERSION=8.13"
set "TOOLS_DIR=%~dp0.tools"
set "JAVA_CACHE=%TOOLS_DIR%\java17.path"

rem ------------------------------------------------------------
rem Java 17 finden oder nur als letzte Moeglichkeit laden
rem ------------------------------------------------------------

if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"

set "JAVA_HOME="

if exist "%JAVA_CACHE%" (
    set /p JAVA_HOME=<"%JAVA_CACHE%"
)

if not defined JAVA_HOME goto find_java
if not exist "%JAVA_HOME%\bin\java.exe" goto find_java
goto java_ready

:find_java
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\prepare-java17.ps1"
if errorlevel 1 (
    echo.
    echo FEHLER: Java 17 konnte nicht gefunden oder bereitgestellt werden.
    exit /b 1
)

set "JAVA_HOME="
if exist "%JAVA_CACHE%" set /p JAVA_HOME=<"%JAVA_CACHE%"

if not defined JAVA_HOME (
    echo FEHLER: Java-17-Pfad wurde nicht gespeichert.
    exit /b 1
)

:java_ready
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [LFN-Scout] Java 17:
echo   %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version
if errorlevel 1 exit /b 1
echo.

rem ------------------------------------------------------------
rem Gradle 8.13 Bootstrap
rem ------------------------------------------------------------

if "%GRADLE_USER_HOME%"=="" set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"

set "BB_BOOTSTRAP=%GRADLE_USER_HOME%\brummbeobachter-bootstrap"
set "GRADLE_HOME=%BB_BOOTSTRAP%\gradle-%GRADLE_VERSION%"
set "GRADLE_EXE=%GRADLE_HOME%\bin\gradle.bat"
set "GRADLE_ZIP=%BB_BOOTSTRAP%\gradle-%GRADLE_VERSION%-bin.zip"

if exist "%GRADLE_EXE%" goto run_gradle

echo [LFN-Scout] Initialisiere Gradle %GRADLE_VERSION% ...
if not exist "%BB_BOOTSTRAP%" mkdir "%BB_BOOTSTRAP%"

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue';" ^
  "$url='https://services.gradle.org/distributions/gradle-8.13-bin.zip';" ^
  "$zip='%GRADLE_ZIP%'; $dest='%BB_BOOTSTRAP%';" ^
  "Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing;" ^
  "Expand-Archive -Path $zip -DestinationPath $dest -Force;" ^
  "Remove-Item $zip -Force"

if errorlevel 1 (
    echo.
    echo FEHLER: Gradle konnte nicht geladen oder entpackt werden.
    exit /b 1
)

:run_gradle
call "%GRADLE_EXE%" %*
exit /b %ERRORLEVEL%
