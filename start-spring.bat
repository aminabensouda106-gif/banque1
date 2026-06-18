@echo off
set "JAVA_HOME=C:\Users\HP\AppData\Local\Programs\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d X:\
call mvnw.cmd spring-boot:run
