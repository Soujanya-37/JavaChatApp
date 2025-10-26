import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the main server for the chat application.
 * It listens on a specific port for new client connections and manages a pool
 * of threads to handle each client.
 *
 * This is a console-only application.
 */
public class ChatServer {

    // A list of all connected client writers. 'volatile' ensures it's thread-safe
    // for simple adds/removals, but we'll synchronize on it for iteration.
    private static volatile Set<PrintWriter> clientWriters = new HashSet<>();
    
    // A thread pool to manage all our client handler threads.
    // This is more efficient than creating a new Thread() for every single client.
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10); // Handles up to 10 clients concurrently

    public static void main(String[] args) {
        final int PORT = 12345; // The port to listen on

        System.out.println("Chat Server is starting up...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and listening on port " + PORT);

            while (true) {
                // Wait for a new client to connect. This is a blocking call.
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                    // Create a new task to handle this client and submit it to the thread pool.
                    ClientHandler clientTask = new ClientHandler(clientSocket);
                    threadPool.submit(clientTask);
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + PORT + ": " + e.getMessage());
        } finally {
            // Shutdown the thread pool when the server closes
            threadPool.shutdown();
        }
    }

    /**
     * A nested static class that handles communication for a single client on its own thread.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;
        private String clientName = "User"; // Default name

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Set up input and output streams for this client
                InputStream input = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));
                
                OutputStream output = socket.getOutputStream();
                writer = new PrintWriter(output, true); // true = auto-flush

                // --- Client Name Handling ---
                // Ask for a name first.
                writer.println("SUBMIT_NAME");
                this.clientName = reader.readLine();
                if (this.clientName == null || this.clientName.trim().isEmpty()) {
                    this.clientName = "Anonymous-" + (int)(Math.random() * 1000);
                }
                
                // Add this client's writer to the shared set so we can broadcast to it.
                // We synchronize to prevent concurrent modification issues.
                synchronized (clientWriters) {
                    clientWriters.add(writer);
                }

                System.out.println(this.clientName + " has joined.");
                broadcastMessage(this.clientName + " has joined the chat.");
                
                // --- Message Reading Loop ---
                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    if (serverMessage.equalsIgnoreCase("exit")) {
                        break; // Client requested to leave
                    }
                    // Broadcast the received message to all other clients
                    String formattedMessage = this.clientName + ": " + serverMessage;
                    broadcastMessage(formattedMessage);
                }

            } catch (SocketException e) {
                System.out.println("Client disconnected abruptly: " + clientName);
            } catch (IOException e) {
                System.err.println("Error in client handler for " + clientName + ": " + e.getMessage());
            } finally {
                // --- Cleanup ---
                // This block executes whether the loop finishes or an error occurs.
                try {
                    // Remove the writer from the set
                    if (writer != null) {
                        synchronized (clientWriters) {
                            clientWriters.remove(writer);
                        }
                    }
                    // Announce the client has left
                    String departureMessage = this.clientName + " has left the chat.";
                    System.out.println(departureMessage);
                    broadcastMessage(departureMessage);
                    
                    // Close the socket
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket for " + clientName + ": " + e.getMessage());
                }
            }
        }

        /**
         * Sends a message to every connected client.
         */
        private void broadcastMessage(String message) {
            // We must synchronize on the set when iterating over it
            // to prevent another thread from modifying it (adding/removing a writer)
            // at the same time.
            synchronized (clientWriters) {
                for (PrintWriter w : clientWriters) {
                    w.println(message);
                }
            }
        }
    }
}

