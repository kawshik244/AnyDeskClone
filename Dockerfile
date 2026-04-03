# Stage 1 — Build the application
# We use a Maven image that already has Java 21 and Maven inside
FROM maven:3.9.4-eclipse-temurin-21 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first — Docker caches this layer
# So if only your code changes, it won't re-download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Now copy your source code
COPY src ./src


# Build the jar file, skip tests for faster build
RUN mvn clean package -DskipTests


# ──────────────────────────────────────────────────────────────
# Stage 2 — Run the application
# We use a smaller image just for running, not building
# This makes the final image much smaller
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built jar from Stage 1
COPY --from=builder /app/target/*.jar app.jar

# Tell Docker this app runs on port 8080
EXPOSE 8080

# Command to start the application
ENTRYPOINT ["java", "-jar", "app.jar"]