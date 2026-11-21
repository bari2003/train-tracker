# Multi-stage build for Spring Boot on Render
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production JAR (skip unit tests for fast cloud build)
RUN mvn clean package -DskipTests

# Stage 2: Production JRE runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Render assigns dynamic PORT environment variable (default 8080)
ENV PORT=8080
EXPOSE 8080

# Launch Spring Boot with dynamic port
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
