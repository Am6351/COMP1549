package com.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

// ClientGUI class for the graphical user interface of the chat client
public class ClientGUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton privateMessageButton;
    private JButton changeCoordinatorButton;
    private ChatClient client;

    // Constructor to initialize the GUI components
    public ClientGUI(ChatClient client) {
        this.client = client;

        // Frame setup
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Text area for displaying chat messages
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Panel for message input and buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Text field for typing messages
        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        // Button for sending messages
        sendButton = new JButton("Send");
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Button for sending private messages
        privateMessageButton = new JButton("Private Message");
        bottomPanel.add(privateMessageButton, BorderLayout.WEST);

        // Button for changing the coordinator
        changeCoordinatorButton = new JButton("Change Coordinator");
        bottomPanel.add(changeCoordinatorButton, BorderLayout.WEST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Action listeners for buttons
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

    // Method to send a message
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            messageField.setText("");
        }
    }

    // Method to send a private message using /msg
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

    // Method to change the coordinator
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

    // Main method to run the client GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI(new ChatClient("localhost", 12345));
            }
        });
    }
}

// ChatClient class for handling communication with the server
class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientGUI gui;

    // Constructor to connect to the server and initialize I/O streams
    public ChatClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

    // Method to change the coordinator
    public void changeCoordinator(int newCoordinatorId) {
        out.println("/nc " + newCoordinatorId);
    }

    // Runnable class for receiving messages from the server in a separate thread
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Append received messages to the GUI chat area
                    gui.appendMessage(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
