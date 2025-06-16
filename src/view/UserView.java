package view;

import controller.ProductInsertionController;
import controller.UserController;
import util.LoginSession;

import java.util.Scanner;

public class UserView {
    private final static UserController controller = new UserController();
    static Scanner scanner = new Scanner(System.in);

    private static void thumbnail() {
        System.out.println("""
                ╔═══════════════════════════════════════════════════════════════════════╗
                ║                               E-Commerce                              ║
                ╟═══════════════════════════════════════════════════════════════════════╢
                ║ 1. View All Products in Store                                         ║
                ║ 2. Search Product                                                     ║
                ║ 3. Cart                                                               ║
                ║ 4. Order Product                                                      ║
                ║ 5. Logout                                                             ║
                ║ 6. Challenge                                                          ║
                ║ 0. Exit                                                               ║
                ╚═══════════════════════════════════════════════════════════════════════╝
                """);
    }

    private static void feature() {

        byte option;
        while (true) {
            thumbnail();
            System.out.print("[+] Insert your option: ");
            try {
                option = scanner.nextByte();
                switch (option) {
                    case 1 -> ProductView.listAllProductsInStoreSeparatedByCategory();
                    case 2 -> ProductView.searchProduct();
                    case 3 -> new CartView().showCartMenu();
                    case 4 -> new OrderView().placeOrder();
                    case 5 -> {
                        if (LoginSession.isLoggedIn()) {
                            LoginSession.clearSession();
                            System.out.println("You have been logged out.");
                        } else {
                            System.out.println("You are not logged in.");
                        }
                        scanner.nextLine();
                        home();
                    }
                    case 6 -> {
                        try {
                            ProductInsertionController.run();
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                    case 0 -> System.exit(0);
                    default -> System.out.println("Invalid option");
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

    }

    public static void home() {

        while (true) {
            if (LoginSession.isLoggedIn()) {
                System.out.println("Already logged in as: " + LoginSession.getLoggedInUsername());
                feature();
                break;
            }

            System.out.println("""
                    ==========================================
                                    User System
                    ==========================================
                    1. REGISTER
                    2. LOGIN
                    3. EXIT
                    ==========================================
                    """);

            System.out.print("[+] Choose option: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1" -> {
                    if (LoginSession.isLoggedIn()) {
                        System.out.println("You are already logged in.");
                        break;
                    }

                    System.out.print("[+] Enter Username: ");
                    String username = scanner.nextLine();
                    System.out.print("[+] Enter Email: ");
                    String email = scanner.nextLine();
                    System.out.print("[+] Enter Password: ");
                    String password = scanner.nextLine();

                    boolean success = controller.register(username, email, password);
                    if (success) {
                        System.out.println("Registration successful. You are now logged in.");
                    } else {
                        System.out.println("Registration failed. Try again.");
                    }
                }

                case "2" -> {
                    if (LoginSession.isLoggedIn()) {
                        System.out.println("You are already logged in.");
                        break;
                    }

                    System.out.print("[+] Enter Username: ");
                    String username = scanner.nextLine();
                    System.out.print("[+] Enter Password: ");
                    String password = scanner.nextLine();

                    boolean success = controller.login(username, password);
                    if (success) {
                        System.out.println("Login successful.");

                    } else {
                        System.out.println("Login failed. Invalid credentials.");
                    }
                }

                case "3" -> {
                    System.out.println("Exiting program...");
                    System.exit(0);
                }

                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
}