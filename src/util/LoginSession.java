package util;

import java.io.*;

public class LoginSession {
    private static final String SESSION_FILE = "login.txt";

    public static void saveLoggedInUsername(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SESSION_FILE))) {
            writer.write(username);
        } catch (IOException e) {
            System.err.println("Failed to write session: " + e.getMessage());
        }
    }

    public static String getLoggedInUsername() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SESSION_FILE))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean isLoggedIn() {
        String user = getLoggedInUsername();
        return user != null && !user.isEmpty();
    }

    public static void clearSession() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SESSION_FILE))) {
            writer.write("");
        } catch (IOException e) {
            System.err.println("Failed to clear session: " + e.getMessage());
        }
    }
}