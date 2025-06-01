// ServerGUI.java
package clientserverchatapplication;

/*
 * These imports are used to build the GUI and handle network communication:
 * - javax.swing and java.awt for creating the interface
 * - java.io and java.net for networking and data transmission
 * - java.util and java.util.concurrent for managing client tracking and threads
 */
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerGUI {

    /*
     * These are the main server components:
     * - JFrame for the server window
     * - JTabbedPane to separate chat tabs for each client
     * - JTextArea for server logs and each client’s messages
     * - ServerSocket for listening to client connections
     * - ExecutorService to run client handlers in parallel
     * - Maps to store client names and their individual chat areas
     */
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JTextArea logArea;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private Map<Socket, String> clientNames = new ConcurrentHashMap<>();
    private Map<String, JTextArea> clientTextAreas = new ConcurrentHashMap<>();
    private boolean isRunning = true;
    private int clientCounter = 1;

    /*
     * Constructor to set up the GUI and start the server.
     */
    public ServerGUI() {
        setupGUI();
        startServer();
    }

    /*
     * This method creates the server interface window with tabs,
     * scrollable log area, and a shutdown button.
     */
    private void setupGUI() {
        frame = new JFrame("Server Console");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(245, 245, 245));

        tabbedPane = new JTabbedPane();

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setBackground(new Color(255, 255, 240));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Server Log"));
        tabbedPane.addTab("Server Log", logScroll);

        frame.add(tabbedPane, BorderLayout.CENTER);

        JButton shutdownButton = new JButton("Shutdown Server");
        shutdownButton.setBackground(new Color(220, 20, 60));
        shutdownButton.setForeground(Color.WHITE);
        shutdownButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        shutdownButton.addActionListener(e -> shutdownServer());
        frame.add(shutdownButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    /*
     * This method starts the server and listens for new client connections.
     * For each client, it assigns a name, creates a tab, and starts a handler thread.
     */
    private void startServer() {
        executor = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(5000);
            log("Server started on port 5000\n");

            executor.execute(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String assignedName = "Client" + clientCounter++;
                        clientNames.put(clientSocket, assignedName);

                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        out.println(assignedName); // Send name to client

                        /*
                         * Create a new text area and tab for this client
                         * so their chat messages appear in their own section.
                         */
                        JTextArea clientArea = new JTextArea();
                        clientArea.setEditable(false);
                        clientArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
                        clientArea.setBackground(new Color(255, 255, 240));
                        clientTextAreas.put(assignedName, clientArea);
                        JScrollPane clientScroll = new JScrollPane(clientArea);
                        clientScroll.setBorder(BorderFactory.createTitledBorder(assignedName + " Messages"));
                        tabbedPane.addTab(assignedName, clientScroll);

                        log(assignedName + " connected.");
                        executor.execute(() -> handleClient(clientSocket));
                    } catch (IOException e) {
                        if (isRunning) {
                            log("Connection accept error: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (IOException e) {
            log("Could not start server: " + e.getMessage());
        }
    }

    /*
     * This method handles a single connected client.
     * It reads incoming messages and replies with responses,
     * while also updating both server log and the client's tab.
     */
    private void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String clientName = clientNames.get(socket);
            JTextArea clientArea = clientTextAreas.get(clientName);
            String message;

            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("SHUTDOWN")) {
                    log(clientName + " requested shutdown.");
                    break;
                }

                log("Received from " + clientName + ": " + message);
                appendToClientTab(clientName, "From Client: " + message);
                String response = processMessage(message);
                out.println("Server to " + clientName + ": " + response);
                appendToClientTab(clientName, "To Client: " + response);
            }
        } catch (IOException e) {
            log("Client error: " + e.getMessage());
        } finally {
            /*
             * After the client disconnects, clean up:
             * - Remove them from tracking maps
             * - Remove their tab from the GUI
             */
            try {
                socket.close();
                String clientName = clientNames.remove(socket);
                if (clientName != null) {
                    clientTextAreas.remove(clientName);
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                            if (tabbedPane.getTitleAt(i).equals(clientName)) {
                                tabbedPane.removeTabAt(i);
                                break;
                            }
                        }
                    });
                }
            } catch (IOException e) {
                log("Error closing client socket: " + e.getMessage());
            }
        }
    }

    /*
     * This method updates the message box (tab) of the specific client
     * by appending the new message there.
     */
    private void appendToClientTab(String clientName, String message) {
        JTextArea area = clientTextAreas.get(clientName);
        if (area != null) {
            SwingUtilities.invokeLater(() -> area.append(message + "\n"));
        }
    }

    /*
     * This method checks the incoming message and returns a response.
     * It handles both simple commands and custom replies.
     */
    private String processMessage(String msg) {
        msg = msg.toLowerCase().trim();
        if (msg.contains("get file")) return "[Server]: File service not yet implemented.";
        if (msg.contains("get data")) return "[Server]: Data retrieval successful.";
        if (msg.contains("run command")) return "[Server]: Command executed.";

        return switch (msg) {
            case "hi" -> "Hello!";
            case "hello" -> "Hi!";
            case "how are you" -> "I am fine, and you?";
            case "bye" -> "Goodbye!";
            default -> "[Server Echo]: " + msg;
        };
    }

    /*
     * This method shuts down the server and all clients gracefully.
     * It sends a message to every client before closing their connection.
     */
    private void shutdownServer() {
        isRunning = false;
        log("Shutting down server...");
        try {
            for (Socket clientSocket : clientNames.keySet()) {
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("[Server]: Server is shutting down.");
                    clientSocket.close();
                } catch (IOException e) {
                    log("Error closing client: " + e.getMessage());
                }
            }
            clientNames.clear();
            clientTextAreas.clear();
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            log("Server shut down complete.");
        } catch (IOException e) {
            log("Error during server shutdown: " + e.getMessage());
        }
    }

    /*
     * This method prints messages to the server log area in the GUI.
     */
    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    /*
     * Main method to start the server GUI on the Swing event thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}
