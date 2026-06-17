FROM eclipse-temurin:21-jdk

VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar
# or Maven
# ARG JAR_FILE_PATH=target/*.jar

COPY ${JAR_FILE} ozz-user.jar

ENTRYPOINT ["java","-jar","/ozz-user.jar"]
