# Use OpenJDK 17 as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
# (Change the JAR name as appropriate for each service)
COPY target/*.jar app.jar

# Expose the port your service runs on (change as needed)
EXPOSE 9060

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
