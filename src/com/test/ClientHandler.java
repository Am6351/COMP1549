package com.test;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private static int nextId = 1; // Static variable to assign unique IDs to clients
    private int id; // Client's unique ID
    private String name; // Client's name
    private Socket socket; // Socket for communication with the client
    private Server server; // Reference to the server
    private PrintWriter out; // Output stream for sending messages to the client
    private BufferedReader in; // Input stream for receiving messages from the client
    private boolean isCoordinator; // Flag indicating whether the client is the coordinator

    // Constructor to initialise the client handler with the provided parameters
    public ClientHandler(Socket socket, Server server, String name, int clientId) {
        this.id = clientId; // Use provided client ID
        this.socket = socket; // Assign socket
        this.server = server; // Assign server reference
        this.name = name; // Assign client's name
        this.isCoordinator = false; // By default, client is not a coordinator

        try {
            // Initialise output stream for sending messages
            this.out = new PrintWriter(socket.getOutputStream(), true);
            // Initialise input stream for receiving messages
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter method for retrieving client's ID
    public int getId() {
        return id;
    }

    // Method to send a message to the client
    public void sendMessage(String message) {
        out.println(message);
    }

    // Method to set whether the client is the coordinator
    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    // Method representing the task performed by the client handler thread
    @Override
    public void run() {
        try {
            String inputLine;
            // Continuously read messages from the client
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equalsIgnoreCase("/list")) {
                    // Handle special command to view client list
                    String clientList = server.getClientListWithId();
                    sendMessage(clientList);
                } else if (inputLine.startsWith("/msg")) {
                    // Handle private message command
                    String[] parts = inputLine.split(" ", 3);
                    try {
                        int recipientId = Integer.parseInt(parts[1]);
                        String privateMessage = parts[2];
                        if (server.getClients().containsKey(recipientId)) {
                            // Send private message to the specified recipient
                            server.getClients().get(recipientId).sendMessage(name + " (private): " + privateMessage);
                            sendMessage("Private message sent to client " + recipientId + ": " + privateMessage);
                        } else {
                            sendMessage("Error: Client " + recipientId + " not found or not connected.");
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        sendMessage("Invalid format. Usage: /msg recipientId message");
                    }
                } else if (inputLine.startsWith("/nc")) {
                    // Handle change coordinator command
                    String[] parts = inputLine.split(" ", 2);
                    try {
                        int newCoordinatorId = Integer.parseInt(parts[1]);
                        if (isCoordinator) {
                            // Change coordinator if the client is the current coordinator
                            server.changeCoordinator(newCoordinatorId);
                        } else {
                            sendMessage("Error: Only the current coordinator can change the coordinator.");
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        sendMessage("Invalid format. Usage: /nc clientID");
                    }
                } else {
                    // Broadcast the received message to all clients
                    server.broadcastMessage(id, inputLine);
                }
            }
            // Remove the client from the server's client list and close the socket
            server.removeClient(id);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
