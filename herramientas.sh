#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
AD_MAVEN="${AD_MAVEN:-}"
if [[ -z "$AD_MAVEN" ]]; then
  if command -v mvn >/dev/null 2>&1; then AD_MAVEN="$(command -v mvn)"
  else
    for candidato_maven in "${HOME}/.vscode-oss/extensions/"oracle.oracle-java-*/nbcode/java/maven/bin/mvn.sh; do
      if [[ -f "$candidato_maven" ]]; then AD_MAVEN="$candidato_maven"; break; fi
    done
  fi
fi
if [[ -z "$AD_MAVEN" ]]; then printf '%s\n' 'Instala Maven 3.9+ o define AD_MAVEN con la ruta a mvn.'; exit 1; fi
case "${1:-test}" in
  test) bash "$AD_MAVEN" test -Djava.awt.headless=true ;;
  paquete)
    bash "$AD_MAVEN" package -Djava.awt.headless=true
    mkdir -p distribucion/lib
    cp target/alcalde-digital-0.1.0-SNAPSHOT.jar distribucion/alcalde-digital.jar
    cp "${HOME}/.m2/repository/org/processing/core/4.5.6/core-4.5.6.jar" distribucion/lib/
    ;;
  evidencias)
    bash "$AD_MAVEN" compile
    java -Djava.awt.headless=true -cp "target/classes:${HOME}/.m2/repository/org/processing/core/4.5.6/core-4.5.6.jar" alcaldedigital.app.EvidenciasApp
    ;;
  *) printf '%s\n' 'Uso: bash herramientas.sh test|paquete|evidencias'; exit 1 ;;
esac
