FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw* .
COPY .mvn .mvn

RUN mvn -B -ntp dependency:go-offline -DskipTests

COPY src src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

USER 1000

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]