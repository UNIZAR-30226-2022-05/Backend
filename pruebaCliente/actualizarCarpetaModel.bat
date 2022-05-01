@echo off
set dir=%CD%
cd src\main\java\es\unizar\unoforall
set androidDir=%CD%
rmdir /S /Q model
cd %dir%
cd ..\Proyecto\src\main\java\es\unizar\unoforall
xcopy /E /I "%CD%\model" "%androidDir%\model"
pause
