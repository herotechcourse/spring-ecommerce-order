#!/usr/bin/env bash
set -euo pipefail


APP_NAME="spring-ecommerce"
JAR_SOURCE=${1:-"build/libs/${APP_NAME}-0.0.1-SNAPSHOT.jar"}
DEPLOY_DIR="/home/ubuntu/app"

JAR_NAME="$(basename "$JAR_SOURCE")"
TARGET="${DEPLOY_DIR}/${JAR_NAME}"

echo "[deploy] Using jar: ${JAR_SOURCE}"
mkdir -p "$DEPLOY_DIR"


if pgrep -f "$JAR_NAME" >/dev/null 2>&1; then
  OLD_PID=$(pgrep -f "$JAR_NAME")
  echo "[deploy] Stopping old process PID=${OLD_PID}"
  kill -15 "$OLD_PID" || true
  sleep 5
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