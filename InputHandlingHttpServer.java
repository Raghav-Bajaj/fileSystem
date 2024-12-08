import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class InputHandlingHttpServer {
    public static void main(String[] args) throws IOException {
        // Create an HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Define the handler
        server.createContext("/process-input", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Read the input data
                    InputStream inputStream = exchange.getRequestBody();
                    String input = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    String response = "";
                    Path currentDir = Paths.get("/app/files/"); // Set the directory to /app/files in the container

                    // Ensure the directory exists
                    Files.createDirectories(currentDir);

                    // Handle the "store ls" command
                    if (input.startsWith("store ls")) {
                        StringBuilder output = new StringBuilder();
                        try (var stream = Files.list(currentDir)) {
                            for (Path path : (Iterable<Path>) stream::iterator) {
                                if (Files.isRegularFile(path)) {
                                    output.append(path.getFileName()).append("\n");
                                }
                            }
                        }
                        response = output.toString().isEmpty() ? "No files found." : output.toString();
                    }
                    // Handle the "store rm <filename>" command
                    else if (input.startsWith("store rm ")) {
                        String filename = input.substring(9).trim(); // Extract the filename
                        Path filePath = currentDir.resolve(filename); // Resolve the file path relative to currentDir
                        if (Files.exists(filePath)) {
                            Files.delete(filePath); // Remove the file
                            response = "File '" + filename + "' has been removed.";
                        } else {
                            response = "File '" + filename + "' does not exist.";
                        }
                    }
                    // Handle the "store update <filename>" command
                    else if (input.startsWith("store update ")) {
                        String filename = input.substring(13).trim(); // Extract the filename
                        Path filePath = currentDir.resolve(filename); // Resolve the file path relative to currentDir
                        if (Files.exists(filePath)) {
                            // Update the file with new content
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), false))) {
                                writer.write("File updated with new content at: " + System.currentTimeMillis());
                            }
                            response = "File '" + filename + "' updated successfully.";
                        } else {
                            // Create a new file with default content
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                                writer.write("Default content created at: " + System.currentTimeMillis());
                            }
                            response = "File '" + filename + "' created successfully.";
                        }
                    }
                    // Handle the "store freq-words" command
                    else if (input.startsWith("store freq-words")) {
                        Map<String, Integer> wordFrequency = new HashMap<>();

                        // Iterate over files and count word frequencies
                        try (var stream = Files.list(currentDir)) {
                            for (Path path : (Iterable<Path>) stream::iterator) {
                                if (Files.isRegularFile(path)) {
                                    try {
                                        String content = Files.readString(path, StandardCharsets.UTF_8);
                                        String[] words = content.split("\\W+"); // Split on non-word characters
                                        for (String word : words) {
                                            if (!word.isBlank()) {
                                                word = word.toLowerCase(); // Normalize to lowercase
                                                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                                            }
                                        }
                                    } catch (IOException e) {
                                        response = "Error reading file: " + path.getFileName();
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            response = "Error accessing the directory: " + e.getMessage();
                        }

                        // Find the 10 most frequent words
                        if (response.isEmpty()) {
                            response = wordFrequency.entrySet().stream()
                                    .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort by frequency (desc)
                                    .limit(10) // Take top 10
                                    .map(entry -> entry.getKey() + ": " + entry.getValue()) // Format as "word: frequency"
                                    .collect(Collectors.joining("\n"));

                            if (response.isEmpty()) {
                                response = "No words found in the files.";
                            }
                        }
                    }
                    // Handle the "store wc" command
                    else if (input.startsWith("store wc")) {
                        long totalWordCount = 0;

                        try (var stream = Files.list(currentDir)) {
                            for (Path path : (Iterable<Path>) stream::iterator) {
                                if (Files.isRegularFile(path)) {
                                    try {
                                        String content = Files.readString(path, StandardCharsets.UTF_8);
                                        totalWordCount += countWords(content);
                                    } catch (IOException e) {
                                        response = "Error reading file: " + path.getFileName();
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            response = "Error accessing the directory: " + e.getMessage();
                        }

                        if (response.isEmpty()) {
                            response = "Total word count in all files: " + totalWordCount;
                        }
                    }
                    // Handle invalid input
                    else {
                        response = "Invalid input. Use:\n" +
                                " - 'store rm <filename>'\n" +
                                " - 'store update <filename>'\n" +
                                " - 'store ls'\n" +
                                " - 'store wc'\n" +
                                " - 'store freq-words'";
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                    }

                    // Send response to client
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        outputStream.write(response.getBytes());
                    }
                } else {
                    // Handle unsupported methods
                    String response = "Method not allowed.";
                    exchange.sendResponseHeaders(405, response.getBytes().length);
                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        outputStream.write(response.getBytes());
                    }
                }
            }
        });

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("HTTP server started on port 8080");
    }

    // Helper method to count words in a string
    private static long countWords(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        return content.split("\\s+").length; // Split on whitespace
    }
}
