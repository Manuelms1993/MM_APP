#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APK_PATH="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.example.mmapp"

cd "$ROOT_DIR"

echo "Compilando APK debug..."
./gradlew assembleDebug

if [[ ! -f "$APK_PATH" ]]; then
  echo "No se encontro la APK en: $APK_PATH" >&2
  exit 1
fi

mapfile -t DEVICES < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')

if [[ ${#DEVICES[@]} -eq 0 ]]; then
  echo "No hay dispositivos Android listos. Revisa 'adb devices'." >&2
  exit 1
fi

if [[ ${#DEVICES[@]} -gt 1 ]]; then
  echo "Hay varios dispositivos conectados. Usa uno de estos seriales con adb -s:" >&2
  printf ' - %s\n' "${DEVICES[@]}" >&2
  exit 1
fi

DEVICE_SERIAL="${DEVICES[0]}"

echo "Instalando APK en $DEVICE_SERIAL..."
adb -s "$DEVICE_SERIAL" install -r "$APK_PATH"

echo "Abriendo app en $DEVICE_SERIAL..."
adb -s "$DEVICE_SERIAL" shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1

echo "Listo."
