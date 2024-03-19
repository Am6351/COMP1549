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
                if (message.startsWith("/msg")) {
                    // Send private message
                    out.println(message);
                } else {
                    sendMessage(message);
                }
                if (message.equalsIgnoreCase("quit")) {
                    disconnect();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            System.out.println("Disconnected from server.");
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
                    if (inputLine.startsWith("/msg")) {
                        // Private message format: "/msg senderId message" use clientid to send private message
                        String[] parts = inputLine.split(" ", 3);
                        try {
                            int senderId = Integer.parseInt(parts[1]);
                            String privateMessage = parts[2];
                            System.out.println("Private message from client " + senderId + ": " + privateMessage);
                        } catch (NumberFormatException e) {
                            System.out.println("Error parsing senderId.");
                        }
                    } else {
                        System.out.println("Received message: " + inputLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 12345;
        Client client = new Client(serverAddress, serverPort);
        client.connect();
    }
}
