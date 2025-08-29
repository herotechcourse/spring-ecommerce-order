#!/bin/bash

JAR_NAME="spring-ecommerce-0.0.1-SNAPSHOT.jar"
DEPLOY_PATH="/home/ubuntu/app/"

echo "> Checking for a running application..."
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]
then
    echo "> No running application found"
else
    echo "> Stopping running application - PID: $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi
echo "> Start new application..."

# add .env file with following key=value, "STRIPE_SECRET_KEY={VALUE}"
export $(grep -v '^#' $DEPLOY_PATH/.env | xargs)
nohup java -jar $DEPLOY_PATH$JAR_NAME > ./application.log 2>&1 &