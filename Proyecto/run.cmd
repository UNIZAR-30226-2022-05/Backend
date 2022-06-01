@echo off
setlocal enabledelayedexpansion
rem File generated with BuildXMLCreator

set jarFile=dist\Backend_with_dependencies.jar

if exist "%jarFile%" (
    java -Dfile.encoding="UTF-8" -jar "%jarFile%"
) else (
    echo File %jarFile% doesn't exist. Have you executed the compile.cmd script? 2>&1
    pause
)
