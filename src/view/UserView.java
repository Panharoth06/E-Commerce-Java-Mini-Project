package view;

import controller.UserController;
import util.LoginSession;

import java.util.Scanner;

public class UserView {
    private final static UserController controller = new UserController();

    public static void home() {
        Scanner scanner = new Scanner(System.in);
        OrderView orderView = new OrderView();

        while (true) {
            if (LoginSession.isLoggedIn()) {
                System.out.println("Already logged in as: " + LoginSession.getLoggedInUsername());
                orderView.placeOrder();
                break;
            }

            System.out.println("""
                    ==========================================
                                    User System
                    ==========================================
                    1. REGISTER
                    2. LOGIN
                    3. LOGOUT
                    4. EXIT
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
                    if (LoginSession.isLoggedIn()) {
                        LoginSession.clearSession();
                        System.out.println("You have been logged out.");
                    } else {
                        System.out.println("You are not logged in.");
                    }
                }

                case "4" -> {
                    System.out.println("Exiting program...");
                    System.exit(0);
                }

                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
}