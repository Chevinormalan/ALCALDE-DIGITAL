@echo off
cd /d "%~dp0"
java --enable-native-access=ALL-UNNAMED "-Dnet.java.games.input.librarypath=%CD%\distribucion\nativos" -cp "distribucion/alcalde-digital.jar;distribucion/lib/*" alcaldedigital.app.ClienteApp
if errorlevel 1 pause
