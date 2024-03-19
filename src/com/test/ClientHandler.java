package com.test;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private static int nextId = 1; // Static variable to assign unique IDs to clients
    private int id; // Unique ID for each client
    private String name; // Client's name same name can be used for different clients but the client id will be different
    private Socket socket; // Socket associated with this client
    private Server server; // Reference to the server
    private PrintWriter out; // Writer to send messages to the client
    private BufferedReader in; // Reader to receive messages from the client
    private boolean isCoordinator; // Flag indicating whether the client is the coordinator

    // Constructor
    public ClientHandler(Socket socket, Server server, String name, int clientId) {
        this.id = clientId; // Use provided client ID
        this.socket = socket;
        this.server = server;
        this.name = name;
        this.isCoordinator = false;

        try {
            this.out = new PrintWriter(socket.getOutputStream(), true); // Initialize output stream
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Initialize input stream
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter for client ID
    public int getId() {
        return id;
    }

    // Method to send a message to the client
    public void sendMessage(String message) {
        out.println(message);
    }

    // Setter for coordinator status
    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    // Method to handle client's operations
    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equalsIgnoreCase("/list")) {
                    // Handle special command to view client list
                    String clientList = server.getClientListWithId();
                    sendMessage(clientList);
                } else if (inputLine.startsWith("/msg")) {
                    // Private message format: "/msg recipientId message"
                    String[] parts = inputLine.split(" ", 3);
                    try {
                        int recipientId = Integer.parseInt(parts[1]);
                        String privateMessage = parts[2];
                        if (server.getClients().containsKey(recipientId)) {
                            server.getClients().get(recipientId).sendMessage(name + " (private): " + privateMessage);
                            sendMessage("Private message sent to client " + recipientId + ": " + privateMessage);
                        } else {
                            sendMessage("Error: Client " + recipientId + " not found or not connected.");
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        sendMessage("Invalid format. Usage: /msg recipientId message");
                    }
                } else if (inputLine.startsWith("/nc")) {
                    // Change coordinator command: /nc clientID
                    String[] parts = inputLine.split(" ", 2);
                    try {
                        int newCoordinatorId = Integer.parseInt(parts[1]);
                        if (isCoordinator) {
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
            server.removeClient(id);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
