@echo off
echo ========================================
echo UAAP Event Management System
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java JDK 11 or higher
    pause
    exit /b 1
)

REM Compile Java files first
echo Compiling Java files...
javac -d classes -cp "lib\mysql-connector-j-9.5.0.jar" javaFiles\*.java 2>compile_errors.txt
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed!
    echo Please check compile_errors.txt for details
    type compile_errors.txt
    echo.
    pause
    exit /b 1
)
echo Compilation successful!
echo.

echo Please enter your MySQL Workbench credentials:
echo.
set /p DB_USER="MySQL Username (default: root): "
if "%DB_USER%"=="" set DB_USER=root

set /p DB_PASSWORD="MySQL Password: "
if "%DB_PASSWORD%"=="" (
    echo.
    echo [ERROR] Password cannot be empty!
    echo.
    pause
    exit /b 1
)

echo.
echo Starting UAAP Application...
echo Connecting to database as: %DB_USER%
echo.

REM Run the application from compiled classes with database credentials
java -Ddb.user=%DB_USER% -Ddb.password=%DB_PASSWORD% -cp "classes;lib/*" UAAPApp

REM If there's an error, pause to see the message
if errorlevel 1 (
    echo.
    echo ERROR: Application failed to start
    echo Make sure MySQL is running and database credentials are correct
    pause
)
