@echo off
setlocal
REM Optional packaging helper using jpackage.
REM Build jar first (from repo root):
REM   cd java
REM   .\mvnw.cmd -q test
REM   .\mvnw.cmd -q -pl cli -am package

cd /d "%~dp0"

where jpackage >nul 2>&1
if errorlevel 1 (
  echo jpackage not found (requires JDK 16+). You can still run with RUN_GLOBAL_LINEAR.bat.
  pause
  exit /b 0
)

set "JAR=bioseq-cli.jar"
set "DIST=dist"

if not exist "%JAR%" (
  echo Jar not found. Run copy_jar_here.bat or build the project.
  pause
  exit /b 1
)

if not exist "%DIST%" mkdir "%DIST%"

jpackage --type app-image -i . -n BioSeqGlobalLinear --main-jar bioseq-cli.jar --dest "%DIST%"
if errorlevel 1 (
  echo jpackage failed for this jar layout.
  echo If main launcher metadata is missing, keep using java -jar bioseq-cli.jar from the run scripts.
  pause
  exit /b 1
)

echo Built app image under: %DIST%
pause
