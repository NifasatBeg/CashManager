# ---- Stage 1: Build ----
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Copy Gradle wrapper and config files first for better caching
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradlew ./
COPY gradlew.bat ./
RUN chmod +x gradlew

# Download dependencies to leverage caching
RUN ./gradlew dependencies --no-daemon

# Copy the application source code and build the JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jdk-jammy AS runtime
WORKDIR /app

# Copy only the built JAR from the previous stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port and run the application
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
