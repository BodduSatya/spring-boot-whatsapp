@echo off
setlocal

REM Define the path to JDK
set "JAVA_HOME=D:\whatsapp4j\jdk-21.0.1"

REM Update the PATH and CLASSPATH environment variables
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "CLASSPATH=%JAVA_HOME%\lib"

REM Define variables
set "SPRING_BOOT_JAR=d:\whatsapp4j\whatsapp-0.0.5-SNAPSHOT.jar"
set "SHUTDOWN_URL=http://localhost/actuator/shutdown"
set "INTERVAL_SECONDS=14400"

REM Check if curl is available
where curl >nul 2>nul
if errorlevel 1 (
    echo curl is not installed. Please install curl to use this script.
    exit /b 1
)

:loop
    echo Starting Spring Boot application...
    REM Start the Spring Boot application
    start "Spring Boot App" java -jar %SPRING_BOOT_JAR% --sqm

    REM Wait for the specified interval
    timeout /t %INTERVAL_SECONDS% /nobreak

    echo Graceful shutdown of Spring Boot application is in progress...
    REM Send a graceful shutdown request
    curl -X POST %SHUTDOWN_URL% --silent --fail --show-error

    REM Wait for a while to allow the application to stop
    timeout /t 15 /nobreak

    echo Application has stopped. Restarting...
    REM Go back to loop
    goto loop