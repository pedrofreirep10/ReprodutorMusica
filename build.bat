@echo off
setlocal

set "dependencia=lib\jlayer-1.0.1.jar"
set "classes=build\classes"
set "jarfinal=ReprodutorMusica.jar"
set "javaccmd=javac"
set "jarcmd=jar"

if not exist "%dependencia%" (
    echo erro: arquivo %dependencia% nao encontrado.
    exit /b 1
)

set "javaclocal="
for /f "delims=" %%i in ('where javac 2^>nul') do if not defined javaclocal set "javaclocal=%%i"
if defined javaclocal (
    set "javaccmd=%javaclocal%"
    for %%i in ("%javaclocal%") do set "jarcmd=%%~dpijar.exe"
)

if not exist "%jarcmd%" (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jar.exe" (
            set "javaccmd=%JAVA_HOME%\bin\javac.exe"
            set "jarcmd=%JAVA_HOME%\bin\jar.exe"
        )
    )
)

if not exist "%jarcmd%" (
    set "jarlocal="
    for /f "delims=" %%i in ('where jar 2^>nul') do if not defined jarlocal set "jarlocal=%%i"
    if defined jarlocal set "jarcmd=%jarlocal%"
)

if not exist "%jarcmd%" (
    for /d %%i in ("%ProgramFiles%\Java\jdk*") do (
        if exist "%%i\bin\jar.exe" (
            set "javaccmd=%%i\bin\javac.exe"
            set "jarcmd=%%i\bin\jar.exe"
        )
    )
)

if not exist "%jarcmd%" (
    echo erro: jar.exe nao encontrado. instale ou selecione um jdk completo.
    exit /b 1
)

if exist build rmdir /s /q build
if exist "%jarfinal%" del "%jarfinal%"
mkdir "%classes%"

rem compila o codigo usando a biblioteca mp3.
"%javaccmd%" -encoding UTF-8 -cp "%dependencia%" -d "%classes%" MainApp.java ui\*.java controller\*.java model\*.java player\*.java
if errorlevel 1 exit /b 1

rem copia as classes da biblioteca para dentro do jar final.
pushd "%classes%"
"%jarcmd%" xf "..\..\%dependencia%"
popd

rem cria um jar executavel com tudo dentro.
"%jarcmd%" cfe "%jarfinal%" MainApp -C "%classes%" .
if errorlevel 1 exit /b 1

rem apaga arquivos temporarios da compilacao.
rmdir /s /q build

echo jar completo criado em %jarfinal%.
