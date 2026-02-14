@echo off
setlocal
set SCRIPT_DIR=%~dp0
set ROOT_DIR=%SCRIPT_DIR%..
set JAR=%ROOT_DIR%\java\cli\target\bioseq-cli.jar

if not exist "%JAR%" (
  echo Missing jar: %JAR%
  echo Build first with: cd java ^&^& .\mvnw.cmd -q package
  exit /b 1
)

java -jar "%JAR%" global_linear --fasta1 "%ROOT_DIR%\data\fasta\a.fa" --fasta2 "%ROOT_DIR%\data\fasta\b.fa" --matrix "%ROOT_DIR%\data\matrices\dna_example.txt" --gap 2 --traceback
endlocal
