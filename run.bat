@echo off
setlocal

if not exist ReprodutorMusica.jar (
    call build.bat
    if errorlevel 1 exit /b 1
)

java -jar ReprodutorMusica.jar
