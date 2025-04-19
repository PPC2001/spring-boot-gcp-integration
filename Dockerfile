# Build stage
FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /app

# Copy Maven Wrapper and set permissions
COPY .mvn/ .mvn
COPY mvnw ./
RUN chmod +x mvnw  # <-- THIS IS THE CRITICAL FIX

COPY pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw package -DskipTests

# Runtime stage (remain the same)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/src/main/resources/application-${PROFILE}.properties ./config/application.properties
ENTRYPOINT ["java", "-Dspring.config.location=file:/app/config/application.properties", "-jar", "app.jar"]
