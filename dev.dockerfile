FROM gradle:8.13-jdk21

WORKDIR /home/gradle/project

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

CMD ["./gradlew", "bootRun", "--no-daemon"]
