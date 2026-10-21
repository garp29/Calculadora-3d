@echo off
:: pruebas_python.bat - Verificación de contratos de trig.py y graficar.py
chcp 65001 > nul
echo =========================================================================
echo PRUEBAS DE trig.py (Calculo unico, ruido flotante, errores G6 y series)
echo =========================================================================

echo.
echo [1] Caso de exito estandar (modo por omision: grados)
py python\trig.py sin 30

echo.
echo [2] Limpieza de ruido de punto flotante (sin 180 debe dar 0)
py python\trig.py sin 180

echo.
echo [3] Calculo en modo radianes (cos de pi = -1)
py python\trig.py cos 3.141592653589793 --modo radianes

echo.
echo [4] Error por asintota en tan(90) y tan(270)
py python\trig.py tan 90
py python\trig.py tan 270

echo.
echo [5] Error por dominio invalido en inversas (asin 2 y acos -1.5)
py python\trig.py asin 2
py python\trig.py acos -1.5

echo.
echo [6] Error por operacion trigonometrica inexistente
py python\trig.py cot 45

echo.
echo [7] Generacion de serie con hueco en tan(90)
py python\trig.py serie tan 60 120 30 --modo grados

echo.
echo [8] Error de validacion en serie (xmin >= xmax)
py python\trig.py serie sin 180 -180 15

echo.
echo =========================================================================
echo PRUEBAS DE sql.py y graficar.py (Entorno y rutas con espacios)
echo =========================================================================

echo.
echo [9] Verificacion de SQLite con sql.py
py bd\sql.py "bd\calculadora.db" -c "SELECT 1 AS prueba_conexion;"

echo.
echo [10] Invocacion de graficar.py soportando rutas con espacios
py python\graficar.py "bd\calculadora.db" 1 "bd\grafica_serie_1.png"

echo.
echo Todas las pruebas de Python han finalizado.
