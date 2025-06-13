// src/controller/CartController.java
package controller;

import model.dto.CartDto;
import model.dto.ProductDto; // To allow search/display product by UUID
import model.service.CartService;
import model.service.ProductService; // To interact with products
import view.CartView;
import view.ProductView; // To display products for selection

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class CartController {
    private final CartService cartService;
    private final CartView cartView;
    private final ProductService productService; // To allow searching products by UUID
    private final ProductView productView; // To display product search results

    // A placeholder for the current user ID. In a real app, this would come from a login system.
    private final Integer CURRENT_USER_ID = 1; // Example user ID

    private final Scanner scanner; // For user input

    public CartController(CartService cartService, CartView cartView, ProductService productService, ProductView productView) {
        this.cartService = cartService;
        this.cartView = cartView;
        this.productService = productService;
        this.productView = productView;
        this.scanner = new Scanner(System.in);
    }

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
                    cartView.displayErrorMessage("Invalid choice. Please try again.");
            }
        }
    }

    public void viewCart() {
        List<CartDto> cartItems = cartService.viewCart(CURRENT_USER_ID);
        cartView.displayCart(cartItems);
    }

    public void addProductToCartByUuid() {
        System.out.println("\n--- Add Product to Cart by UUID ---");
        // Optionally list all products first to help user find UUIDs
        System.out.println("Listing all available products (for reference):");
        productService.getAllProducts().forEach(p ->
                System.out.println("  UUID: " + p.getProductUuid() + " | Name: " + p.getProductName() + " | Price: " + p.getPrice() + " | Qty: " + p.getQuantity())
        );

        System.out.print("Enter Product UUID to add: ");
        String uuidString = scanner.nextLine();
        UUID productUuid;
        try {
            productUuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            cartView.displayErrorMessage("Invalid UUID format. Please try again.");
            return;
        }

        System.out.print("Enter Quantity: ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine());
            if (quantity <= 0) {
                cartView.displayErrorMessage("Quantity must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            cartView.displayErrorMessage("Invalid quantity. Please enter a number.");
            return;
        }

        boolean success = cartService.addProductToCart(CURRENT_USER_ID, productUuid, quantity);
        if (success) {
            cartView.displayMessage("Product added to cart successfully!");
            viewCart(); // Show updated cart
        } else {
            cartView.displayErrorMessage("Failed to add product to cart. Check product existence or stock.");
        }
    }

    public void updateCartItemQuantity() {
        System.out.println("\n--- Update Cart Item Quantity ---");
        viewCart(); // Show current cart first
        if (cartService.viewCart(CURRENT_USER_ID).isEmpty()) {
            return; // Exit if cart is empty
        }

        System.out.print("Enter Product ID to update: ");
        long productId;
        try {
            productId = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            cartView.displayErrorMessage("Invalid Product ID. Please enter a number.");
            return;
        }

        System.out.print("Enter New Quantity (0 to remove): ");
        int newQuantity;
        try {
            newQuantity = Integer.parseInt(scanner.nextLine());
            if (newQuantity < 0) {
                cartView.displayErrorMessage("Quantity cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            cartView.displayErrorMessage("Invalid quantity. Please enter a number.");
            return;
        }

        boolean success = cartService.updateCartItemQuantity(CURRENT_USER_ID, productId, newQuantity);
        if (success) {
            cartView.displayMessage("Cart quantity updated successfully!");
            viewCart(); // Show updated cart
        } else {
            cartView.displayErrorMessage("Failed to update cart quantity. Check product ID or stock.");
        }
    }

    public void removeProductFromCart() {
        System.out.println("\n--- Remove Product from Cart ---");
        viewCart(); // Show current cart first
        if (cartService.viewCart(CURRENT_USER_ID).isEmpty()) {
            return; // Exit if cart is empty
        }

        System.out.print("Enter Product ID to remove: ");
        long productId;
        try {
            productId = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            cartView.displayErrorMessage("Invalid Product ID. Please enter a number.");
            return;
        }

        boolean success = cartService.removeProductFromCart(CURRENT_USER_ID, productId);
        if (success) {
            cartView.displayMessage("Product removed from cart successfully!");
            viewCart(); // Show updated cart
        } else {
            cartView.displayErrorMessage("Failed to remove product from cart. Check product ID.");
        }
    }

    public void clearUserCart() {
        System.out.println("\n--- Clear Cart ---");
        viewCart(); // Show current cart first
        if (cartService.viewCart(CURRENT_USER_ID).isEmpty()) {
            cartView.displayMessage("Cart is already empty.");
            return;
        }

        System.out.print("Are you sure you want to clear your cart? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirmation)) {
            boolean success = cartService.clearUserCart(CURRENT_USER_ID);
            if (success) {
                cartView.displayMessage("Your cart has been cleared!");
                viewCart(); // Show empty cart
            } else {
                cartView.displayErrorMessage("Failed to clear cart.");
            }
        } else {
            cartView.displayMessage("Cart clear operation cancelled.");
        }
    }
}