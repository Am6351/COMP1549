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

    public ClientHandler(Socket socket, Server server, String name) {
        this.id = nextId++;
        this.socket = socket;
        this.server = server;
        this.name = name;
        this.isCoordinator = false; // Initially not a coordinator

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
                if (inputLine.equalsIgnoreCase("list")) {
                    // Handle special command to view client list
                    String clientList = server.getClientList();
                    sendMessage(clientList);
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
