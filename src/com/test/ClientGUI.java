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

    // Constructor
    public ClientGUI(ChatClient client) {
        this.client = client;

        // Initialize frame
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Initialize chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Initialize bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Initialize message field
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

        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Add action listeners
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

    // Method to append message to chat area
    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }

    // Method to send message
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            messageField.setText("");
        }
    }

    // Method to send private message
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

    // Method to change coordinator
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

    // Main method to start client GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI(new ChatClient("localhost", 12345));
            }
        });
    }
}

// ChatClient class
class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientGUI gui;

    // Constructor
    public ChatClient(String serverAddress, int serverPort) {
        try {
            // Initialize socket, input stream, and output stream
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            gui = new ClientGUI(this); // Initialize GUI

            // Start a new thread to receive messages
            new Thread(new MessageReceiver()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send message
    public void sendMessage(String message) {
        out.println(message);
    }

    // Method to send private message
    public void sendPrivateMessage(int recipientId, String message) {
        out.println("/msg " + recipientId + " " + message);
    }

    // Method to change coordinator
    public void changeCoordinator(int newCoordinatorId) {
        out.println("/nc " + newCoordinatorId);
    }

    // MessageReceiver class
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    gui.appendMessage(inputLine); // Append received message to GUI
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
