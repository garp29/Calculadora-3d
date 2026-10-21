@echo off
:: pruebas.bat - Automatización de la base de datos en Windows
cls
echo ========================================================
echo   INICIANDO PRUEBAS DE LA BASE DE DATOS - CALCULADORA
echo ========================================================

echo.
echo [1/4] Creando esquema relacional (create_schema.sql)...
py bd\sql.py bd\calculadora.db -f bd\create_schema.sql

echo.
echo [2/4] Cargando datos semilla del catálogo (seed.sql)...
py bd\sql.py bd\calculadora.db -f bd\seed.sql

echo.
echo [3/4] Ejecutando pruebas de integridad y restricciones (pruebas.sql)...
py bd\sql.py bd\calculadora.db -f bd\pruebas.sql

echo.
echo [4/4] Ejecutando consultas de historial y estadísticas (consultas.sql)...
py bd\sql.py bd\calculadora.db -f bd\consultas.sql

echo ========================================================
echo   PROCESO FINALIZADO CORRECTAMENTE
echo ========================================================
pause
