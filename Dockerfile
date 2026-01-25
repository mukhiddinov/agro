FROM maven:3.9.6-eclipse-temurin-17 AS build
ARG SERVICE_NAME
WORKDIR /workspace
COPY . .
RUN mvn -pl ${SERVICE_NAME} -am -DskipTests package

FROM eclipse-temurin:17-jre
ARG SERVICE_NAME
WORKDIR /app
COPY --from=build /workspace/${SERVICE_NAME}/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
