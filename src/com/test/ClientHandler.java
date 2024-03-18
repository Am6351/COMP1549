package com.test;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private static int nextId = 1;
    private int id;
    private String name; // Client's name
    private Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isCoordinator; // Flag indicating whether the client is the coordinator

    public ClientHandler(Socket socket, Server server, String name, int clientId) {
        this.id = clientId; // Use provided client ID
        this.socket = socket;
        this.server = server;
        this.name = name;
        this.isCoordinator = false;

        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

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
