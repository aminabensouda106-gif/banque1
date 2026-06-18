@echo off
set "JAVA_HOME=C:\Users\HP\AppData\Local\Programs\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set MVNW_VERBOSE=true
cd /d X:\
call mvnw.cmd -version > X:\mvn-verbose-out.txt 2>&1
