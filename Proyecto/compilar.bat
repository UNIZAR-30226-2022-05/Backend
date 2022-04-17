@echo off

IF not exist mvnw.cmd (
    echo Error: El archivo "mvnw.cmd" no existe
    echo Has colocado "compilar.bat" en el directorio del proyecto?
    pause
    exit 1
)

title Compilando backend...
call mvnw package

if NOT "%ERRORLEVEL%" == "0" (
    title Error en la compilacion
    pause
    exit /B 1
)

title Compilacion completada
echo.
echo.
echo.
pause

rem El archivo generado estará en la carpeta 'target'
rem     ¡No olvidarse de añadirla al gitignore!


rem Para limpiar los archivos de compilación, usar: mvnw clean
rem Para recompilar todo y empaquetar, usar: mvnw clean package
