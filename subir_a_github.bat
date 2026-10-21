@echo off
set "PATH=C:\Users\Gabriel\AppData\Local\Programs\Git\cmd;%PATH%"
echo ========================================================
echo   SUBIENDO PROYECTO A GITHUB (garp29/Calculadora-3d)
echo ========================================================
echo.
git push origin main
echo.
if errorlevel 1 (
    echo [ERROR] No se pudo completar el push. Asegurate de iniciar sesion en el navegador.
) else (
    echo [OK] Repositorio actualizado exitosamente en GitHub:
    echo https://github.com/garp29/Calculadora-3d
)
echo.
pause
