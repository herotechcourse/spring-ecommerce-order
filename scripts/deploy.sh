#!/usr/bin/env bash
set -euo pipefail

APP_NAME="spring-ecommerce"

JAR_SOURCE=${1:-$(ls -1t build/libs/*.jar 2>/dev/null | head -n1)}


DEPLOY_DIR=${DEPLOY_DIR:-"$HOME/app"}
BUILD_DIR=${BUILD_DIR:-"$HOME/build"}

if [[ -z "${JAR_SOURCE}" ]]; then
  echo "[deploy] No jar found. Run './gradlew bootJar' first." >&2
  exit 1
fi


JAR_NAME="$(basename "$JAR_SOURCE")"
TARGET="${DEPLOY_DIR}/${JAR_NAME}"

echo "[deploy] Using jar: ${JAR_SOURCE}"
mkdir -p "$DEPLOY_DIR"

if pgrep -f "$JAR_NAME" >/dev/null 2>&1; then
  OLD_PID=$(pgrep -f "$JAR_NAME")
  echo "[deploy] Stopping old process PID=${OLD_PID}"
  kill -15 "$OLD_PID" || true
  sleep 3
fi

echo "[deploy] Copying jar to ${TARGET}"
cp "$JAR_SOURCE" "$TARGET"


echo "[deploy] Starting ${JAR_NAME}"
cd "$DEPLOY_DIR"


nohup java -jar "$TARGET" \
  --spring.profiles.active=prod \
  > app.out 2>&1 &

NEW_PID=$!
echo "[deploy] Started PID=${NEW_PID}"
echo "[deploy] Logs: tail -f ${DEPLOY_DIR}/app.out"