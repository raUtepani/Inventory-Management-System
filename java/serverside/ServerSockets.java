package serverside;

import clientside.ClientGUI;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ServerSockets {
    private static final int SERVER_PORT = 7777;
    private List<ClientHandler> clients = new ArrayList<>();

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);

            while (true) {
                // Accept incoming client connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);
                // Create a new thread (ClientHandler) for each connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);

                // Add the client handler to the list of clients
                clients.add(clientHandler);
                // Start a new thread for the client handler
                new Thread(clientHandler::handleClient).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast data to all connected clients
    public void broadcastData(List<ClientGUI.Product> productList) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendData(productList);
        }
    }

    // Remove a client handler from the list (e.g., when a client disconnects)
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

}
