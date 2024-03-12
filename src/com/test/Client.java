package com.test;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void connect() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to server " + serverAddress + ":" + serverPort);

            new Thread(new MessageReceiver()).start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received message: " + inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Change server address as needed
        int serverPort = 12345; // Change server port as needed
        Client client = new Client(serverAddress, serverPort);
        client.connect();
    }
}
