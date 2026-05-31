@echo off
title PennyWise
color 0A
cls
echo.
echo  ==========================================
echo    PennyWise - Personal Finance Manager
echo  ==========================================
echo.

:: Check Java
echo [1/4] Checking Java 21+...
java -version 2>&1 | findstr /i "version" > nul
if %errorlevel% neq 0 (
    echo  ERROR: Java not found!
    echo  Download Java 21 from: https://adoptium.net
    pause & exit /b 1
)
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER=%%g
)
echo  Java found: %JAVA_VER%

:: Check Maven
echo [2/4] Checking Maven...
mvn -version 2>&1 | findstr /i "Apache Maven" > nul
if %errorlevel% neq 0 (
    echo  ERROR: Maven not found!
    echo  Download Maven from: https://maven.apache.org
    pause & exit /b 1
)
echo  Maven found.

:: Clear any bad cached dependencies
echo Clearing bad cached dependencies...
if exist "%USERPROFILE%\.m2\repository\org\kordamp\ikonli\ikonli-fontawesome6-pack" (
    rmdir /s /q "%USERPROFILE%\.m2\repository\org\kordamp\ikonli\ikonli-fontawesome6-pack"
    echo  Cleared bad cache.
)

:: Build
echo.
echo [3/4] Building project (first run takes 2-3 minutes)...
cd /d "%~dp0"
call mvn clean install -DskipTests -q
if %errorlevel% neq 0 (
    echo.
    echo  BUILD FAILED. Trying with verbose output...
    call mvn clean install -DskipTests
    pause & exit /b 1
)
echo  Build successful!

:: NOTE: If you get HTTP 403 errors, delete the old database:
:: del "%USERPROFILE%\.pennywise\pennywise.db"
:: Then restart and register a new account.

:: Start backend
echo.
echo [4/4] Starting Backend on port 8080...
cd /d "%~dp0backend"
start "PennyWise Backend" cmd /k "color 0B && echo  PennyWise Backend && echo  ======================== && mvn spring-boot:run -q"

echo.
echo  Backend starting... waiting 8 seconds for it to initialize.
echo  (Watch the Backend window for: Started BackendApplication)
echo.
timeout /t 8 /nobreak > nul

:: Start UI
echo  Starting UI...
cd /d "%~dp0ui"
mvn javafx:run -q

echo.
echo  UI closed.
pause
