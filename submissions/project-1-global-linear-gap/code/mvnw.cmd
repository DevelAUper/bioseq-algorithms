@echo off
setlocal

if "%JAVA_HOME%"=="" (
  echo The JAVA_HOME environment variable is not defined correctly 1>&2
  echo This environment variable is needed to run this program 1>&2
  exit /B 1
)

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_LAUNCHER=%WRAPPER_DIR%\MavenWrapperDownloader.java

if not exist "%WRAPPER_JAR%" (
  if not exist "%WRAPPER_LAUNCHER%" (
    echo MavenWrapperDownloader.java is missing. 1>&2
    exit /B 1
  )
  "%JAVA_HOME%\bin\javac.exe" -d "%WRAPPER_DIR%" "%WRAPPER_LAUNCHER%"
  if ERRORLEVEL 1 exit /B 1
  "%JAVA_HOME%\bin\java.exe" -cp "%WRAPPER_DIR%" MavenWrapperDownloader
  if ERRORLEVEL 1 exit /B 1
)

"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /B %ERRORLEVEL%
