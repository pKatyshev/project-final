FROM eclipse-temurin:17-jdk-alpine
COPY resources ./resources
COPY target/jira-1.0.jar jira-1.0.jar
ENTRYPOINT ["java", "-jar", "/jira-1.0.jar"]