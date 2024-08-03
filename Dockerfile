# Use an official OpenJDK runtime as a parent image
FROM dream2bcoder/openjre21

# Set the working directory in the container
WORKDIR /spring-boot-whatsapp

# Copy the application's jar file to the container
COPY target/whatsapp-0.0.5-SNAPSHOT.jar /spring-boot-whatsapp/whatsapp.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "whatsapp.jar"]