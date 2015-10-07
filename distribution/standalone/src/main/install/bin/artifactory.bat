@echo off
title Artifactory
echo.
echo Starting Artifactory...
echo.
echo To stop, press Ctrl+c

setlocal

rem defaults
set ARTIFACTORY_HOME=%~dp0..
set CATALINA_HOME=%ARTIFACTORY_HOME%\tomcat
set JAVA_OPTIONS=-server -Xms512m -Xmx2g -Xss256k -XX:+UseG1GC
set CATALINA_OPTS=%JAVA_OPTIONS% -Dartifactory.home="%ARTIFACTORY_HOME%" -Dfile.encoding=UTF8 -Djruby.compile.invokedynamic=false

if not "%JAVA_HOME%" == "" goto javaOk
if not "%JRE_HOME%" == "" goto javaOk
echo Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
echo Will try to guess them from the registry.

:findJDK
FOR /F "usebackq skip=2 tokens=3" %%A IN (`REG QUERY "HKLM\Software\JavaSoft\Java Development Kit" /v CurrentVersion 2^>nul`) DO (set CurVer=%%A)
FOR /F "usebackq skip=2 tokens=3*" %%A IN (`REG QUERY "HKLM\Software\JavaSoft\Java Development Kit\%CurVer%" /v JavaHome 2^>nul`) DO (set JAVA_HOME=%%A %%B)
if not "%JAVA_HOME%" == "" goto javaOk

:findJRE
FOR /F "usebackq skip=2 tokens=3" %%A IN (`REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment" /v CurrentVersion 2^>nul`) DO (set CurVer=%%A)
FOR /F "usebackq skip=2 tokens=3*" %%A IN (`REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment\%CurVer%" /v JavaHome 2^>nul`) DO (set JRE_HOME=%%A %%B)

:javaOk
rem start
"%CATALINA_HOME%\bin\catalina.bat" run

@endlocal