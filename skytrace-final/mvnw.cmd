@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "MAVEN_HOME=%PROJECT_DIR%.mvn\wrapper\apache-maven-3.9.9"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Maven embarque introuvable: "%MAVEN_HOME%\bin\mvn.cmd"
  exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
