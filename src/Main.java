import configuration.DbConnection;
import util.PasswordEncryptor;
import view.ProductView;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        Connection connection = DbConnection.getDatabaseConnection();
        String password = PasswordEncryptor.hashPassword("koko!@#$@!");
        System.out.println(password);
        boolean isMatch = PasswordEncryptor.checkPassword("koko!@#$@!", password);
        if (isMatch) System.out.println("login successful");
        else System.out.println("login failed");
        ProductView.home();
    }
}
