@echo off
cd /d "%~dp0"
java -cp "distribucion/alcalde-digital.jar;distribucion/lib/*" alcaldedigital.app.ClienteApp
if errorlevel 1 pause
