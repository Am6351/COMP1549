package com.test;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private int port; // Port number for server
    private Map<Integer, String> clientNames; // Map to store client IDs and names
    private Map<Integer, ClientHandler> clients; // Map to store connected clients and their handlers
    private boolean running; // Flag to indicate if server is running
    private Integer coordinatorId; // ID of the coordinator client
    private int nextClientId; // Track next available client ID

    // Constructor
    public Server(int port) {
        this.port = port;
        this.clientNames = new HashMap<>();
        this.clients = new HashMap<>();
        this.running = false;
        this.coordinatorId = null;
        this.nextClientId = 1; // Initialise client ID counter
    }

    // Getter for clients map
    public Map<Integer, ClientHandler> getClients() {
        return clients;
    }

    // Method to start the server
    public void start() {
        try {
            // Initialise server socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            running = true;
            // Accept incoming client connections
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Initialise output stream for sending initial prompt to client
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Prompt including client ID
                out.println("Enter your name (Your ID is " + nextClientId + "):");
                
                // Initialise input stream for reading client name
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String name = in.readLine();

                // Create client handler with client ID
                ClientHandler clientHandler = new ClientHandler(socket, this, name, nextClientId);
                clients.put(clientHandler.getId(), clientHandler);
                clientNames.put(clientHandler.getId(), name);

                // Increment client ID for the next client
                nextClientId++;

                // Coordinator setup
                if (coordinatorId == null) {
                    coordinatorId = clientHandler.getId();
                    clientHandler.setCoordinator(true);
                }

                // Start a new thread for handling client
                new Thread(clientHandler).start();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to broadcast message to all clients
    public synchronized void broadcastMessage(int clientId, String message) {
        String senderName = clientNames.get(clientId);
        if (message.equalsIgnoreCase("/list")) {
            // Special command to view client list
            String clientList = getClientListWithId(); // Get client list with IDs
            clients.get(clientId).sendMessage(clientList);
        } else if (message.startsWith("/msg")) {
            // Private message format: "/msg recipientId message"
            String[] parts = message.split(" ", 3);
            try {
                int recipientId = Integer.parseInt(parts[1]);
                String privateMessage = parts[2];
                if (clients.containsKey(recipientId)) {
                    clients.get(recipientId).sendMessage(senderName + " (private): " + privateMessage);
                    clients.get(clientId).sendMessage("Private message sent to client " + recipientId + ": " + privateMessage);
                } else {
                    clients.get(clientId).sendMessage("Error: Client " + recipientId + " not found or not connected.");
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                clients.get(clientId).sendMessage("Invalid format. Usage: /msg recipientId message");
            }
        } else {
            // Broadcast the message to all clients except the sender
            for (ClientHandler client : clients.values()) {
                if (client.getId() != clientId) {
                    client.sendMessage(senderName + ": " + message); // Include sender's name in the message
                }
            }
        }
    }

    // Method to get client list with IDs
    public synchronized String getClientListWithId() {
        StringBuilder listBuilder = new StringBuilder();
        listBuilder.append("List of clients with IDs:\n");
        for (Map.Entry<Integer, String> entry : clientNames.entrySet()) {
            listBuilder.append("ID: ").append(entry.getKey()).append(", Name: ").append(entry.getValue());
            if (entry.getKey().equals(coordinatorId)) {
                listBuilder.append(" (Coordinator)");
            }
            listBuilder.append("\n");
        }
        return listBuilder.toString();
    }

    // Method to remove client from server
    public synchronized void removeClient(int clientId) {
        clients.remove(clientId);
        clientNames.remove(clientId);
        if (clientId == coordinatorId) {
            if (!clients.isEmpty()) {
                coordinatorId = clients.keySet().iterator().next();
                clients.get(coordinatorId).setCoordinator(true);
            } else {
                coordinatorId = null;
            }
        }
        System.out.println("Client disconnected: " + clientId);
    }

    // Method to get client list
    public synchronized String getClientList() {
        StringBuilder listBuilder = new StringBuilder();
        listBuilder.append("List of clients:\n");
        for (Map.Entry<Integer, String> entry : clientNames.entrySet()) {
            listBuilder.append(entry.getValue());
            if (entry.getKey().equals(coordinatorId)) {
                listBuilder.append(" (Coordinator)");
            }
            listBuilder.append("\n");
        }
        return listBuilder.toString();
    }

    // Method to change coordinator
    public synchronized void changeCoordinator(int newCoordinatorId) {
        if (clients.containsKey(newCoordinatorId)) {
            if (coordinatorId != null) {
                // Remove coordinator status from the current coordinator
                clients.get(coordinatorId).setCoordinator(false);
            }
            coordinatorId = newCoordinatorId;
            // Set the new coordinator flag for the chosen client
            clients.get(coordinatorId).setCoordinator(true);
            // Notify clients about the change
            broadcastMessage(-1, "Coordinator changed. New coordinator is: " + clientNames.get(coordinatorId));
        } else {
            System.out.println("Error: Client " + newCoordinatorId + " not found or not connected.");
        }
    }

    // Main method to start the server
    public static void main(String[] args) {
        int port = 12345; // Port number for server
        Server server = new Server(port);
        server.start();
    }
}
