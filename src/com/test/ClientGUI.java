package com.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientGUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton privateMessageButton;
    private JButton changeCoordinatorButton;
    private ChatClient client;

    public ClientGUI(ChatClient client) {
        this.client = client;

        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        bottomPanel.add(sendButton, BorderLayout.EAST);

        privateMessageButton = new JButton("Private Message");
        bottomPanel.add(privateMessageButton, BorderLayout.WEST);

        changeCoordinatorButton = new JButton("Change Coordinator");
        bottomPanel.add(changeCoordinatorButton, BorderLayout.WEST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

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

        frame.setVisible(true);
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            messageField.setText("");
        }
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI(new ChatClient("localhost", 12345));
            }
        });
    }
}

class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientGUI gui;

    public ChatClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            gui = new ClientGUI(this);

            new Thread(new MessageReceiver()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void sendPrivateMessage(int recipientId, String message) {
        out.println("/msg " + recipientId + " " + message);
    }

    public void changeCoordinator(int newCoordinatorId) {
        out.println("/nc " + newCoordinatorId);
    }

    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    gui.appendMessage(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
