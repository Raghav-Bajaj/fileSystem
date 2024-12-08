# File Management HTTP Server and Client

This project consists of a **server-side** program and a **client-side** program for managing files via HTTP requests. The server listens on port 8080 and handles multiple commands related to file manipulation, such as listing files, removing files, updating files, counting words, and determining word frequencies. The client sends commands to the server using HTTP POST requests.

Additionally, there is a **Dockerfile** to containerize the application for easy deployment.

## Components

### 1. **Server-Side (InputHandlingHttpServer.java)**

The server is responsible for accepting HTTP POST requests and executing file-related commands. It supports the following commands:

- **store ls**: Lists all files in the `/app/files/` directory.
- **store rm <filename>**: Removes the specified file from the `/app/files/` directory.
- **store update <filename>**: Creates a new file with the given name or updates an existing file with a timestamp.
- **store freq-words**: Counts the frequency of words across all files in the `/app/files/` directory.
- **store wc**: Counts the total number of words across all files in the `/app/files/` directory.

The server is implemented using Java's `HttpServer` and listens for requests on `http://localhost:8080/process-input`.

### 2. **Client-Side (InputHandlingHttpClient.java)**

The client is a simple command-line program that prompts the user for input. The user can enter commands that the client sends as HTTP POST requests to the server. Valid commands include `store ls`, `store rm <filename>`, `store update <filename>`, `store freq-words`, and `store wc`.

The client will print the server's response to the console. If an invalid command is entered, an error message is shown.

### 3. **Dockerfile**

The `Dockerfile` is used to build a Docker image for the application. It copies the Java source files, compiles them, and sets up the server and client to run in the same container.

## Flow of the Application

1. **Client Interaction**: 
   - The user runs the client, which prompts for commands.
   - The client sends the input as a POST request to the server's `/process-input` endpoint.
   - The server processes the command and returns a response, which is displayed in the client.

2. **Server Operations**:
   - The server listens for incoming POST requests at `http://localhost:8080/process-input`.
   - Depending on the command received, the server interacts with the `/app/files/` directory, performs the requested file operation, and returns a response.

3. **Dockerization**:
   - The Dockerfile ensures the server and client can run together in a container. The server listens for requests, and the client can interact with it via the same containerized environment.

## Running the Program

### Prerequisites

- Docker installed on your machine.
- Java (JDK 17) installed if you are running the code outside of Docker.

### Steps to Run Using Docker

1. **Build the Docker Image**:
   Open a terminal and navigate to the directory containing the project files, including the Dockerfile. Run the following command to build the Docker image:
   ```bash
    docker build -f mydocer.dockerfile -t java-http-server-client .

### Directory Structure:
```bash
    /app
  ├── InputHandlingHttpServer.java      # Server-side Java program that handles file management commands.
  ├── InputHandlingHttpClient.java      # Client-side Java program that sends commands to the server.
  ├── /files/                          # Directory containing files for manipulation (e.g., for storing, reading).
  └── resources/
      └── retro.txt                    # Example text file used for testing word frequency commands.

