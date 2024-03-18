package com.test;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private int port;
    private Map<Integer, String> clientNames;
    private Map<Integer, ClientHandler> clients;
    private boolean running;
    private Integer coordinatorId;
    private int nextClientId; // Track next available client ID

    public Server(int port) {
        this.port = port;
        this.clientNames = new HashMap<>();
        this.clients = new HashMap<>();
        this.running = false;
        this.coordinatorId = null;
        this.nextClientId = 1; // Initialize client ID counter
    }

    // Getter for clients map
    public Map<Integer, ClientHandler> getClients() {
        return clients;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            running = true;
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Prompt including client ID
                out.println("Enter your name (Your ID is " + nextClientId + "):");
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

                new Thread(clientHandler).start();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    // New method to get client list with IDs
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

    public static void main(String[] args) {
        int port = 12345;
        Server server = new Server(port);
        server.start();
    }
}
