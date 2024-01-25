package clientside;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientSocket
{
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private final ClientGUI clientGUI;

    public ClientSocket(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    public void connectToServer() {
        try {
            // Establish a connection to the server on port 6001
            socket = new Socket("localhost", 7777);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // Assume additional initialization if needed
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromServer() {
        try {
            // Close the socket and streams
            if (socket != null) {
                socket.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUpdateToServer(ClientGUI.Product updatedProduct) {
        try {
            // Create an UpdateRequest object to encapsulate the updated product
            UpdateRequest updateRequest = new UpdateRequest(updatedProduct);

            // Send the update request to the server
            outputStream.writeObject(updateRequest);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle update request sending error (notify user or take appropriate action)
        }
    }

    // Inside ClientSocket class

    public void fetchDataFromServer() {
        try {
            // Send a request to the server for data
            outputStream.writeObject("FETCH_DATA");
            outputStream.flush();

            // Receive an ArrayList<ClientGUI.Product> in response
            Object response = inputStream.readObject();

            if (response instanceof ArrayList<?> receivedData) {

                if (receivedData.isEmpty()) {
                    clientGUI.modal("Operation Successful","No data received from the server.");
                    ArrayList<ClientGUI.Product> newData = (ArrayList<ClientGUI.Product>) receivedData;
                    clientGUI.receiveDataFromServer(newData);
                    // Handle the case where no data is received (e.g., show an error message)
                } else if (receivedData.getFirst() instanceof ClientGUI.Product) {
                    // Process the received data as usual
                    @SuppressWarnings("unchecked")
                    ArrayList<ClientGUI.Product> newData = (ArrayList<ClientGUI.Product>) receivedData;
                    clientGUI.receiveDataFromServer(newData);
                    clientGUI.modal("Operation Successful", "Data received from server.");
                } else {
                    System.err.println("Unexpected response format from server. ArrayList should contain ClientGUI.Product objects.");
                }

            } else {
                System.err.println("Unexpected response from server: " + response);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            // Handle fetch data error (notify user or take appropriate action)
        }
    }
    public void sendDataToServer(ClientGUI.Product data) {
        try {
            // Send data to the server using serialization
            outputStream.writeObject(data);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendDeleteToServer(ClientGUI.Product product) {
        try {
            // Send the delete request to the server
            outputStream.writeObject("DELETE_DATA");
            outputStream.flush();

            // Send the product to be deleted
            outputStream.writeObject(product);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


