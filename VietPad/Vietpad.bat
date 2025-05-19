@echo off
if "%OS%" == "Windows_NT" goto Windows_NT
start javaw -cp %0\..\VietPad.jar VietPad %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

:Windows_NT
cd /d %0\..
start javaw -cp VietPad.jar VietPad %*
if NOT "%COMSPEC%" == "%SystemRoot%\system32\cmd.exe" goto end
if %errorlevel% == 9009 echo Java is not in your PATH. Cannot run program.
if errorlevel 1 pause

:end
