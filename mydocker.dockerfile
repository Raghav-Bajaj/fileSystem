FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Java files into the container
COPY InputHandlingHttpServer.java /app/
COPY InputHandlingHttpClient.java /app/

# Create directory for files inside the container
RUN mkdir /app/files

COPY resources/retro.txt /app/files/

# Install necessary packages (like maven if you need)
# RUN apt-get update && apt-get install -y maven

# Compile the Java files
RUN javac InputHandlingHttpServer.java InputHandlingHttpClient.java

# Expose port for server
EXPOSE 8080

# Run server and client
CMD java InputHandlingHttpServer & java InputHandlingHttpClient
