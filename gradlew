#!/bin/sh

set -eu

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

for candidate in \
  "$HOME/gradle/bin/gradle" \
  "/opt/gradle/bin/gradle" \
  "/usr/local/gradle/bin/gradle" \
  "/usr/local/bin/gradle"
do
  if [ -x "$candidate" ]; then
    exec "$candidate" "$@"
  fi
done

echo "No se encontró un binario de Gradle accesible. Configura Gradle en PATH o genera el wrapper estándar desde el IDE." >&2
exit 1

