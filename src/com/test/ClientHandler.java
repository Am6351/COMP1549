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
                if (isCoordinator) {
                    // If the client is the coordinator, handle messages differently (if desired)
                    handleCoordinatorMessage(inputLine);
                } else {
                    System.out.println("Message from client " + name + ": " + inputLine);
                    server.broadcastMessage(id, inputLine);
                }
            }
            server.removeClient(id);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCoordinatorMessage(String message) {
        // Handle coordinator's messages here (if needed)
        // For example, you may want to process coordinator-specific commands or actions
        // In this example, coordinator's messages are broadcasted just like regular messages
        server.broadcastMessage(id, message);
    }
}
