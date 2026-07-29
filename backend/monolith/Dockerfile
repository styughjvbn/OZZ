FROM eclipse-temurin:21-jre

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} ozz-backend.jar

ENTRYPOINT ["java", "-jar", "/app/ozz-backend.jar"]
