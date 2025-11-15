@echo off
REM FinBasics Underwriter - Run Script
REM This script compiles and runs the application with proper JavaFX module configuration

echo Cleaning and packaging...
call mvn -q clean package

echo.
echo Launching FinBasics Underwriter...
echo.

cd target
java ^
  --add-modules javafx.controls,javafx.fxml ^
  --module-path lib ^
  -cp "classes;lib\*" ^
  com.finbasics.App

pause
