@echo off
set "JAVAC_BIN=C:\Program Files\Apache NetBeans\jdk\bin\javac.exe"
if not exist "%JAVAC_BIN%" (
    set "JAVAC_BIN=javac"
)

if not exist "out" mkdir out

"%JAVAC_BIN%" -encoding UTF-8 -cp "lib\*" -d out src\calculadora\modelo\*.java src\calculadora\dao\*.java src\calculadora\vista\*.java src\calculadora\Main.java

if errorlevel 1 (
    echo [ERROR] La compilacion fallo.
) else (
    echo [OK] Compilacion exitosa en la carpeta out\
)
