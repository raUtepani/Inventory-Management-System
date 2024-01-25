package serverside;

import clientside.ClientGUI;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServerData {
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/pro";
    private static final String USER = "root";
    private static final String PASSWORD = "nityaniyam";

    private static List<ClientGUI.Product> productList = new ArrayList<>();  // Declaration of productList

    // Save product data to the database
    public static void saveProduct(ClientGUI.Product product) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO products (name, description, price) VALUES (?, ?, ?)")) {

            preparedStatement.setString(1, product.getName());
            preparedStatement.setString(2, product.getDescription());
            preparedStatement.setDouble(3, product.getQuantity());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean deleteProduct(ClientGUI.Product product) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM products WHERE name = ?")) {

            preparedStatement.setString(1, product.getName());
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Remove the product from the local list
                productList.removeIf(p -> p.getName().equals(product.getName()));
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all products from the database
    public static List<ClientGUI.Product> getAllProducts() {
        productList.clear();  // Clear existing products before fetching from the database

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM products")) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                double price = resultSet.getDouble("price");

                ClientGUI.Product product = new ClientGUI.Product(name, description, price);
                productList.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productList;
    }

    public static void setAllProducts(List<ClientGUI.Product> updatedProductList) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            // Clear existing products
            // Clear existing products
            try (PreparedStatement clearStatement = connection.prepareStatement("DELETE FROM products")) {
                clearStatement.executeUpdate();
            }

// Insert updated products
            String insertQuery = "INSERT INTO products (name, description, price) VALUES (?, ?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                for (ClientGUI.Product updatedProduct : updatedProductList) {
                    insertStatement.setString(1, updatedProduct.getName());
                    insertStatement.setString(2, updatedProduct.getDescription());
                    insertStatement.setDouble(3, updatedProduct.getQuantity());

                    insertStatement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
