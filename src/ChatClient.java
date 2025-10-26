import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This is the client-side GUI for the chat application.
 * It connects to the ChatServer, provides a UI for sending and receiving
 * messages, and handles asking the user for their name.
 */
public class ChatClient {

    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;

    // GUI Components
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField textField;
    private String clientName = "User";

    public ChatClient() {
        // Build the GUI
        frame = new JFrame("Java Chat Client");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Message Display Area ---
        messageArea = new JTextArea(10, 40);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // --- Message Input Panel ---
        JPanel inputPanel = new JPanel(new BorderLayout());
        textField = new JTextField(30);
        JButton sendButton = new JButton("Send");
        
        // Add action listener to the text field (for pressing Enter)
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Add action listener to the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        // Handle window closing event to gracefully disconnect
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (writer != null) {
                        writer.println("exit");
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    System.err.println("Error on closing: " + ex.getMessage());
                }
            }
        });
        
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the window
    }

    /**
     * Sends the text from the text field to the server.
     */
    private void sendMessage() {
        try {
            String message = textField.getText();
            if (message != null && !message.trim().isEmpty()) {
                writer.println(message);
                textField.setText(""); // Clear the input field
            }
        } catch (Exception e) {
            messageArea.append("Failed to send message: " + e.getMessage() + "\n");
        }
    }

    /**
     * Connects to the server and starts the main logic.
     */
    private void run() {
        try {
            final String SERVER_ADDRESS = "127.0.0.1"; // localhost
            final int PORT = 12345;
            
            socket = new Socket(SERVER_ADDRESS, PORT);
            
            // Set up streams
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
            
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true); // true = auto-flush

            // --- Server Message Handling Loop ---
            // This loop will run on the main thread, blocking until a message is received
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (line.startsWith("SUBMIT_NAME")) {
                    // Server is asking for a name. Show a dialog.
                    this.clientName = JOptionPane.showInputDialog(
                        frame,
                        "Choose your screen name:",
                        "Name Selection",
                        JOptionPane.PLAIN_MESSAGE
                    );
                    
                    if (this.clientName == null || this.clientName.trim().isEmpty()) {
                        this.clientName = "Anonymous-" + (int)(Math.random() * 1000);
                    }
                    
                    writer.println(this.clientName); // Send the name to the server
                    frame.setTitle("Java Chat Client - " + this.clientName); // Update window title
                } else {
                    // It's a regular chat message. Append it to the text area.
                    messageArea.append(line + "\n");
                    // Auto-scroll to the bottom
                    messageArea.setCaretPosition(messageArea.getDocument().getLength());
                }
            }

        } catch (ConnectException e) {
            System.err.println("Connection refused. Is the server running on port 12345?");
            JOptionPane.showMessageDialog(frame, 
                "Could not connect to the server. Is it running?", 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure GUI updates are thread-safe
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChatClient client = new ChatClient();
                client.frame.setVisible(true); // Show the GUI
                
                // We run the network connection logic in a separate thread
                // to avoid freezing the GUI.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.run();
                    }
                }).start();
            }
        });
    }
}

