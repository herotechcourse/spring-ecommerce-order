#!/usr/bin/env bash
set -e

BUILD_DIR="/home/ubuntu/build"
APP_JAR="/home/ubuntu/app.jar"
LOG_FILE="/home/ubuntu/logs/app.out"

JAR="$(ls -t "$BUILD_DIR"/*.jar 2>/dev/null | head -n1)"
if [ -z "$JAR" ]; then
  echo "No JAR found in $BUILD_DIR" >&2
  exit 1
fi

echo "Using artifact: $JAR"

OLD_PID=$(pgrep -f "$APP_JAR" || true)
if [ -n "$OLD_PID" ]; then
  echo "Stopping old process: $OLD_PID"
  kill -TERM "$OLD_PID" || true
  sleep 3
fi

cp -f "$JAR" "$APP_JAR"
nohup java -jar "$APP_JAR" > "$LOG_FILE" 2>&1 &

NEW_PID=$!
echo "Started PID=$NEW_PID"
echo "Logs: $LOG_FILE"
