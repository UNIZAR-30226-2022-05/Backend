@echo off
setlocal enabledelayedexpansion
rem File generated with BuildXMLCreator

set libDir=lib
mkdir %libDir% 1>nul 2>nul

set homeDir=%userprofile%
set baseDir=%userprofile%\.ant
set librariesDir=%baseDir%\cached_libraries
set antDirName=apache-ant
set antDir=%baseDir%\%antDirName%
set antFile=%antDir%\bin\ant
set tempDir=%baseDir%\temp

cd %libDir%

echo Downloading libraries...

if not exist "mailx.jar" (
    if exist "%librariesDir%\mailx.jar" (
        copy "%librariesDir%\mailx.jar" . 1>nul
    ) else (
        java -jar ..\utils\FileDownloader.jar %tempDir%\mailx.jar https://repo1.maven.org/maven2/javax/mail/mail/1.4/mail-1.4.jar
        if not "!errorlevel!"=="0" (
            echo An error occurred while downloading mailx.jar 1>&2
            pause
            exit 1
        ) else (
            mkdir "%librariesDir%" 1>nul 2>nul
            move "%tempDir%\mailx.jar" "%librariesDir%\mailx.jar" 1>nul
            copy "%librariesDir%\mailx.jar" . 1>nul
        )
    )
)
if not exist "activation.jar" (
    if exist "%librariesDir%\activation.jar" (
        copy "%librariesDir%\activation.jar" . 1>nul
    ) else (
        java -jar ..\utils\FileDownloader.jar %tempDir%\activation.jar https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar
        if not "!errorlevel!"=="0" (
            echo An error occurred while downloading activation.jar 1>&2
            pause
            exit 1
        ) else (
            mkdir "%librariesDir%" 1>nul 2>nul
            move "%tempDir%\activation.jar" "%librariesDir%\activation.jar" 1>nul
            copy "%librariesDir%\activation.jar" . 1>nul
        )
    )
)

cd ..

where ant 1>nul 2>nul
if "!errorlevel!"=="0" (
    for /f "delims=" %%i in ('where ant') do set ant=%%i
) else (
    if exist "%antFile%" (
        set ant="%antFile%"
    ) else (
        del /s /f /q "%antDir%" 1>nul 2>nul
        del /s /f /q "%tempDir%\%antDirName%" 1>nul 2>nul
        java -jar utils\FileDownloader.jar -u "%tempDir%\%antDirName%" http://archive.apache.org/dist/ant/binaries/apache-ant-1.10.12-bin.zip
        if not "!errorlevel!"=="0" (
            echo An error occurred while downloading ant 1>&2
            pause
            exit 2
        ) else (
            move "%tempDir%\%antDirName%\apache-ant-1.10.12" "%antDir%" 1>nul
            rd "%tempDir%\%antDirName%"
            set ant="%antFile%"
        )
    )
)

echo Compiling project...
call %ant%
pause
