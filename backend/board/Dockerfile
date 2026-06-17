FROM eclipse-temurin:21-jdk

VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar
# or Maven
# ARG JAR_FILE_PATH=target/*.jar

COPY ${JAR_FILE} ozz-board.jar

ENTRYPOINT ["java","-jar","/ozz-board.jar"]
