@echo off
setlocal
cd /d "%~dp0"
REM Build jar (from repo root) if needed:
REM   cd java
REM   .\mvnw.cmd -q test
REM   .\mvnw.cmd -q -pl cli -am package

set "JAR=bioseq-cli.jar"
set "OUT_DIR=output"
set "FA1=examples\seq1.fa"
set "FA2=examples\seq2.fa"
set "MATRIX=examples\dna_matrix.txt"
set "OUT_FILE=%OUT_DIR%\global_count_example.txt"

if not exist "%JAR%" (
  echo Jar not found. Run copy_jar_here.bat or build the project.
  pause
  exit /b 1
)

java -version >nul 2>&1
if errorlevel 1 (
  echo Java is not installed or not on PATH.
  echo Install Java, reopen this window, and try again.
  pause
  exit /b 1
)

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

java -jar "%JAR%" global_count --fasta1 "%FA1%" --fasta2 "%FA2%" --matrix "%MATRIX%" --gap 2 --out "%OUT_FILE%"
if errorlevel 1 (
  echo global_count failed.
  pause
  exit /b 1
)

echo Done. Wrote: %OUT_FILE%
echo Hint: for custom inputs, run:
echo   java -jar bioseq-cli.jar global_count --fasta1 your1.fa --fasta2 your2.fa --matrix your_matrix.txt --gap 5 --out output\your_count.txt
pause
