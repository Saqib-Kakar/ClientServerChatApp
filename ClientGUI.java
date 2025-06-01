// ClientGUI.java
package clientserverchatapplication;

/*
 * These imports bring in the necessary Java classes:
 * - javax.swing and java.awt are used for building the graphical interface
 * - java.io is used for reading and writing messages over the network
 * - java.net is used to connect to the server using sockets
 */
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientGUI {

    /*
     * These are the main components used in the GUI:
     * - JFrame: Main window of the application
     * - JTextArea: Displays all chat messages
     * - JTextField: Where the user types a message
     * - JButton: For sending and shutting down
     * - Socket: Connects to the server
     * - BufferedReader and PrintWriter: Used for message input/output
     */
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton shutdownButton;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;
    private String clientName = "Unknown";

    /*
     * Constructor that starts the application.
     * It first creates the GUI and then tries to connect to the server.
     */
    public ClientGUI() {
        createGUI();
        connectToServer();
    }

    /*
     * This method builds the GUI window for the client.
     * It includes layout, fonts, colors, and placing all the components.
     */
    private void createGUI() {
        frame = new JFrame("Client Chat");
        frame.setSize(450, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(245, 245, 245));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        chatArea.setBackground(new Color(255, 255, 240));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Chat Messages"));
        frame.add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(30, 144, 255));
        sendButton.setForeground(Color.WHITE);
        shutdownButton = new JButton("Shutdown");
        shutdownButton.setBackground(new Color(220, 20, 60));
        shutdownButton.setForeground(Color.WHITE);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(shutdownButton, BorderLayout.SOUTH);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        /*
         * Set up actions when buttons are clicked or Enter key is pressed.
         * These actions handle sending messages and shutting down the client.
         */
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        shutdownButton.addActionListener(e -> shutdownClient());

        frame.setVisible(true);
    }

    /*
     * This method connects the client to the server at localhost on port 5000.
     * It receives the assigned client name from the server and starts a thread
     * to read incoming messages from the server.
     */
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            /*
             * After connecting, the client receives its name (like Client1)
             * from the server and updates the window title with that name.
             */
            clientName = in.readLine();
            frame.setTitle(clientName);
            chatArea.append("Connected to server as " + clientName + "\n");
            connected = true;

            readMessages();
        } catch (IOException e) {
            chatArea.append("Connection failed: " + e.getMessage() + "\n");
        }
    }

    /*
     * This method runs a background thread that keeps listening
     * for incoming messages from the server and displays them in the chat area.
     */
    private void readMessages() {
        Thread thread = new Thread(() -> {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    chatArea.append(msg + "\n");
                }
            } catch (IOException e) {
                chatArea.append("Server not available. Waiting to reconnect...\n");
                connected = false;
            }
        });
        thread.start();
    }

    /*
     * This method sends the message typed by the user to the server.
     * It also shows the same message in the chat area under "Me:".
     */
    private void sendMessage() {
        if (!connected) {
            chatArea.append("Cannot send message: Not connected to server.\n");
            return;
        }
        String message = inputField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            chatArea.append("Me: " + message + "\n");
            inputField.setText("");
        }
    }

    /*
     * This method shuts down the client safely.
     * It sends a shutdown message to the server, closes the socket,
     * and then closes the window.
     */
    private void shutdownClient() {
        try {
            if (socket != null && !socket.isClosed()) {
                out.println("SHUTDOWN");
                socket.close();
            }
            chatArea.append("Client shutdown complete.\n");
            frame.dispose();
        } catch (IOException e) {
            chatArea.append("Error during shutdown: " + e.getMessage() + "\n");
        }
    }

    /*
     * This is the entry point of the program.
     * It launches the GUI on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
