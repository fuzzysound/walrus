FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
HEALTHCHECK CMD curl --fail http://localhost:8080/hello
ENTRYPOINT ["java", "-jar", "/app.jar"]
