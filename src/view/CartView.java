package view;

import controller.CartController;
import model.dto.CartResponseDto;
import model.dto.ProductResponseDto;
import model.entities.User;
import model.repository.CartRepository;
import model.repository.UserRepositoryImpl;
import model.service.cart.CartImpl;
import model.service.product.ProductService;
import model.service.product.ProductServiceImpl;
import util.LoginSession;

import java.sql.SQLSyntaxErrorException;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static org.postgresql.core.Oid.UUID;

public class CartView {
    private final CartImpl cartService = new CartImpl();
    private final CartController cartController = new CartController();
    private final UserRepositoryImpl userRepository = new UserRepositoryImpl();
    private final User user = userRepository.getLoggedInUser();
    private final ProductService productService = new ProductServiceImpl(); // To allow searching products by UUID
    // To display product search results

    // A placeholder for the current user ID. In a real app, this would come from a login system.
    private final Integer CURRENT_USER_ID = user.getId(); // Example user ID

    private final Scanner scanner = new Scanner(System.in); // For user input


    public void showCartMenu() {

        while (true) {
            System.out.println("\n--- Cart Menu ---");
            System.out.println("1. View My Cart");
            System.out.println("2. Add Product to Cart by UUID");
            System.out.println("3. Update Cart Item Quantity");
            System.out.println("4. Remove Product from Cart");
            System.out.println("5. Clear My Cart");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    viewCart();
                    break;
                case "2":
                    addProductToCartByUuid();
                    break;
                case "3":
                    updateCartItemQuantity();
                    break;
                case "4":
                    removeProductFromCart();
                    break;
                case "5":
                    clearUserCart();
                    break;
                case "0":
                    System.out.println("Returning to main menu.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public void viewCart() {
        List<CartResponseDto> cartItems = cartController.getAllProductInCart(user);
        assert cartItems != null;
        if (cartItems.isEmpty()) {
            System.out.println("There are no items in the cart.");
            return;
        }

        TableUI<CartResponseDto> tableUI = new TableUI<>();
        tableUI.getTableDisplay(cartItems);
    }

    public void addProductToCartByUuid() {
        System.out.println("--- Add Product to Cart by UUID ---");
        // Optionally list all products first to help user find UUIDs
        System.out.println("Listing all available products (for reference):");
        ProductView.addProductToCart();
        System.out.print("Enter Product UUID to add: ");
        String productUuid = scanner.nextLine();
        if (productUuid.isEmpty()) {
            return;
        }

        System.out.print("Enter Quantity: ");
        int quantity;
        try {
            quantity = scanner.nextInt();
            scanner.nextLine();
            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity. Please enter a number.");
            return;
        }

        boolean success = cartController.addProductToCart(user, productUuid, quantity);
        if (success) {
            System.out.println("Product added to cart successfully!");
            viewCart(); // Show updated cart
        } else {
            System.out.println("Failed to add product to cart. Check product existence or stock.");
        }
    }

    public void updateCartItemQuantity() {
        System.out.println("\n--- Update Cart Item Quantity ---");
        viewCart(); // Show current cart first

        System.out.print("Enter Product UUID to update: ");
        String productUuid = scanner.nextLine();
        System.out.print("Enter New Quantity (0 to remove): ");
        int newQuantity;
        try {
            newQuantity = scanner.nextInt();
            scanner.nextLine();
            if (newQuantity < 0) {
                System.out.println("Quantity cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity. Please enter a number.");
            return;
        }

        boolean success = cartController.updateProductInCart(user, productUuid, newQuantity);
        if (success) {
            System.out.println("Cart quantity updated successfully!");
            viewCart(); // Show updated cart
        } else {
            System.out.println("Failed to update cart quantity. Check product ID or stock.");
        }
    }

    public void removeProductFromCart() {
        System.out.println("\n--- Remove Product from Cart ---");
        if (cartController.getAllProductInCart(user).isEmpty()) {
            System.out.println("There are no items in the cart.");
            return; // Exit if cart is empty
        }
        viewCart();

        System.out.print("Enter Product UUID to remove: ");
        String productUuid = scanner.nextLine();

        boolean success = cartController.removeProductFromCart(user, productUuid);
        if (success) {
            System.out.println("Product removed from cart successfully!");
            viewCart(); // Show updated cart
        } else {
            System.out.println("Failed to remove product from cart. Check product ID.");
        }
    }

    public void clearUserCart() {
        System.out.println("\n--- Clear Cart ---");
         // Show current cart first
        if (cartController.getAllProductInCart(user).isEmpty()) {
            System.out.println("Cart is already empty.");
            return;
        }
        viewCart();

        System.out.print("Are you sure you want to clear your cart? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirmation)) {
            boolean success = cartController.clearCart(user);
            if (success) {
                System.out.println("Your cart has been cleared!");
                viewCart(); // Show empty cart
            } else {
                System.out.println("Failed to clear cart.");
            }
        } else {
            System.out.println("Cart clear operation cancelled.");
        }
    }
}
