FROM docker.io/library/eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY mvnw mvnw
COPY .mvn .mvn
COPY wrapper wrapper
RUN chmod +x mvnw

# Download dependencies first (cached layer unless pom.xml changes)
RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

# ---

FROM docker.io/library/eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/foos-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
