@echo off
setlocal
cd /d "%~dp0"
set "ALCALDE_JAVA=java.exe"
if exist "distribucion\runtime-windows-x64\bin\java.exe" set "ALCALDE_JAVA=%CD%\distribucion\runtime-windows-x64\bin\java.exe"
"%ALCALDE_JAVA%" --enable-native-access=ALL-UNNAMED "-Dnet.java.games.input.librarypath=%CD%\distribucion\nativos" -cp "distribucion/alcalde-digital.jar;distribucion/lib/*" alcaldedigital.app.ClienteApp
if errorlevel 1 pause
endlocal
