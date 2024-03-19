package com.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientGUI {
    private JFrame frame; // Main frame for the GUI
    private JTextArea chatArea; // Text area to display chat messages
    private JTextField messageField; // Text field for typing messages
    private JButton sendButton; // Button to send messages
    private JButton privateMessageButton; // Button to send private messages
    private JButton changeCoordinatorButton; // Button to change coordinator
    private ChatClient client; // Instance of ChatClient for handling communication with the server

    // Constructor to initialize the GUI
    public ClientGUI(ChatClient client) {
        this.client = client;

        // Initialize main frame
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Initialize chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Initialize bottom panel for message input and buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Initialize message input field
        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        // Initialize send button
        sendButton = new JButton("Send");
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Initialize private message button
        privateMessageButton = new JButton("Private Message");
        bottomPanel.add(privateMessageButton, BorderLayout.WEST);

        // Initialize change coordinator button
        changeCoordinatorButton = new JButton("Change Coordinator");
        bottomPanel.add(changeCoordinatorButton, BorderLayout.WEST);

        // Add bottom panel to the main frame
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Attach action listeners to buttons
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        privateMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPrivateMessage();
            }
        });

        changeCoordinatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeCoordinator();
            }
        });

        // Make the frame visible
        frame.setVisible(true);
    }

    // Method to append a message to the chat area
    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }

    // Method to send a message to the server
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            messageField.setText("");
        }
    }

    // Method to send a private message to a specific recipient
    private void sendPrivateMessage() {
        String recipientIdStr = JOptionPane.showInputDialog(frame, "Enter recipient ID:");
        if (recipientIdStr != null && !recipientIdStr.isEmpty()) {
            try {
                int recipientId = Integer.parseInt(recipientIdStr);
                String message = messageField.getText().trim();
                if (!message.isEmpty()) {
                    client.sendPrivateMessage(recipientId, message);
                    messageField.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid recipient ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method to change the coordinator (server) in the chat system
    private void changeCoordinator() {
        String newCoordinatorIdStr = JOptionPane.showInputDialog(frame, "Enter new coordinator ID:");
        if (newCoordinatorIdStr != null && !newCoordinatorIdStr.isEmpty()) {
            try {
                int newCoordinatorId = Integer.parseInt(newCoordinatorIdStr);
                client.changeCoordinator(newCoordinatorId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid coordinator ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Main method to create and run the GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create and initialize the GUI with a ChatClient instance
                new ClientGUI(new ChatClient("localhost", 12345));
            }
        });
    }
}

// Class representing a client in the chat system
class ChatClient {
    private Socket socket; // Socket for communication with the server
    private PrintWriter out; // Output stream for sending messages to the server
    private BufferedReader in; // Input stream for receiving messages from the server
    private ClientGUI gui; // Reference to the GUI for updating the chat interface

    // Constructor to initialize the client with the server address and port
    public ChatClient(String serverAddress, int serverPort) {
        try {
            // Establish connection with the server
            socket = new Socket(serverAddress, serverPort);
            // Initialize output stream for sending messages
            out = new PrintWriter(socket.getOutputStream(), true);
            // Initialize input stream for receiving messages
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Initialize GUI with a reference to this client instance
            gui = new ClientGUI(this);

            // Start a new thread for receiving messages from the server
            new Thread(new MessageReceiver()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send a message to the server
    public void sendMessage(String message) {
        out.println(message);
    }

    // Method to send a private message to a specific recipient
    public void sendPrivateMessage(int recipientId, String message) {
        out.println("/msg " + recipientId + " " + message);
    }

    // Method to change the coordinator (server) in the chat system
    public void changeCoordinator(int newCoordinatorId) {
        out.println("/nc " + newCoordinatorId);
    }

    // Inner class for receiving messages from the server in a separate thread
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String inputLine;
                // Continuously read messages from the server
                while ((inputLine = in.readLine()) != null) {
                    // Update the GUI with the received message
                    gui.appendMessage(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
