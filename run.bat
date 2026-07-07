@echo off
setlocal
set JAVA=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot\bin\java.exe

if not exist bin\MainApp.class (
    echo Not compiled yet. Run compile.bat first.
    pause
    exit /b 1
)

if not exist data mkdir data

"%JAVA%" -cp bin MainApp
