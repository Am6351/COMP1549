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
    private Map<Integer, ClientHandler> clients;
    private boolean running;

    /**
     * Constructs a Server object with the specified port.
     *
     * @param port the port number on which the server will listen for connections
     */
    public Server(int port) {
        this.port = port;
        this.clients = new HashMap<>();
        this.running = false;
    }

    /**
     * Starts the server by creating a ServerSocket and accepting incoming client connections.
     */
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            running = true;
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Create a new ClientHandler for each connected client and start it in a separate thread
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.put(clientHandler.getId(), clientHandler);
                new Thread(clientHandler).start();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all clients except the one with the specified client ID.
     *
     * @param clientId the ID of the client to exclude from receiving the message
     * @param message the message to broadcast
     */
    public synchronized void broadcastMessage(int clientId, String message) {
        for (ClientHandler client : clients.values()) {
            if (client.getId() != clientId) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Removes a client from the list of connected clients.
     *
     * @param clientId the ID of the client to remove
     */
    public synchronized void removeClient(int clientId) {
        clients.remove(clientId);
        System.out.println("Client disconnected: " + clientId);
    }

    /**
     * Entry point for starting the server.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        int port = 12345; // Change port as needed
        Server server = new Server(port);
        server.start();
    }
}
