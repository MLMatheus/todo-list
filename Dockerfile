# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build the application (skip tests in image build; tests run in CI)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Non-root user
RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /workspace/target/todolist.jar app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
