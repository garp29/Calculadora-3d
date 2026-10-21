@echo off
chcp 65001 > nul
set "JAVA_BIN=C:\Program Files\Apache NetBeans\jdk\bin\java.exe"
if not exist "%JAVA_BIN%" (
    set "JAVA_BIN=java"
)

"%JAVA_BIN%" --enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -cp "out;lib\*" calculadora.Main
pause
