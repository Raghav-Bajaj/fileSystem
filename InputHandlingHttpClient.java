import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class InputHandlingHttpClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter commands to send to the server (type 'quit' to exit):");
        while (true) {
            System.out.print("Command: ");
            String input = scanner.nextLine();  // Read user input

            // Exit the loop if the user enters "quit"
            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Exiting client...");
                break;
            }

            try {
                // Prepare the HTTP POST request
                URL url = new URL("http://localhost:8080/process-input");  // Server is at localhost on port 8080
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "text/plain");

                // Send the user input as the request body
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(input.getBytes());
                    os.flush();
                }

                // Read the server's response
                int responseCode = connection.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode < 400 ? connection.getInputStream() : connection.getErrorStream()))) {
                    String line;
                    System.out.println("Server Response:");
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                connection.disconnect();

            } catch (Exception e) {
                // Display a custom error message
                String errorMessage = "An error occurred while communicating with the server.\n" +
                                      "Ensure the server is running and try again.\n" +
                                      "Invalid input. Use:\n" +
                                      " - 'store rm <filename>'\n" +
                                      " - 'store update <filename>'\n" +
                                      " - 'store ls'\n" +
                                      " - 'store wc'\n" +
                                      " - 'store freq-words'\n" +
                                      " - 'store add <filename> <content>'";
                System.out.println(errorMessage);
            }
        }

        scanner.close();
    }
}
