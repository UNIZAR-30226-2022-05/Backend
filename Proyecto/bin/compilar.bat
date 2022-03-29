@echo off

IF not exist mvnw.cmd (
    echo Error: El archivo "mvnw.cmd" no existe
    echo Has colocado "compilar.bat" en el directorio del proyecto?
    pause
    exit 1
)

start mvnw package

rem El archivo generado estará en la carpeta 'target'
rem     ¡No olvidarse de añadirla al gitignore!


rem Para limpiar los archivos de compilación, usar: mvnw clean
rem Para recompilar todo y empaquetar, usar: mvnw clean package
