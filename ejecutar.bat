@echo off
setlocal enabledelayedexpansion
title Ciudad y Conduccion - OpenGL LWJGL

cd /d "%~dp0"

if not "%~1"=="" (
    if "%~1"=="1" goto run_c1
    if "%~1"=="2" goto run_c2
    if "%~1"=="3" goto run_c3
    if "%~1"=="4" goto run_c4
    if /i "%~1"=="test" goto run_test
    if /i "%~1"=="package" goto run_package
    goto uso
)

:menu
cls
echo ==============================================================
echo            CIUDAD Y CONDUCCION CON OPENGL (LWJGL 3)
echo ==============================================================
echo.
echo   [1] Clase 1: Ciudad 3D, mapa y renderizador
echo   [2] Clase 2: Auto, conduccion, camara y colisiones
echo   [3] Clase 3: Iluminacion (sol, noche, farolas y focos)
echo   [4] Clase 4: Juego completo (entregas y minimapa) [POR DEFECTO]
echo   [5] Ejecutar pruebas unitarias (mvn test)
echo   [6] Empaquetar JAR con dependencias (mvn package)
echo   [7] Salir
echo.
echo ==============================================================
set /p opc="Selecciona una opcion [1-7] (Presiona Enter para 4): "
if "%opc%"=="" set opc=4

if "%opc%"=="1" goto run_c1
if "%opc%"=="2" goto run_c2
if "%opc%"=="3" goto run_c3
if "%opc%"=="4" goto run_c4
if "%opc%"=="5" goto run_test
if "%opc%"=="6" goto run_package
if "%opc%"=="7" exit /b 0

echo Opcion invalida.
timeout /t 2 >nul
goto menu

:run_c1
echo.
echo [INFO] Iniciando Clase 1...
call mvn compile exec:exec -Pclase1
goto fin

:run_c2
echo.
echo [INFO] Iniciando Clase 2...
call mvn compile exec:exec -Pclase2
goto fin

:run_c3
echo.
echo [INFO] Iniciando Clase 3...
call mvn compile exec:exec -Pclase3
goto fin

:run_c4
echo.
echo [INFO] Iniciando Clase 4 (Juego Completo)...
call mvn compile exec:exec -Pclase4
goto fin

:run_test
echo.
echo [INFO] Ejecutando pruebas unitarias...
call mvn test
goto fin

:run_package
echo.
echo [INFO] Generando JAR ejecutable en target/...
call mvn package
goto fin

:uso
echo Uso: ejecutar.bat [1^|2^|3^|4^|test^|package]
goto fin

:fin
echo.
echo Presiona una tecla para continuar...
pause >nul
