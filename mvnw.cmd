@REM Maven Wrapper startup batch script for Windows
@echo off
setlocal

set MAVEN_CMD_LINE_ARGS=%*

set WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

if not exist %WRAPPER_JAR% (
    echo Cannot find maven-wrapper.jar at %WRAPPER_JAR%
    exit /b 1
)

@REM Use JAVA_HOME java if available, otherwise fall back to PATH
if defined JAVA_HOME (
    set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
) else (
    set JAVA_CMD=java
)

%JAVA_CMD% -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath %WRAPPER_JAR% %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS%
set MAVEN_EXIT_CODE=%ERRORLEVEL%

endlocal & exit /b %MAVEN_EXIT_CODE%
