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

echo Please enter your MySQL database credentials:
echo.
set /p DB_USER="Enter MySQL Username (default: root): "
set /p DB_PASSWORD="Enter MySQL Password: "

REM Use default values if empty
if "%DB_USER%"=="" set DB_USER=root
if "%DB_PASSWORD%"=="" set DB_PASSWORD=Dlsu1234!

echo.
echo Starting UAAP Application...
echo Connecting to database as user: %DB_USER%
echo.

REM Run the application from compiled classes with database credentials
java -Ddb.user=%DB_USER% -Ddb.password=%DB_PASSWORD% -cp "classes;lib/mysql-connector-j-9.5.0.jar" UAAPApp

REM If there's an error, pause to see the message
if errorlevel 1 (
    echo.
    echo ERROR: Application failed to start
    echo Make sure MySQL is running and database credentials are correct
    pause
)
