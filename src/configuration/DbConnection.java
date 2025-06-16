package configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection {
    private static final String dbUrl = "jdbc:postgresql://localhost:5432/e_commerce";
    private static final String dbPassword = "qwer";
    private static final String dbUsername = "postgres";

    public static Connection getDatabaseConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
