package com.test;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private String serverAddress; // Address of the server to connect to
    private int serverPort; // Port number of the server
    private Socket socket; // Socket for communication with the server
    private PrintWriter out; // Output stream for sending messages to the server
    private BufferedReader in; // Input stream for receiving messages from the server

    // Constructor to initialise server address and port
    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    // Method to establish connection with the server
    public void connect() {
        try {
            // Create a socket and connect to the server
            socket = new Socket(serverAddress, serverPort);
            // Initialize output stream for sending messages
            out = new PrintWriter(socket.getOutputStream(), true);
            // Initialize input stream for receiving messages
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Print connection success message
            System.out.println("Connected to server " + serverAddress + ":" + serverPort);

            // Start a new thread for receiving messages from the server
            new Thread(new MessageReceiver()).start();

            // Create a scanner to read user input
            Scanner scanner = new Scanner(System.in);
            // Continuously read user input
            while (true) {
                // Read a line of input from the user
                String message = scanner.nextLine();
                // Check if the message is a private message command
                if (message.startsWith("/msg")) {
                    // Send private message to the server
                    out.println(message);
                } else {
                    // Send the message to the server
                    sendMessage(message);
                }
                // Check if the user wants to quit
                if (message.equalsIgnoreCase("quit")) {
                    // Disconnect from the server and exit the loop
                    disconnect();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to disconnect from the server
    public void disconnect() {
        try {
            // Close the socket
            if (socket != null) {
                socket.close();
            }
            // Close the output stream
            if (out != null) {
                out.close();
            }
            // Close the input stream
            if (in != null) {
                in.close();
            }
            // Print disconnection message
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send a message to the server
    public void sendMessage(String message) {
        out.println(message);
    }

    // Inner class for receiving messages from the server in a separate thread
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String inputLine;
                // Continuously read messages from the server
                while ((inputLine = in.readLine()) != null) {
                    // Check if the message is a private message
                    if (inputLine.startsWith("/msg")) {
                        // Parse the private message
                        String[] parts = inputLine.split(" ", 3);
                        try {
                            // Extract sender ID and message content
                            int senderId = Integer.parseInt(parts[1]);
                            String privateMessage = parts[2];
                            // Print the private message
                            System.out.println("Private message from client " + senderId + ": " + privateMessage);
                        } catch (NumberFormatException e) {
                            // Handle error in parsing sender ID
                            System.out.println("Error parsing senderId.");
                        }
                    } else {
                        // Print the received message
                        System.out.println("Received message: " + inputLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Main method to create and run the client
    public static void main(String[] args) {
        // Define server address and port
        String serverAddress = "localhost";
        int serverPort = 12345;
        // Create and connect the client
        Client client = new Client(serverAddress, serverPort);
        client.connect();
    }
}
