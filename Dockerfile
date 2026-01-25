FROM eclipse-temurin:17-jre
ARG SERVICE_NAME
WORKDIR /app
COPY ${SERVICE_NAME}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
