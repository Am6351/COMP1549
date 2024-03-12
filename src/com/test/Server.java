package com.test;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private int port;
    private Map<Integer, ClientHandler> clients;
    private boolean running;

    public Server(int port) {
        this.port = port;
        this.clients = new HashMap<>();
        this.running = false;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            running = true;
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.put(clientHandler.getId(), clientHandler);
                new Thread(clientHandler).start();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void broadcastMessage(int clientId, String message) {
        for (ClientHandler client : clients.values()) {
            if (client.getId() != clientId) {
                client.sendMessage(message);
            }
        }
    }

    public synchronized void removeClient(int clientId) {
        clients.remove(clientId);
        System.out.println("Client disconnected: " + clientId);
    }

    public static void main(String[] args) {
        int port = 12345; // Change port as needed
        Server server = new Server(port);
        server.start();
    }
}
