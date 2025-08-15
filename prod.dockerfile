FROM eclipse-temurin:21-jdk-jammy

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

WORKDIR /app

COPY ./build/libs/spring-ecommerce-0.0.1-SNAPSHOT.jar ./app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:MaxMetaspaceSize=128m", "-XX:+UseG1GC", "-jar", "/app/app.jar"]
