package clientside;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import clientside.UpdateResponse;
import clientside.UpdateRequest;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Optional;

public class ClientGUI extends Application
{

    private TextField nameField;
    private TextArea descriptionArea;
    private TextField quantityField;
    private Label resultLabel;

    private ObservableList<Product> products;
    private ClientSocket clientSocket;

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Inventory Management System");
        nameField = new TextField();
        nameField.setPromptText("Enter name...");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter  description here...");
        descriptionArea.setPrefHeight(350);
        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity...");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> sendDataToServer());

        resultLabel = new Label();

        // TableView setup
        TableView<Product> tableView = new TableView<>();
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        TableColumn<Product, String> descriptionColumn = new TableColumn<>("Description");
        TableColumn<Product, Double> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Product, Void> editColumn = new TableColumn<>("Edit");
        TableColumn<Product, Void> deleteColumn = new TableColumn<>("Delete");

        tableView.getColumns().addAll(nameColumn, descriptionColumn, quantityColumn);
        tableView.getColumns().addAll(editColumn, deleteColumn);


        // Refresh button
        Button refreshButton = new Button("View-Available  Product");
        refreshButton.setOnAction(e -> fetchDataFromServer());

        // Inside the start method
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(20));

        // Add components to the VBox
        vBox.getChildren().addAll(
                new Label("Product-Name:"), nameField,
                new Label("Description:"), descriptionArea,
                new Label("Quantity:"), quantityField,
                submitButton, resultLabel,
                tableView, refreshButton
        );
        Image icon = new Image("logoapp.jpg");
        primaryStage.getIcons().add(icon);

        // Create and set the scene
        Scene scene = new Scene(vBox, 600, 500);
        primaryStage.setScene(scene);

        // Initialize products list
        products = FXCollections.observableArrayList();
        tableView.setItems(products);

        // Initialize TableColumn instances and associate them with Product properties
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showDeleteConfirmationDialog(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });



        //initialize edit column
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showEditDialog(product,getTableView());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });
        //tableView.getColumns().addAll(nameColumn, descriptionColumn, quantityColumn, editColumn);

        clientSocket = new ClientSocket(this);
        clientSocket.connectToServer();

        primaryStage.show();
    }
    private void showDeleteConfirmationDialog(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete the product: " + product.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed deletion, send delete request to the server
            clientSocket.sendDeleteToServer(product);
        }
    }
    private void showEditDialog(Product product,TableView<Product> tableView) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField nameField = new TextField(product.getName());
        TextField descriptionField = new TextField(product.getDescription());
        TextField quantityField = new TextField(String.valueOf(product.getQuantity()));

        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("Description:"), 0, 1);
        gridPane.add(descriptionField, 1, 1);
        gridPane.add(new Label("Quantity:"), 0, 2);
        gridPane.add(quantityField, 1, 2);

        dialog.getDialogPane().setContent(gridPane);

        // Enable/Disable save button based on the input fields
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Add listeners to enable/disable the save button
        nameField.textProperty().addListener((observable, oldValue, newValue) ->
                saveButton.setDisable(newValue.trim().isEmpty() || descriptionField.getText().trim().isEmpty() || quantityField.getText().trim().isEmpty()));

        descriptionField.textProperty().addListener((observable, oldValue, newValue) ->
                saveButton.setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty() || quantityField.getText().trim().isEmpty()));

        quantityField.textProperty().addListener((observable, oldValue, newValue) ->
                saveButton.setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty() || descriptionField.getText().trim().isEmpty()));

        // Convert the result to a Product when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Product(nameField.getText(), descriptionField.getText(), Double.parseDouble(quantityField.getText()));
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();

        result.ifPresent(updatedProduct -> {
            // Update the product with the new values
            product.nameProperty().set(updatedProduct.getName());
            product.descriptionProperty().set(updatedProduct.getDescription());
            product.setQuantity(updatedProduct.getQuantity());

            // Update the TableView with the modified product
            tableView.refresh();

            // Send an update request to the server
            clientSocket.sendUpdateToServer(product);
        });
    }

    private void sendDataToServer() {
        // Retrieve data from input fields and display it
        String name = nameField.getText();
        String description = descriptionArea.getText();
        double price;

        try {
            if (name.isEmpty() || description.isEmpty()) {
                throw new IllegalArgumentException("Name and description cannot be empty.");
            }
            price = Double.parseDouble(quantityField.getText());
            // Send data to the server using the ClientSocket
            Product product = new Product(name, description, price);
            clientSocket.sendDataToServer(product);
            modal("Operation Successful","Data sent to server: \nName: " + name + "\nDescription: " + description + "\nPrice: " + price);
            // Optionally, clear input fields
            nameField.clear();
            descriptionArea.clear();
            quantityField.clear();
            // Add data to the TableView
            //products.add(new Product(name, description, price));
        } catch (NumberFormatException e) {
            resultLabel.setText("Invalid price. Please enter a numeric value.");
        } catch (IllegalArgumentException e) {
            resultLabel.setText(e.getMessage());
        }
    }

    private void fetchDataFromServer() {
        // Request data from the server using the ClientSocket
        clientSocket.fetchDataFromServer();
    }

    public void receiveDataFromServer(ArrayList<Product> newData) {
        System.out.println("Received data from server: " + newData);
        // Update the TableView with the new data received from the server
        products.setAll(newData);
        // Display a message indicating that data has been received
        resultLabel.setText("Data received from server.");
    }

    public void stop() {
        // Cleanup code (if any) when the application is closed
        clientSocket.disconnectFromServer();
    }

    // Model class for product
    public static class Product implements Serializable {
        private final String name;
        private final String description;

        private double quantity;

        public Product(String name, String description, double price) {
            this.name = name;
            this.description = description;
            this.quantity = price;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
        private static final long serialVersionUID = 1L;
        public double getQuantity() {
            return quantity;
        }
        public void setQuantity(double quantity)
        {
            this.quantity = quantity;
        }

        public StringProperty nameProperty() {
            return new SimpleStringProperty(name);
        }

        public StringProperty descriptionProperty() {
            return new SimpleStringProperty(description);
        }

        public DoubleProperty quantityProperty() {
            return new SimpleDoubleProperty(quantity);
        }

    }
    public void modal(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


