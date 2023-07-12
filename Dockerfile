FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} cloudonix-challenge-0.0.1.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/cloudonix-challenge-0.0.1.jar"]