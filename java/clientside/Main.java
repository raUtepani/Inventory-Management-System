package clientside;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        // Instantiate the client GUI
        ClientGUI clientGUI = new ClientGUI();

        //initialize and start the client
        clientGUI.init();

        // Start the client GUI
        clientGUI.start(primaryStage);


        //ClientSocket clientSock = new ClientSocket(clientGUI);


    }

}

