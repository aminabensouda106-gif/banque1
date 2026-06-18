@echo off
set "JAVA_HOME=C:\Users\HP\AppData\Local\Programs\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d C:\Users\HP\ONEDRI~1\Bureau\Banque
call "C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.9\8e74001100ff70d6af083c5511fcc5ec49282d7017cde82c3698eee8fdf86698\bin\mvn.cmd" spring-boot:run > C:\Users\HP\ONEDRI~1\Bureau\Banque\spring-boot-run.log 2>&1
