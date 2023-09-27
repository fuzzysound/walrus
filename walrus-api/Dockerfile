FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /home/eth-wallets/
ENTRYPOINT ["java", "-jar", "/app.jar"]
