#!/bin/bash

JAR_NAME="spring-ecommerce-0.0.1-SNAPSHOT.jar"
DEPLOY_PATH="/home/ubuntu/app/"

ENV_FILE="/home/ubuntu/app/.env"
set -a; [ -f "$ENV_FILE" ] && source "$ENV_FILE"; set +a

echo "> Checking for a currently running application..."
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]
then
  echo "> No running application found."
else
  echo "> Stopping running application. PID: $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo "> Starting application..."
nohup java -jar $DEPLOY_PATH$JAR_NAME > ./application.log 2>&1 &
