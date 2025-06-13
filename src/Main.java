
import configuration.DbConnection;
import controller.ProductController;
import controller.CartController; // NEW: Import CartController
import model.repository.ProductRepository;
import model.repository.CartRepository; // NEW: Import CartRepository
import model.service.ProductService;
import model.service.CartService; // NEW: Import CartService
import model.serviceImpl.CartServiceImpl;
import model.serviceImpl.ProductServiceImpl;
import util.PasswordEncryptor;
import view.ProductView;
import view.CartView; // NEW: Import CartView

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner; // For main menu input

public class Main {
    public static void main(String[] args) {
        // --- Database Connection Test ---
        Connection connection = null;
        try {
            connection = DbConnection.getDatabaseConnection();
            System.out.println("Database connection successful!");
        } catch (RuntimeException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            System.err.println("Please check your database server, credentials, and connection URL.");
            return; // Exit application if database connection fails
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Database connection closed after initial test.");
                } catch (SQLException e) {
                    System.err.println("Error closing database connection: " + e.getMessage());
                }
            }
        }

        // --- Password Encryptor
        String plainTextPassword = "koko!@#$@!";
        String hashedPassword = PasswordEncryptor.hashPassword(plainTextPassword);
        System.out.println("Hashed password: " + hashedPassword);
        boolean isMatch = PasswordEncryptor.checkPassword(plainTextPassword, hashedPassword);
        if (isMatch) {
            System.out.println("Login test: successful!");
        } else {
            System.out.println("Login test: failed!");
        }

        System.out.println("\n===== E-COMMERCE APPLICATION START =====");

        // --- Initialize Core Application Components ---
        ProductRepository productRepository = new ProductRepository();
        ProductService productService = new ProductServiceImpl(productRepository);
        ProductView productView = new ProductView();
        ProductController productController = new ProductController(productService, productView);

        // --- Initialize Cart Components ---
        CartRepository cartRepository = new CartRepository();
        CartService cartService = new CartServiceImpl(cartRepository, productRepository) {
        }; // CartService needs ProductRepository
        CartView cartView = new CartView();
        CartController cartController = new CartController(cartService, cartView, productService, productView); // CartController needs ProductService/View

        Scanner mainScanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n======= Main Menu ======");
            System.out.println("1. Product Management");
            System.out.println("2. Shopping Cart");
            System.out.println("0. Exit Application");
            System.out.print("Enter your choice: ");

            String choice = mainScanner.nextLine();

            switch (choice) {
                case "1":
                    // Existing product display logic
                    System.out.println("\n==== Product Management Menu ====");
                    System.out.println("1. List All Products");
                    System.out.println("2. List Products by Category");
                    System.out.print("Enter product display choice: ");
                    String productChoice = mainScanner.nextLine();
                    if ("1".equals(productChoice)) {
                        productController.listAllProductsInStore();
                    } else if ("2".equals(productChoice)) {
                        productController.listAllProductsInStoreSeparatedByCategory();
                    } else {
                        System.out.println("Invalid product display choice.");
                    }
                    break;
                case "2":
                    cartController.showCartMenu(); // Call the new cart menu
                    break;
                case "0":
                    System.out.println("\n===== E-COMMERCE APPLICATION EXIT =====");
                    mainScanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}