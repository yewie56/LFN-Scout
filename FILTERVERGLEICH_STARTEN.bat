@echo off
setlocal EnableExtensions
cd /d "%~dp0"
set "SCRIPT=tools\filter_methods_comparison_v0541.py"
set "REPORT=Filtervergleich_v0.5.41"
if not exist "%SCRIPT%" goto missing
if defined CONDA_PREFIX if exist "%CONDA_PREFIX%\python.exe" call :try "%CONDA_PREFIX%\python.exe"
if defined FOUND goto launch
if exist "%USERPROFILE%\anaconda3\python.exe" call :try "%USERPROFILE%\anaconda3\python.exe"
if defined FOUND goto launch
if exist "%USERPROFILE%\miniconda3\python.exe" call :try "%USERPROFILE%\miniconda3\python.exe"
if defined FOUND goto launch
if exist "%LOCALAPPDATA%\anaconda3\python.exe" call :try "%LOCALAPPDATA%\anaconda3\python.exe"
if defined FOUND goto launch
if exist "%LOCALAPPDATA%\miniconda3\python.exe" call :try "%LOCALAPPDATA%\miniconda3\python.exe"
if defined FOUND goto launch
py -3 -c "import numpy,scipy" >nul 2>nul
if not errorlevel 1 (set "RUN_PY=py -3" & set "FOUND=1")
if defined FOUND goto launch
python -c "import numpy,scipy" >nul 2>nul
if not errorlevel 1 (set "RUN_PY=python" & set "FOUND=1")
if defined FOUND goto launch
echo Python 3 with NumPy and SciPy required.
echo Install with: python -m pip install numpy scipy
goto failed
:try
"%~1" -c "import numpy,scipy" >nul 2>nul
if not errorlevel 1 (set "RUN_PY=%~1" & set "ABS=1" & set "FOUND=1")
exit /b 0
:launch
if defined ABS (
  "%RUN_PY%" "%SCRIPT%" --output "%REPORT%"
) else (
  %RUN_PY% "%SCRIPT%" --output "%REPORT%"
)
if errorlevel 1 goto failed
if not exist "%REPORT%\VERGLEICHSBERICHT.md" goto failed
echo PASS: %CD%\%REPORT%\VERGLEICHSBERICHT.md
pause
exit /b 0
:missing
echo Missing %SCRIPT%
:failed
echo FAIL: Comparison report did not complete.
pause
exit /b 1
