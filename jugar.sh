#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
if [[ -f distribucion/alcalde-digital.jar && -f distribucion/lib/core-4.5.6.jar ]]; then
  exec java -cp 'distribucion/alcalde-digital.jar:distribucion/lib/*' alcaldedigital.app.ClienteApp
fi
printf '%s\n' 'Primero ejecuta: bash herramientas.sh paquete'
exit 1
