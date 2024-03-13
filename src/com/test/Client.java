package com.test;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Represents a simple client for communicating with a server.
 */
public class Client {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructs a Client object with the specified server address and port.
     *
     * @param serverAddress the IP address or hostname of the server
     * @param serverPort the port number on which the server is listening for connections
     */
    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Establishes a connection to the server and starts communication.
     */
    public void connect() {
        try {
            // Connect to the server and initialize input and output streams
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to server " + serverAddress + ":" + serverPort);

            // Start a new thread to continuously receive messages from the server
            new Thread(new MessageReceiver()).start();

            // Read messages from the user and send them to the server
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server.
     *
     * @param message the message to send
     */
    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * Runnable implementation for receiving messages from the server.
     */
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                // Continuously read messages from the server and print them to the console
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received message: " + inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Entry point for starting the client.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Change server address as needed
        int serverPort = 12345; // Change server port as needed
        Client client = new Client(serverAddress, serverPort);
        client.connect();
    }
}
