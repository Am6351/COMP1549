package com.test;

import java.io.*;
import java.net.*;

/**
 * Represents a handler for communication with a single client.
 * Each instance of this class runs in its own thread.
 */
public class ClientHandler implements Runnable {
    private static int nextId = 1;
    private int id;
    private Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructs a ClientHandler object with the specified socket and server.
     *
     * @param socket the socket representing the connection to the client
     * @param server the server object managing the client connections
     */
    public ClientHandler(Socket socket, Server server) {
        this.id = nextId++;
        this.socket = socket;
        this.server = server;

        try {
            // Initialize input and output streams for communication with the client
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the ID of this client handler.
     *
     * @return the ID of this client handler
     */
    public int getId() {
        return id;
    }

    /**
     * Sends a message to the client associated with this handler.
     *
     * @param message the message to send
     */
    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * The main execution logic of the client handler.
     * Listens for incoming messages from the client and broadcasts them to other clients.
     */
    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Print received message and broadcast it to other clients
                System.out.println("Message from client " + id + ": " + inputLine);
                server.broadcastMessage(id, inputLine);
            }
            // When client disconnects, remove it from the server's list of clients and close the socket
            server.removeClient(id);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
