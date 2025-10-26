# 💬 Java Client-Server Chat Application

(A second year Java project by Soujanya Shanbhag)

This is a classic, full-featured client-server chat application built entirely in Java. It demonstrates core networking, concurrency, and GUI programming concepts.

#### The project consists of two main parts:

*ChatServer: A command-line server that handles multiple clients simultaneously using multi-threading.

*ChatClient: A graphical (GUI) chat window built with Java Swing that users interact with.

## ✨ Features

*Real-time Messaging: Send and receive messages instantly.

*Multi-Client Support: The server uses a thread pool to manage connections, allowing many clients to join the same chat room.

*GUI Interface: A user-friendly graphical client built with Java Swing.

*Graceful Disconnects: Users are announced when they join or leave the chat.

## 🛠️ Technologies & Concepts Used

*Programming Language: Java

*Networking: java.net.Socket and java.net.ServerSocket for network communication.

*Concurrency: java.util.concurrent.ExecutorService (Thread Pool) to handle multiple clients efficiently.

*GUI: Java Swing (JFrame, JTextArea, JButton, etc.) for the client's graphical interface.

*Streams: java.io.BufferedReader and java.io.PrintWriter for sending text data over the network.

## 📁 Project Structure

The project is organized in a standard Java build structure:

/JavaChatApp
    ├── .vscode/        (VS Code settings)
    │   └── launch.json
    ├── bin/            (Compiled .class files)
    │   ├── ChatClient.class
    │   └── ChatServer.class
    ├── src/            (Source .java files)
    │   ├── ChatClient.java
    │   └── ChatServer.java
    └── README.md       (This file)


## 🚀 How to Run (from VS Code Terminal)

1. Open the Terminals

You will need two separate terminals open in VS Code.

Open the first terminal (`Ctrl + ``).

Click the + icon in the terminal panel to open a second one.

2. Compile the Code

In one of your terminals, run the javac command to compile all .java files from the src folder and place the .class files into the bin folder.

#### ->javac src\*.java -d bin


3. Start the Server

In your first terminal, run this command. The server will start and wait for clients to connect.

#### ->java -cp bin ChatServer


You should see the message: Server is running and listening on port 12345

4. Start the Client

In your second terminal, run this command to launch the graphical chat window.

#### ->java -cp bin ChatClient


A window will pop up asking for your name.

You can repeat Step 4 in a third (or fourth!) terminal to have more clients join the chat and talk to each other.
