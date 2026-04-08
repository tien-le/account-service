FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:25-jre-alpine

COPY --from=build /app/target/account_service-1.0.0.jar /account_service-1.0.0.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/account_service-1.0.0.jar"]