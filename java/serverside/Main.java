package serverside;

public class Main {
    public static void main(String[] args) {
        // Instantiate the server socket
        ServerSockets server = new ServerSockets();

        // Start the server
        server.startServer();
    }
}
