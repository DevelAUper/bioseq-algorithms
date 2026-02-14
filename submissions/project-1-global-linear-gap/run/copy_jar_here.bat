@echo off
setlocal
REM Build jar first (from repo root):
REM   cd java
REM   .\mvnw.cmd -q test
REM   .\mvnw.cmd -q -pl cli -am package

cd /d "%~dp0"
set "SRC_PRIMARY=..\..\..\..\java\cli\target\bioseq-cli.jar"
set "SRC_FALLBACK=..\..\..\java\cli\target\bioseq-cli.jar"
set "DST=bioseq-cli.jar"

REM Preferred path from repository root layout.
if exist "%SRC_PRIMARY%" (
  copy /Y "%SRC_PRIMARY%" "%DST%" >nul
  echo Success: copied "%SRC_PRIMARY%" to "%DST%".
  pause
  exit /b 0
)

REM Fallback path in case folder depth differs.
if exist "%SRC_FALLBACK%" (
  copy /Y "%SRC_FALLBACK%" "%DST%" >nul
  echo Success: copied "%SRC_FALLBACK%" to "%DST%".
  pause
  exit /b 0
)

echo Failed: could not find source jar.
echo Checked:
echo   %SRC_PRIMARY%
echo   %SRC_FALLBACK%
echo Build the CLI jar first:
echo   cd java
echo   .\mvnw.cmd -q test
echo   .\mvnw.cmd -q -pl cli -am package
if exist "..\..\..\..\bioseq-cli.jar" (
  copy /Y "..\..\..\..\bioseq-cli.jar" "%DST%" >nul
  echo Fallback success: copied repo-root bioseq-cli.jar to "%DST%".
  pause
  exit /b 0
)
if exist "..\..\..\bioseq-cli.jar" (
  copy /Y "..\..\..\bioseq-cli.jar" "%DST%" >nul
  echo Fallback success: copied repo-root bioseq-cli.jar to "%DST%".
  pause
  exit /b 0
)
echo Also did not find repo-root bioseq-cli.jar.
pause
exit /b 1
