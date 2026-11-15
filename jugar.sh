#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
if [[ -f distribucion/alcalde-digital.jar && -f distribucion/lib/core-4.5.6.jar && -f distribucion/lib/jinput-2.0.10.jar ]]; then
  exec java --enable-native-access=ALL-UNNAMED "-Dnet.java.games.input.librarypath=$PWD/distribucion/nativos" -cp 'distribucion/alcalde-digital.jar:distribucion/lib/*' alcaldedigital.app.ClienteApp
fi
printf '%s\n' 'Primero ejecuta: bash herramientas.sh paquete'
exit 1
