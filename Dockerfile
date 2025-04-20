# ----------- Build Stage -----------
# Use the Eclipse Temurin JDK 17 base image for building the application
FROM eclipse-temurin:17-jdk-jammy as builder

# Set the working directory inside the container
WORKDIR /app

# Copy Maven wrapper files and pom.xml to the working directory
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Give execute permissions to the Maven wrapper script
RUN chmod +x mvnw

# Download all project dependencies ahead of time for faster builds
RUN ./mvnw dependency:go-offline

# Copy the application source code into the container
COPY src ./src

# Package the application into a JAR file, skipping tests for faster build
RUN ./mvnw package -DskipTests


# ----------- Runtime Stage -----------
# Use a lighter JRE-only image for running the application
FROM eclipse-temurin:17-jre-jammy

# Set the working directory for the runtime container
WORKDIR /app

# Copy the built JAR file from the builder stage into the runtime image
COPY --from=builder /app/target/*.jar app.jar

# Define a health check to monitor the container’s health status (useful in GKE or other platforms)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Run the application using the dynamically set port (Cloud Run injects $PORT)
CMD sh -c "java -Dserver.port=${PORT:-8080} -jar app.jar"
