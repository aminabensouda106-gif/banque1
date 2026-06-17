@echo off
set "JAVA_HOME=C:\Users\HP\AppData\Local\Programs\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
echo Java:
java -version
echo.
echo Starting Banque Agence on http://localhost:8081 ...
call mvnw.cmd spring-boot:run
