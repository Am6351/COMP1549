package com.test;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Represents a simple chat server that listens for client connections
 * and handles communication between clients.
 */
public class Server {
    private int port;
    private Map<Integer, String> clientNames; // Map to store client names with their IDs
    private Map<Integer, ClientHandler> clients;
    private boolean running;
    private Integer coordinatorId; // ID of the current coordinator

    public Server(int port) {
        this.port = port;
        this.clientNames = new HashMap<>();
        this.clients = new HashMap<>();
        this.running = false;
        this.coordinatorId = null; // Initially no coordinator
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            running = true;
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Prompt the client for their name
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Enter your name:");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String name = in.readLine();

                // Create a new ClientHandler for each connected client and start it in a separate thread
                ClientHandler clientHandler = new ClientHandler(socket, this, name);
                clients.put(clientHandler.getId(), clientHandler);
                clientNames.put(clientHandler.getId(), name);
                if (coordinatorId == null) {
                    coordinatorId = clientHandler.getId(); // Assign the first client as coordinator
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
        for (ClientHandler client : clients.values()) {
            if (client.getId() != clientId) {
                client.sendMessage(senderName + ": " + message); // Include sender's name in the message
            }
        }
    }

    public synchronized void removeClient(int clientId) {
        clients.remove(clientId);
        clientNames.remove(clientId);
        if (clientId == coordinatorId) {
            // If the coordinator disconnected, assign a new coordinator
            if (!clients.isEmpty()) {
                coordinatorId = clients.keySet().iterator().next();
                clients.get(coordinatorId).setCoordinator(true);
            } else {
                coordinatorId = null; // If no clients left, set coordinator to null
            }
        }
        System.out.println("Client disconnected: " + clientId);
    }

    public static void main(String[] args) {
        int port = 12345; // Change port as needed
        Server server = new Server(port);
        server.start();
    }
}
