package serverside;
import clientside.UpdateResponse;

import clientside.ClientGUI.Product;
import clientside.UpdateRequest;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ClientHandler {
    private final ServerSockets server;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket, ServerSockets server) {
        this.clientSocket = clientSocket;
        this.server = server;

        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            handleException("Error setting up streams", e);
        }
    }

    public void handleClient() {
        try {
            Object input;
            while (null != (input = inputStream.readObject())) {
                System.out.println("Received data from client: " + input);

                if (input instanceof Product) {
                    handleProduct((Product) input);
                } else if (input instanceof UpdateRequest) {
                    handleUpdateRequest((UpdateRequest) input);
                } else if (input.equals("DELETE_DATA")) {
                    handleDeleteRequest();
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected.");
        } catch (SocketException e) {
            System.err.println("SocketException: Connection reset. Client may have disconnected.");
        } catch (IOException | ClassNotFoundException e) {
            handleException("Error during communication with client", e);
        } finally {
            cleanup();
        }
    }

    private void handleProduct(Product product) {
        ServerData.saveProduct(product);
        List<Product> productList = ServerData.getAllProducts();
        server.broadcastData(productList);
    }

    private void handleUpdateRequest(UpdateRequest updateRequest) {
        Product updatedProduct = updateRequest.getUpdatedProduct();
        boolean updateSuccessful = updateProductInDatabase(updatedProduct);

        sendData(new UpdateResponse(updateSuccessful, updatedProduct));
    }

    private void handleDeleteRequest() {
        try {
            Product deletedProduct = (Product) inputStream.readObject();
            deleteProductFromDatabase(deletedProduct);

            List<Product> productList = ServerData.getAllProducts();
            server.broadcastData(productList);
        } catch (IOException | ClassNotFoundException e) {
            handleException("Error handling delete request", e);
        }
    }

    private void deleteProductFromDatabase(Product deletedProduct) {
        ServerData.deleteProduct(deletedProduct);
    }

    private boolean updateProductInDatabase(Product updatedProduct) {
        List<Product> productList = ServerData.getAllProducts();
        int index = findProductIndexByName(updatedProduct.getName());
        if (index != -1) {
            productList.set(index, updatedProduct);
            ServerData.setAllProducts(productList);
            return true;
        }
        return false;
    }

    private int findProductIndexByName(String productName) {
        List<Product> productList = ServerData.getAllProducts();
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getName().equals(productName)) {
                return i;
            }
        }
        return -1;
    }

     void sendData(Object data) {
        try {
            outputStream.writeObject(data);
            outputStream.flush();
        } catch (IOException e) {
            handleException("Error sending data to client", e);
        }
    }

    private void cleanup() {
        server.removeClient(this);
        try {
            clientSocket.close();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            handleException("Error closing resources", e);
        }
    }

    private void handleException(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }
}
