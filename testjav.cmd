@echo off 
for %%%%i in (java.exe) do set JAVACMD=%%%%~ 
echo JAVACMD=%%JAVACMD%% 
if exist \" "%%JAVACMD%%\ echo EXISTS 
